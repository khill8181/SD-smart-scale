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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


public class addDailyEntry extends AppCompatActivity {
    double calMassRatioG;
    double calCountRatio;
    SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
    SQLiteDatabase db;
    String food;
    String strEntryCalories;
    SharedPreferences sharedPreferences;
    double currentCalLeft;
    TextView calLeft;
    boolean isProportionEntry;
    protected ArrayList<String> proportionData;
    double sumOfRatios;
    TextView propEntryValue;
    TextView dbText;
    double totalCaloriesBeingProportioned;
    double calConsumedToday;
    double entryCalories;
    int calGoal;
    boolean isCountEntry;
    double firstEntryRatio;
    Intent intent;
    EditText calories;
    int foodID;
    double entryMass;
    EditText massFromScale;
    EditText massSeenByUser;
    boolean isCompleteDelayedMeasurement;
    String focusedDate;
    TextView units;
    String unitsString;
    Button unitToggle;
    boolean emptyEntryMass = false;
    Button tareButton;



    //bluetooth variables
    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket = null;
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
        LinearLayout proportionView = findViewById(R.id.proportionView);
        calories = (EditText) findViewById(R.id.calcCalories);
        units = findViewById(R.id.units);
        tareButton = findViewById(R.id.tareButton);
        unitsString = units.getText().toString();//defaults to "g"
        intent = getIntent();
        unitToggle = findViewById(R.id.unitToggle);
        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        focusedDate = sharedPreferences.getString("focusedDate","string");
        LinearLayout massFromScaleLayout = findViewById(R.id.massFromScaleLayout);
        isCompleteDelayedMeasurement = intent.getBooleanExtra("isCompleteDelayedMeasurement",false);
        if(!isCompleteDelayedMeasurement) massFromScaleLayout.setVisibility(View.GONE);
        isProportionEntry = intent.getBooleanExtra("isProportionEntry", false);
        isCountEntry = intent.getBooleanExtra("isCountEntry",false);
        TextView propEntryText = (TextView) findViewById(R.id.propEntryText);
        propEntryValue = (TextView) findViewById(R.id.propEntryValue);
        dbText = (TextView) findViewById(R.id.foodName);
        calLeft = (TextView) findViewById(R.id.calLeftAddingEntry);
        massFromScale = findViewById(R.id.massFromScale);
        massSeenByUser = (EditText) findViewById(R.id.massSeenByUser);
        db = smartscaleDBHelper.getReadableDatabase();
        setCaloriesLeft();
        if(isCountEntry)
        {

            unitsString = ""; units.setVisibility(View.GONE); unitToggle.setVisibility(View.GONE);
            tareButton.setVisibility(View.GONE);
        }
        massSeenByUser.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.toString().contentEquals("")) emptyEntryMass = true;
                calcCalories();
            }
        });
        if(!isProportionEntry) {
            proportionView.setVisibility(View.GONE);
            foodID = intent.getIntExtra("id", 0);
            Cursor cursor = db.query("Foodlist", new String[]{"food", "mass", "calories","count"}
                    , "_id = ?", new String[]{Integer.toString(foodID)}, null, null, null);
            if (cursor.moveToFirst()) {
                food = cursor.getString(0);
                double mass = cursor.getInt(1);
                double calories = cursor.getInt(2);
                int count = cursor.getInt(3);
                if (isCountEntry) calCountRatio = calories/count;
                else calMassRatioG = calories / mass;
            }
            dbText.setText(food);
            cursor.close();
        }
        //handles combos based on given weight
        else if (!isCountEntry)
        {
            proportionData = intent.getStringArrayListExtra("proportionData");
            for(int i = 1; i < proportionData.size() ; i += 5 )
                sumOfRatios += Double.parseDouble(proportionData.get(i));
            double chosenAmountOfCalories = intent.getDoubleExtra("comboCalAmount",-1);
            if (chosenAmountOfCalories != -1) totalCaloriesBeingProportioned = chosenAmountOfCalories;
            else totalCaloriesBeingProportioned = currentCalLeft;
            proportionedEntry();
        }
        //handles combo based on count of item
        else
        {
            ArrayList<String> firstEntry = intent.getStringArrayListExtra("firstEntry");
            food = firstEntry.get(0);
            int count = Integer.parseInt(firstEntry.get(2));
            double calories = Double.parseDouble(firstEntry.get(1));
            firstEntryRatio = Double.parseDouble(firstEntry.get(3));
            dbText.setText(food);
            calCountRatio = calories/count;
            proportionData = new ArrayList<String>();
            proportionData.add("token string for logic purposes in insertDailyEntry");
        }
        // josh addition////////////////////////////////////////////////////////////
        //For Bluetooth Connectivity
        final Button buttonConnect = findViewById(R.id.buttonConnect);
        // If a bluetooth device has been selected from SelectDeviceActivity
        SharedPreferences btDetail = getSharedPreferences("btDetail", MODE_PRIVATE);
        //deviceName = getIntent().getStringExtra("deviceName");
        deviceName = btDetail.getString("btName", null);
        if (deviceName != null){
            // Get the device address to make BT Connection
            deviceAddress = btDetail.getString("btAddress", null);
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
                        buttonConnect.setText("Weigh Mass");
                        buttonConnect.setEnabled(true);
                        if (msg.obj != null) {
                            btMass = msg.obj.toString();
                            EditText editText = (EditText) findViewById(R.id.massSeenByUser);
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
                else if (mmSocket != null){
                    connectedThread.write("r");
                }
            }
        });
        tareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mmSocket != null)
                    connectedThread.write("t");
            }
        });
        unitToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mmSocket != null)
                    connectedThread.write("u");
                if(unitsString.contentEquals("g")) {units.setText("oz"); unitsString = "oz";}
                else {units.setText("g"); unitsString = "g";}
                calcCalories();
            }
        });

        /////////////////////////////////////////////////////////////////////////////
    }

    public void toggleUnits(View view)
    {
        if(unitsString.contentEquals("g")) {units.setText("oz"); unitsString = "oz";}
        else {units.setText("g"); unitsString = "g";}
        calcCalories();
    }
    public void updateMassSeenByUser(View view)
    {
        if(isCompleteDelayedMeasurement)
        {
            double initialMeasurement = intent.getDoubleExtra("initialMeasurement",0);
            double calculatedMassMeasurement = initialMeasurement - Double.parseDouble(massFromScale.getText().toString());
            massSeenByUser.setText(String.format("%.1f", calculatedMassMeasurement));
        }
    }

    public void setCaloriesLeft() {
        Cursor calConsumedCursor = db.query("calories", new String[] {"calGoal","calConsumed"},"date = ?",
                new String [] {focusedDate},null,null,null);
        calConsumedCursor.moveToFirst();
        calGoal = calConsumedCursor.getInt(0);
        calConsumedToday = calConsumedCursor.getDouble(1);
        currentCalLeft = calGoal-calConsumedToday;
        calLeft.setText(String.format("%.1f", currentCalLeft));
    }

    public void proportionedEntry()
    {
        massSeenByUser.setText("");
        food = proportionData.get(0);
        double mass = Double.parseDouble(proportionData.get(2));
        double calories = Double.parseDouble(proportionData.get(3));
        calMassRatioG = calories/mass;
        double fractionOfTotalCalories = Double.parseDouble(proportionData.get(1))/sumOfRatios;
        proportionData.subList(0,5).clear();
        propEntryValue.setText(String.format("%.1f", fractionOfTotalCalories*totalCaloriesBeingProportioned));
        dbText.setText(food);
    }

    public void calcCalories()
    {
        if (emptyEntryMass) entryMass = 0;
        else entryMass = Double.parseDouble(massSeenByUser.getText().toString());

        if(isCountEntry) entryCalories = calCountRatio*entryMass;
        else if (unitsString.contentEquals("g")) entryCalories = calMassRatioG*entryMass;
        else entryCalories = calMassRatioG*28.35*entryMass;

        calories.setText(String.format("%.1f", entryCalories));
        calLeft.setText(String.format("%.1f", currentCalLeft-entryCalories));
        emptyEntryMass = false;
    }

    public void insertDailyEntry(View view)
    {
        String mealTime = sharedPreferences.getString("mealTime","string");
        if(isCompleteDelayedMeasurement) mealTime = intent.getStringExtra("mealTime");
        if(intent.getBooleanExtra("isDelayedMeasurement",false))
        {
            SmartscaleDatabaseHelper.insertDelayedMeasurement(db,foodID,entryMass,unitsString,mealTime);
            Intent newIntent = new Intent(this, MainActivity.class);
            startActivity(newIntent);
        }
        else {
            strEntryCalories = String.format("%.1f", Double.parseDouble(calories.getText().toString()));
            ContentValues contentValues = new ContentValues();
            contentValues.put("calConsumed", calConsumedToday + entryCalories);
            db.update("calories", contentValues, "date=?", new String[]{focusedDate});
            SmartscaleDatabaseHelper.insertEntry(db, food, focusedDate, entryMass, unitsString, entryCalories,mealTime);
            if (!isProportionEntry || proportionData.isEmpty()) {
                if(isCompleteDelayedMeasurement)
                    db.delete("delayedEntries","foodID = ?", new String[] {Integer.toString(foodID)} );
                db.close();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            else {
                setCaloriesLeft();
                if (!isCountEntry) proportionedEntry();
                else countComboSetup();
            }
        }
    }

    public void countComboSetup()
    {
        isCountEntry = false;
        proportionData = intent.getStringArrayListExtra("proportionData");
        sumOfRatios = firstEntryRatio;
        for(int i = 1; i < proportionData.size() ; i += 5 )
            sumOfRatios += Double.parseDouble(proportionData.get(i));
        totalCaloriesBeingProportioned = entryCalories*(sumOfRatios/firstEntryRatio);
        unitsString = "g";
        unitToggle.setVisibility(View.VISIBLE);units.setVisibility(View.VISIBLE);
        tareButton.setVisibility(View.VISIBLE);
        proportionedEntry();

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
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}