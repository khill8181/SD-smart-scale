package com.example.Smartscale;

import androidx.appcompat.app.AppCompatActivity;
//Bluetooth imports
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;


public class addDailyEntry extends AppCompatActivity {
   //different input cases
    boolean isProportionEntry;
    boolean isCountEntry;
    boolean isCompleteDelayedMeasurement;
    boolean isBeginDelayedMeasurement;
    boolean isRecipeItem;
    boolean isRecipeFinalMass;
    //general variables
    double calMassRatioG;
    double calCountRatio;
    SQLiteOpenHelper smartscaleDBHelper = new SmartscaleDatabaseHelper(this);
    SQLiteDatabase db;
    String food;
    SharedPreferences sharedPreferences;
    double currentCalLeft;
    TextView calLeft;
    protected ArrayList<String> proportionData;
    double sumOfRatios;
    TextView propEntryValue;
    TextView dbText;
    double totalCaloriesBeingProportioned;
    double calConsumedToday;
    double entryCalories;
    int calGoal;
    double firstEntryRatio;
    Intent intent;
    EditText calories;
    int foodID;
    double entryMass;
    TextView massFromScale;
    EditText ETfoodQuantity;
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
    boolean isConnected = false;
    String btMass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_daily_entry);
        LinearLayout proportionView = findViewById(R.id.proportionView);
        calories = findViewById(R.id.calcCalories);
        units = findViewById(R.id.units);
        Button addEntryBttn = findViewById(R.id.addEntryBttn);
        tareButton = findViewById(R.id.tareButton);
        final Button buttonConnect = findViewById(R.id.connectButton);
        unitsString = units.getText().toString();//defaults to "g"
        intent = getIntent();
        unitToggle = findViewById(R.id.unitToggle);
        dbText = (TextView) findViewById(R.id.foodName);
        sharedPreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);
        focusedDate = sharedPreferences.getString("focusedDate","string");
        LinearLayout massFromScaleLayout = findViewById(R.id.massFromScaleLayout);
        LinearLayout calorieLayout = findViewById(R.id.calorieLayout);
        LinearLayout recipeItemBttnLayout = findViewById(R.id.recipeItemButtonLayout);
        LinearLayout recipeETLayout = findViewById(R.id.recipeETLayout);
        LinearLayout caloriesLeftLayout = findViewById(R.id.caloriesLeftLayout);


        isCompleteDelayedMeasurement = intent.getBooleanExtra("isCompleteDelayedMeasurement",false);
        isBeginDelayedMeasurement = intent.getBooleanExtra("isBeginDelayedMeasurement",false);
        isRecipeItem = intent.getBooleanExtra("isRecipeItem",false);
        isRecipeFinalMass = intent.getBooleanExtra("isRecipeFinalMass",false);

        if(!isCompleteDelayedMeasurement) massFromScaleLayout.setVisibility(View.GONE);
        if(isBeginDelayedMeasurement) calorieLayout.setVisibility(View.GONE);

        if(isRecipeFinalMass){calorieLayout.setVisibility(View.GONE);dbText.setVisibility(View.GONE);}
        else recipeETLayout.setVisibility(View.GONE);

        if(isRecipeItem) {addEntryBttn.setVisibility(View.GONE);caloriesLeftLayout.setVisibility(View.GONE);}
        else recipeItemBttnLayout.setVisibility(View.GONE);

        isProportionEntry = intent.getBooleanExtra("isProportionEntry", false);
        isCountEntry = intent.getBooleanExtra("isCountEntry",false);
        TextView propEntryText = (TextView) findViewById(R.id.propEntryText);
        propEntryValue = (TextView) findViewById(R.id.propEntryValue);
        calLeft = (TextView) findViewById(R.id.calLeftAddingEntry);
        massFromScale = findViewById(R.id.massFromScale);
        ETfoodQuantity = findViewById(R.id.ETfoodQuantity);
        disableEditText(calories);
        db = smartscaleDBHelper.getReadableDatabase();
        setCaloriesLeft();

        //LinearLayout foodMassLayout = findViewById(R.id.foodMassLayout);
        if(isCountEntry)
        {
            unitsString = "";
            units.setText(unitsString);
            unitToggle.setVisibility(View.GONE);
            tareButton.setVisibility(View.GONE);
            buttonConnect.setVisibility(View.GONE);
        }
        else    disableEditText(ETfoodQuantity);

        ETfoodQuantity.addTextChangedListener(new TextWatcher() {

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

        massFromScale.addTextChangedListener(new TextWatcher() {
            double delayedMeasurementInitialValue = intent.getDoubleExtra("initialMeasurement",0);
            double calculatedMassMeasurement = 0;
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                String mass = massFromScale.getText().toString();
                if (mass.isEmpty() || mass.equals(".")) return;
                calculatedMassMeasurement = delayedMeasurementInitialValue - Double.parseDouble(massFromScale.getText().toString());
                ETfoodQuantity.setText(String.format("%.1f", calculatedMassMeasurement));
                    }
                }
            );

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
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress, this);
            createConnectThread.start();
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg){
                switch (msg.what){
                    case CONNECTING_STATUS:
                        switch(msg.arg1){
                            case 1:
                                disableEditText(ETfoodQuantity);
                                //buttonConnect.setEnabled(true);
                                break;
                            case -1:
                                if (isCompleteDelayedMeasurement)
                                    massFromScale.setEnabled(true);
                                else
                                    ETfoodQuantity.setEnabled(true);
                                buttonConnect.setEnabled(true);
                                buttonConnect.setText("Connect scale");
                                break;
                        }
                    case MESSAGE_READ:
                        //buttonConnect.setText("Weigh Mass");
                        //buttonConnect.setEnabled(true);
                        if (msg.obj != null && !isCountEntry) {
                            btMass = msg.obj.toString();
                            if(btMass.charAt(btMass.length() - 2) == 'g') {
                                unitsString = "g";
                                units.setText("g");
                            }
                            else {
                                unitsString = "oz";
                                units.setText("oz");
                            }

                            EditText editText;

                            if (isCompleteDelayedMeasurement) {
                                editText = (EditText) findViewById(R.id.massFromScale);
                            }
                            else {
                                editText = (EditText) findViewById(R.id.ETfoodQuantity);
                            }
                            buttonConnect.setText("Connected");
                            buttonConnect.setEnabled(false);
                            editText.setText(btMass.substring(0, btMass.length() - 2));
                            //ETfoodQuantity.setText(btMass.substring(0, btMass.length() - 2))
                        }

                        break;
                }
            }
        };
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                if (deviceName == null && !isCountEntry){
                    Intent intent = new Intent(addDailyEntry.this, SelectDeviceActivity.class);
                    startActivity(intent);
                }
                else if (isConnected == false){
                    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress, addDailyEntry.this);
                    createConnectThread.start();
                }
            }
        });
        tareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mmSocket != null && isConnected)
                    connectedThread.write("t");
            }
        });
        unitToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isConnected == true) //(mmSocket != null)
                    connectedThread.write("u");
                else {//if (CONNECTING_STATUS == -1) {
                    if (unitsString.contentEquals("g")) {
                        units.setText("oz");
                        unitsString = "oz";
                    } else {
                        units.setText("g");
                        unitsString = "g";
                    }
                }
                if(ETfoodQuantity.getText().toString().contentEquals("")) emptyEntryMass = true;
                calcCalories();
            }
        });

        /////////////////////////////////////////////////////////////////////////////

        BroadcastReceiver mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                    isConnected = false;
                    if (isCompleteDelayedMeasurement)
                        massFromScale.setEnabled(true);
                    else
                        ETfoodQuantity.setEnabled(true);
                    buttonConnect.setText("Connect Scale");
                    buttonConnect.setEnabled(true);
                    Log.e("Bluetooth", "lost connection to scale");
                }
                else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                    isConnected = true;
                    if (isCompleteDelayedMeasurement)
                        massFromScale.setEnabled(false);
                    else
                        ETfoodQuantity.setEnabled(false);
                    buttonConnect.setText("Connected");
                    buttonConnect.setEnabled(false);
                }
            }
        };

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        IntentFilter filter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mReceiver, filter1);
        this.registerReceiver(mReceiver, filter2);
    }



    public void simulateScaleValues(View view)
    {
        EditText ETsimulateScaleInput = findViewById(R.id.ETsimulateScaleInput);
        if(isCompleteDelayedMeasurement) massFromScale.setText(ETsimulateScaleInput.getText());
        else ETfoodQuantity.setText(ETsimulateScaleInput.getText());
    }

    private void disableEditText(EditText editText) {

        editText.setEnabled(false);
        editText.setTextColor(Color.BLACK);
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
        else entryMass = Double.parseDouble(ETfoodQuantity.getText().toString());

        if(isCountEntry) entryCalories = calCountRatio*entryMass;
        else if (unitsString.contentEquals("g")) entryCalories = calMassRatioG*entryMass;
        else entryCalories = calMassRatioG*28.35*entryMass;

        calories.setText(String.format("%.1f", entryCalories));
        calLeft.setText(String.format("%.1f", currentCalLeft-entryCalories));
        emptyEntryMass = false;
    }

    public void addAnotherItemToRecipe(View view)
    {
        float currentCalorieTotal = sharedPreferences.getFloat("recipeCalorieTotal",0);
        currentCalorieTotal += entryCalories;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("recipeCalorieTotal",currentCalorieTotal); editor.commit();
        Intent intent = new Intent(this,chooseFood.class);
        intent.putExtra("isRecipeItem",true);
        startActivity(intent);
    }

    public void getRecipeFinalMass(View view)
    {
        float currentCalorieTotal = sharedPreferences.getFloat("recipeCalorieTotal",0);
        currentCalorieTotal += entryCalories;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("recipeCalorieTotal",currentCalorieTotal); editor.commit();
        Intent intent = new Intent(this,addDailyEntry.class);
        intent.putExtra("isRecipeFinalMass",true);
        startActivity(intent);
    }

    public void insertDailyEntry(View view)
    {
        String mealTime = sharedPreferences.getString("mealTime","string");
        if(isCompleteDelayedMeasurement) mealTime = intent.getStringExtra("mealTime");
        if(isBeginDelayedMeasurement)
        {
            SmartscaleDatabaseHelper.insertDelayedMeasurement(db,foodID,entryMass,unitsString,mealTime);
            Intent newIntent = new Intent(this, MainActivity.class);
            startActivity(newIntent);
        }
        else if(isRecipeFinalMass)
        {
            int count = 0;
            String recipeName = ((EditText)findViewById(R.id.recipeNameET)).getText().toString();
            String recipeCount = ((EditText)findViewById(R.id.recipeCountET)).getText().toString();
            if (!recipeCount.contentEquals("")) count = Integer.parseInt(recipeCount);
            if (recipeName.contentEquals(""))
            {
                Context context = getApplicationContext();
                CharSequence text = "A recipe name must be provided to store this new food";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else
            {
                int calories = (int)sharedPreferences.getFloat("recipeCalorieTotal",0);
                SmartscaleDatabaseHelper.insertNewFood(db,recipeName,entryMass,calories,count);
                Context context = getApplicationContext();
                CharSequence text = "Food added";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                startActivity(new Intent(this,MainActivity.class));
            }
        }
        else {
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
        units.setText(unitsString);
        disableEditText(ETfoodQuantity);
        unitToggle.setVisibility(View.VISIBLE);units.setVisibility(View.VISIBLE);
        tareButton.setVisibility(View.VISIBLE);
        proportionedEntry();

        /*
        //final Button buttonConnect = findViewById(R.id.buttonConnect);
        // If a bluetooth device has been selected from SelectDeviceActivity
        SharedPreferences btDetail = getSharedPreferences("btDetail", MODE_PRIVATE);
        //deviceName = getIntent().getStringExtra("deviceName");
        deviceName = btDetail.getString("btName", null);
        if (deviceName != null){
            deviceAddress = btDetail.getString("btAddress", null);
            //buttonConnect.setText("Connecting to " + deviceName + "...");
            //buttonConnect.setEnabled(false);
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter,deviceAddress, this);
            createConnectThread.start();
        }*/
    }
    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        addDailyEntry parent;

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address, addDailyEntry parent) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();
            this.parent = parent;

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
                this.parent.isConnected = true;
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
            connectedThread.run(parent);
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

        public void run(addDailyEntry parent) {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            if (parent.unitsString.equals("g"))
                connectedThread.write("g");
            else
                connectedThread.write("o");
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
                        connectedThread.write("r");
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
                connectedThread.write("d");
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {

    /*    // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);*/
        if(isRecipeFinalMass)
            {cancelNewRecipeDialogFragment cancelRecipe = new cancelNewRecipeDialogFragment();
            cancelRecipe.show(getSupportFragmentManager(),"cancel recipe");}
        else
            super.onBackPressed();
    }
}