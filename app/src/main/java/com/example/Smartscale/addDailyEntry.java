package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;
//Bluetooth imports
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;

import static android.content.ContentValues.TAG;


//
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


public class addDailyEntry extends AppCompatActivity {
    double calMassRatio;
    SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
    SQLiteDatabase db;
    String food;
    String strEntryCalories;
    String strEntryMass;
    String strCalLeft;
    SharedPreferences sharedpreferences;
    double currentCalLeft;
    double projectedCaloriesLeft;
    TextView calLeft;
    boolean isProportionEntry;
    protected ArrayList<String> proportionData;
    double sumOfRatios;
    TextView propEntryValue;
    TextView dbText;
    double totalCaloriesBeingProportioned;


    //bluetooth variables
    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    String btMass;
    //
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_entry);
        Intent intent = getIntent();
        isProportionEntry = intent.getBooleanExtra("isProportionEntry", false);
        TextView propEntryText = (TextView) findViewById(R.id.propEntryText);
        propEntryValue = (TextView) findViewById(R.id.propEntryValue);
        dbText = (TextView) findViewById(R.id.foodName);
        calLeft = (TextView) findViewById(R.id.calLeftAddingEntry);
        setCaloriesLeft();
        db = smartscaleDBHelper.getReadableDatabase();
        if(!isProportionEntry) {
            propEntryText.setVisibility(View.GONE);
            propEntryValue.setVisibility(View.GONE);
            int intNum = intent.getIntExtra("id", 0);
            Cursor cursor = db.query("Foodlist", new String[]{"food", "mass", "calories"}
                    , "_id = ?", new String[]{Integer.toString(intNum)}, null, null, null);
            if (cursor.moveToFirst()) {
                food = cursor.getString(0);
                double mass = Double.parseDouble(cursor.getString(1));
                double calories = Double.parseDouble(cursor.getString(2));
                calMassRatio = calories / mass;
            }
            dbText.setText(food);
            cursor.close();
        }
        else
        {
            proportionData = intent.getStringArrayListExtra("proportionData");
            for(int i = 1; i < proportionData.size() ; i += 4 )
                sumOfRatios += Double.parseDouble(proportionData.get(i));
            totalCaloriesBeingProportioned = currentCalLeft;
            proportionedEntry();

        }
        // josh addition////////////////////////////////////////////////////////////
        //For Bluetooth Connectivity
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        // If a bluetooth device has been selected from SelectDeviceActivity

        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            buttonConnect.setText("Connecting to " + deviceName + "...");
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress);
            createConnectThread.start();
        }



        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                buttonConnect.setText("Weigh Mass");
                                buttonConnect.setEnabled(true);
                                break;
                            case -1:
                                buttonConnect.setText("Connect Scale");
                                buttonConnect.setEnabled(true);
                                break;
                        }
                    case MESSAGE_READ:
                        if (msg.obj != null) {
                            btMass = msg.obj.toString();
                            EditText editText = (EditText) findViewById(R.id.givenMass);
                            editText.setText(btMass);
                        }

                        break;
                }
            }
        };
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                if (deviceName == null){
                    Intent intent = new Intent(addDailyEntry.this, SelectDeviceActivity.class);
                    startActivity(intent);
                }
                else{
                    String askWeight = "asking";
                    connectedThread.write(askWeight);
                }

            }
        });

        /////////////////////////////////////////////////////////////////////////////
    }

    public void setCaloriesLeft()
    {
        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        currentCalLeft = sharedpreferences.getFloat("calLeft", 2000);
        calLeft.setText(String.format("%.1f", currentCalLeft));
    }

    public void proportionedEntry()
    {
        food = proportionData.get(0);
        double mass = Double.parseDouble(proportionData.get(2));
        double calories = Double.parseDouble(proportionData.get(3));
        calMassRatio = calories/mass;
        double fractionOfTotalCalories = Double.parseDouble(proportionData.get(1))/sumOfRatios;
        proportionData.subList(0,4).clear();
        propEntryValue.setText(String.format("%.1f", fractionOfTotalCalories*totalCaloriesBeingProportioned));
        dbText.setText(food);
    }

    public void calcCalories(View view)
    {
        EditText editText = (EditText) findViewById(R.id.givenMass);
        strEntryMass = editText.getText().toString();
        double entryMass = Double.parseDouble(strEntryMass);
        double entryCalories = calMassRatio*entryMass;
        strEntryCalories = String.format("%.1f", entryCalories);
        strEntryMass = String.format("%.1f", entryMass);
        TextView calories = (TextView) findViewById(R.id.calcCalories);
        calories.setText(strEntryCalories);
        projectedCaloriesLeft = currentCalLeft-entryCalories;
        strCalLeft = String.format("%.1f", projectedCaloriesLeft);
        calLeft.setText(strCalLeft);
    }

    public void insertDailyEntry(View view)
    {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat("calLeft", (float) projectedCaloriesLeft);
        editor.commit();
        SmartscaleDatabaseHelper.insertEntry(db, food, strEntryMass, strEntryCalories);
        if (!isProportionEntry) db.close();
        if (!isProportionEntry || proportionData.isEmpty())
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else
        {
            setCaloriesLeft();
            proportionedEntry();
        }
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n'){
                        readMessage = new String(buffer,0,bytes);
                        Log.e("Arduino Message",readMessage);
                        handler.obtainMessage(MESSAGE_READ,readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error","Unable to send message",e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null){
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}