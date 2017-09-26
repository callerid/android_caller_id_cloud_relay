package www.callerid.com.androidcalleridcloudrelay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.IBinder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import www.callerid.com.androidcalleridcloudrelay.Classes.Poster;
import www.callerid.com.androidcalleridcloudrelay.Classes.ServiceCallbacks;
import www.callerid.com.androidcalleridcloudrelay.Classes.UDPListen;

public class actMain extends Activity implements ServiceCallbacks {

    // UDP listen requirements
    private UDPListen mService;
    private boolean mBound = false;
    private String inString = "Waiting for Calls.";

    // Required for memory capturing during app in background
    private boolean isInFront;

    // Create all UI variables
    private CheckBox ckbRequiresAuth;
    private TextView tbUsername;
    private TextView tbPassword;

    private RadioButton rbUseSuppliedURL;
    private RadioButton rbUseBuiltURL;
    private RadioButton rbDeluxeUnit;
    private RadioButton rbBasicUnit;
    private Button btnTestURL;

    private TextView tbSuppliedURL;
    private TextView lbSuppliedURL;
    private Button btnPaste;

    private TextView lbDevSection;

    private TextView lbBuiltURL;
    private TextView lbGeneratedURL;
    private Button btnGenerateURL;
    private Button btnCopyGenURL;

    private TextView lbServer;
    private TextView tbServer;

    private TextView lbCallerIDVars;
    private TextView lbYourVars;
    private TextView lbDescriptions;

    private TextView lbLine;
    private TextView lbLineD;
    private TextView tbLine;

    private TextView lbTime;
    private TextView lbTimeD;
    private TextView tbTime;

    private TextView lbPhone;
    private TextView lbPhoneD;
    private TextView tbPhone;

    private TextView lbName;
    private TextView lbNameD;
    private TextView tbName;

    private TextView lbIO;
    private TextView lbIOD;
    private TextView tbIO;

    private TextView lbSE;
    private TextView lbSED;
    private TextView tbSE;

    private TextView lbStatus;
    private TextView lbStatusD;
    private TextView tbStatus;

    private TextView lbDuration;
    private TextView lbDurationD;
    private TextView tbDuration;

    private TextView lbRingNumber;
    private TextView lbRingNumberD;
    private TextView tbRingNumber;

    private TextView lbRingType;
    private TextView lbRingTypeD;
    private TextView tbRingType;

    private TableLayout tableCallLog;
    private ScrollView svCallLog;

    private Button btnClearLog;

    // ---------------------------------------------------------------------------------------------------- Activity Functions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_main);

        // Link UI variables
        LinkAllUIControls();

        // Load all settings variables
        // TODO

        // Start UDP listener
        // TODO

        // Attach event handlers
        ckbRequiresAuth.setOnClickListener(ckbRequiresAuth_Click);

        rbUseBuiltURL.setOnClickListener(rbChangeToBuilt);
        rbUseSuppliedURL.setOnClickListener(rbChangeToSupplied);

        rbDeluxeUnit.setOnClickListener(rbChangeToDeluxe);
        rbBasicUnit.setOnClickListener(rbChangeToBasic);

        // Update UI based on loaded in values
        ckbRequiresAuth_Click.onClick(new View(this));
        changeURL(rbUseSuppliedURL.isChecked());
        changeUnit(rbDeluxeUnit.isChecked());

    }

    @Override
    public void onResume() {
        super.onResume();
        isInFront = true;
    }

    // Setup connection/binder to service
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            UDPListen.LocalBinder binder = (UDPListen.LocalBinder) iBinder;
            mService = binder.getService();
            mBound = true;
            mService.setCallbacks(actMain.this);

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }

    };

    @Override
    protected void onStart() {
        super.onStart();

        // bind to Service
        Intent intent = new Intent(this, UDPListen.class);
        mBound = bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public void onPause() {
        super.onPause();
        isInFront = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // end listener Service
        Intent intent = new Intent(this, UDPListen.class);
        stopService(intent);
        unbindService(mConnection);

    }

    // ----------------------------------------------------------------------------------- Link UI controls to JAVA variables
    private void LinkAllUIControls(){

        ckbRequiresAuth = (CheckBox) findViewById(R.id.ckbRequiresAuth);
        tbUsername = (TextView) findViewById(R.id.tbUsername);
        tbPassword = (TextView) findViewById(R.id.tbPassword);

        rbUseSuppliedURL = (RadioButton) findViewById(R.id.rbUseSuppliedURL);
        rbUseBuiltURL = (RadioButton)findViewById(R.id.rbUseBuiltURL);
        rbDeluxeUnit = (RadioButton)findViewById(R.id.rbDeluxeUnit);
        rbBasicUnit = (RadioButton)findViewById(R.id.rbBasicUnit);
        btnTestURL = (Button)findViewById(R.id.btnTestURL);

        lbDevSection = (TextView)findViewById(R.id.lbDevSection);

        tbSuppliedURL = (TextView)findViewById(R.id.tbSuppliedURL);
        lbSuppliedURL = (TextView)findViewById(R.id.lbSuppliedURL);
        btnPaste = (Button)findViewById(R.id.btnPaste);

        lbBuiltURL = (TextView)findViewById(R.id.lbBuiltURL);
        lbGeneratedURL = (TextView)findViewById(R.id.lbGeneratedURL);
        btnGenerateURL = (Button)findViewById(R.id.btnGenerateURL);
        btnCopyGenURL = (Button)findViewById(R.id.btnCopyGenURL);

        lbServer = (TextView)findViewById(R.id.lbServer);
        tbServer = (TextView)findViewById(R.id.tbServer);

        lbCallerIDVars = (TextView)findViewById(R.id.lbCallerIDVars);
        lbYourVars = (TextView)findViewById(R.id.lbYourVar);
        lbDescriptions = (TextView)findViewById(R.id.lbDescriptions);

        lbLine = (TextView)findViewById(R.id.lbLine);
        lbLineD = (TextView)findViewById(R.id.lbLineD);
        tbLine = (TextView)findViewById(R.id.tbLine);

        lbTime = (TextView)findViewById(R.id.lbTime);
        lbTimeD = (TextView)findViewById(R.id.lbTimeD);
        tbTime = (TextView)findViewById(R.id.tbTime);

        lbPhone = (TextView)findViewById(R.id.lbPhone);
        lbPhoneD = (TextView)findViewById(R.id.lbPhoneD);
        tbPhone = (TextView)findViewById(R.id.tbPhone);

        lbName = (TextView)findViewById(R.id.lbName);
        lbNameD = (TextView)findViewById(R.id.lbNameD);
        tbName = (TextView)findViewById(R.id.tbName);

        lbIO = (TextView)findViewById(R.id.lbIO);
        lbIOD = (TextView)findViewById(R.id.lbIOD);
        tbIO = (TextView)findViewById(R.id.tbIO);

        lbSE = (TextView)findViewById(R.id.lbSE);
        lbSED = (TextView)findViewById(R.id.lbSED);
        tbSE = (TextView)findViewById(R.id.tbSE);

        lbStatus = (TextView)findViewById(R.id.lbStatus);
        lbStatusD = (TextView)findViewById(R.id.lbStatusD);
        tbStatus = (TextView)findViewById(R.id.tbStatus);

        lbDuration = (TextView)findViewById(R.id.lbDuration);
        lbDurationD = (TextView)findViewById(R.id.lbDurationD);
        tbDuration = (TextView)findViewById(R.id.tbDuration);

        lbRingNumber = (TextView)findViewById(R.id.lbRingNumber);
        lbRingNumberD = (TextView)findViewById(R.id.lbRingNumberD);
        tbRingNumber = (TextView)findViewById(R.id.tbRingNumber);

        lbRingType = (TextView)findViewById(R.id.lbRingType);
        lbRingTypeD = (TextView)findViewById(R.id.lbRingTypeD);
        tbRingType = (TextView)findViewById(R.id.tbRingType);

        tableCallLog = (TableLayout)findViewById(R.id.tableCallLog);
        svCallLog = (ScrollView)findViewById(R.id.svCallLog);

        btnClearLog = (Button)findViewById(R.id.btnClearLog);

    }

    // ------------------------------------------------------------------------------------------------------- Event Handlers
    private View.OnClickListener ckbRequiresAuth_Click = new View.OnClickListener() {
        public void onClick(View v) {
            // Toggle auth parameters
            tbUsername.setEnabled(ckbRequiresAuth.isChecked());
            tbPassword.setEnabled(ckbRequiresAuth.isChecked());
        }
    };

    private View.OnClickListener rbChangeToSupplied = new View.OnClickListener() {
        public void onClick(View v) {
            changeURL(true);
        }
    };
    private View.OnClickListener rbChangeToBuilt = new View.OnClickListener() {
        public void onClick(View v) {
            changeURL(false);
        }
    };

    private View.OnClickListener rbChangeToDeluxe = new View.OnClickListener() {
        public void onClick(View v) {
            changeUnit(true);
        }
    };
    private View.OnClickListener rbChangeToBasic = new View.OnClickListener() {
        public void onClick(View v) {
            changeUnit(false);
        }
    };

    // --------------------------------------------------------------------------------------------------------- UI Updating

    private void changeURL(boolean isSupplied){

        if(isSupplied){

            rbUseSuppliedURL.setChecked(true);
            rbUseBuiltURL.setChecked(false);
            toggleDevSection(false);

        }
        else{

            rbUseSuppliedURL.setChecked(false);
            rbUseBuiltURL.setChecked(true);
            toggleDevSection(true);

        }

    }

    private void toggleDevSection(boolean isSupplied){

        if(isSupplied){

            lbDevSection.setTextColor(Color.GRAY);
            lbBuiltURL.setTextColor(Color.GRAY);
            lbGeneratedURL.setTextColor(Color.GRAY);
            btnGenerateURL.setEnabled(false);
            lbServer.setTextColor(Color.GRAY);

            tbServer.setTextColor(Color.GRAY);
            tbServer.setEnabled(false);

            lbCallerIDVars.setTextColor(Color.GRAY);
            lbYourVars.setTextColor(Color.GRAY);
            lbDescriptions.setTextColor(Color.GRAY);

            lbLine.setTextColor(Color.GRAY);
            lbLineD.setTextColor(Color.GRAY);
            tbLine.setTextColor(Color.GRAY);
            tbLine.setEnabled(false);

            lbTime.setTextColor(Color.GRAY);
            lbTimeD.setTextColor(Color.GRAY);
            tbTime.setTextColor(Color.GRAY);
            tbTime.setEnabled(false);

            lbPhone.setTextColor(Color.GRAY);
            lbPhoneD.setTextColor(Color.GRAY);
            tbPhone.setTextColor(Color.GRAY);
            tbPhone.setEnabled(false);

            lbName.setTextColor(Color.GRAY);
            lbNameD.setTextColor(Color.GRAY);
            tbName.setTextColor(Color.GRAY);
            tbName.setEnabled(false);

            toggleDeluxe(rbDeluxeUnit.isChecked());

        }
        else{

            if(lbGeneratedURL.getText().toString().contains("must generate")){
                lbGeneratedURL.setTextColor(Color.RED);
            }
            else{
                lbGeneratedURL.setTextColor(Color.GREEN);
            }

            lbDevSection.setTextColor(Color.BLACK);
            lbBuiltURL.setTextColor(Color.BLACK);
            btnGenerateURL.setEnabled(true);
            lbServer.setTextColor(Color.BLACK);

            tbServer.setTextColor(Color.BLACK);
            tbServer.setEnabled(true);

            lbCallerIDVars.setTextColor(Color.BLACK);
            lbYourVars.setTextColor(Color.BLACK);
            lbDescriptions.setTextColor(Color.BLACK);

            lbLine.setTextColor(Color.BLACK);
            lbLineD.setTextColor(Color.BLACK);
            tbLine.setTextColor(Color.BLACK);
            tbLine.setEnabled(true);

            lbTime.setTextColor(Color.BLACK);
            lbTimeD.setTextColor(Color.BLACK);
            tbTime.setTextColor(Color.BLACK);
            tbTime.setEnabled(true);

            lbPhone.setTextColor(Color.BLACK);
            lbPhoneD.setTextColor(Color.BLACK);
            tbPhone.setTextColor(Color.BLACK);
            tbPhone.setEnabled(true);

            lbName.setTextColor(Color.BLACK);
            lbNameD.setTextColor(Color.BLACK);
            tbName.setTextColor(Color.BLACK);
            tbName.setEnabled(true);

            toggleDeluxe(rbDeluxeUnit.isChecked());

        }

    }

    private void changeUnit(boolean isDeluxe){

        if(isDeluxe){

            rbDeluxeUnit.setChecked(true);
            rbBasicUnit.setChecked(false);
            toggleDeluxe(true);

        }
        else{

            rbDeluxeUnit.setChecked(false);
            rbBasicUnit.setChecked(true);
            toggleDeluxe(false);

        }

    }

    private void toggleDeluxe(boolean isDeluxe){

        if(isDeluxe){

            lbIO.setTextColor(Color.BLACK);
            lbIOD.setTextColor(Color.BLACK);
            tbIO.setTextColor(Color.BLACK);
            tbIO.setEnabled(true);

            lbSE.setTextColor(Color.BLACK);
            lbSED.setTextColor(Color.BLACK);
            tbSE.setTextColor(Color.BLACK);
            tbSE.setEnabled(true);

            lbStatus.setTextColor(Color.BLACK);
            lbStatusD.setTextColor(Color.BLACK);
            tbStatus.setTextColor(Color.BLACK);
            tbStatus.setEnabled(true);

            lbDuration.setTextColor(Color.BLACK);
            lbDurationD.setTextColor(Color.BLACK);
            tbDuration.setTextColor(Color.BLACK);
            tbDuration.setEnabled(true);

            lbRingNumber.setTextColor(Color.BLACK);
            lbRingNumberD.setTextColor(Color.BLACK);
            tbRingNumber.setTextColor(Color.BLACK);
            tbRingNumber.setEnabled(true);

            lbRingType.setTextColor(Color.BLACK);
            lbRingTypeD.setTextColor(Color.BLACK);
            tbRingType.setTextColor(Color.BLACK);
            tbRingType.setEnabled(true);

        }
        else{

            lbIO.setTextColor(Color.GRAY);
            lbIOD.setTextColor(Color.GRAY);
            tbIO.setTextColor(Color.GRAY);
            tbIO.setEnabled(false);

            lbSE.setTextColor(Color.GRAY);
            lbSED.setTextColor(Color.GRAY);
            tbSE.setTextColor(Color.GRAY);
            tbSE.setEnabled(false);

            lbStatus.setTextColor(Color.GRAY);
            lbStatusD.setTextColor(Color.GRAY);
            tbStatus.setTextColor(Color.GRAY);
            tbStatus.setEnabled(false);

            lbDuration.setTextColor(Color.GRAY);
            lbDurationD.setTextColor(Color.GRAY);
            tbDuration.setTextColor(Color.GRAY);
            tbDuration.setEnabled(false);

            lbRingNumber.setTextColor(Color.GRAY);
            lbRingNumberD.setTextColor(Color.GRAY);
            tbRingNumber.setTextColor(Color.GRAY);
            tbRingNumber.setEnabled(false);

            lbRingType.setTextColor(Color.GRAY);
            lbRingTypeD.setTextColor(Color.GRAY);
            tbRingType.setTextColor(Color.GRAY);
            tbRingType.setEnabled(false);

        }

    }

    // --------------------------------------------------------------------------------------------------- Call Log Features

    private void addCallToLog(String myLine,String myType,
                              String myIndicator,String myDuration,
                              String myCheckSum,String myRings,
                              String myDateTime,String myNumber,
                              String myName){

        // Print call to call log
        TableRow newRow = new TableRow(this);
        newRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT,TableRow.LayoutParams.WRAP_CONTENT));

        TextView tv = new TextView(this);

        // Line
        tv.setText("" + myLine);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // I/O
        tv = new TextView(this);
        tv.setText("" + myType);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Start/End
        tv = new TextView(this);
        tv.setText("" + myIndicator);
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Duration
        tv = new TextView(this);
        tv.setText("" + myDuration);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Checksum
        tv = new TextView(this);
        tv.setText("" + myCheckSum);
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Ring
        tv = new TextView(this);
        tv.setText("" + myRings);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Date & Time
        tv = new TextView(this);
        tv.setText("" + myDateTime);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Number
        tv = new TextView(this);
        tv.setText("" + myNumber);
        tv.setPadding(0,0,45,0);
        newRow.addView(tv);

        // Name
        tv = new TextView(this);
        tv.setText("" + myName);
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Add row to call log table
        tableCallLog.addView(newRow);

        // Auto-scroll to bottom
        svCallLog.post(new Runnable() {

            @Override
            public void run() {
                svCallLog.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

    }

    // ------------------------------------------------------------------------------------------- Actual POST to Cloud code
    private void PostToCloud(String urlFull, String line, String dateTime, String number, String name, String io,
                             String se, String status, String duration, String ring){

        Boolean secured = urlFull.contains("https://");

        // Post to Cloud using new thread
        Thread post = new Thread(new Poster(secured,urlFull,line,dateTime,number,name,io,se,status,duration,ring,
                tbLine.getText().toString(),tbTime.getText().toString(),tbPhone.getText().toString(),tbName.getText().toString(),
                tbIO.getText().toString(),tbSE.getText().toString(),tbStatus.getText().toString(),tbDuration.getText().toString(),tbRingNumber.getText().toString(),
                tbRingType.getText().toString(),ckbRequiresAuth.isChecked(),tbUsername.getText().toString(),tbPassword.getText().toString()));

        post.start();

    }

    // ------------------------------------------------------------------------------------------------ UDP LOWER Level Code

    // Link Display to Update so the UI gets updated through interface
    @Override
    public void getUDP(String rString){

        inString = rString;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                handleUDP(inString,isInFront);
            }
        });

    }

    private void handleUDP(String received, boolean isInFront){

        // Handle UDP
        // Setup variables for use
        String myData = received;

        Integer myLine = 0;
        String myType="";
        String myIndicator="";

        // Unused in this app but available for other custom apps
        String myDuration="";
        String myCheckSum="";
        String myRings="";
        //------------------------------------------------------

        String myDateTime="";
        String myNumber="";
        String myName="";

        // Check if matches a call record
        Pattern myPattern = Pattern.compile(".*(\\d\\d) ([IO]) ([ES]) (\\d{4}) ([GB]) (.)(\\d) (\\d\\d/\\d\\d \\d\\d:\\d\\d [AP]M) (.{8,15})(.*)");
        Matcher matcher = myPattern.matcher(myData);

        if(matcher.find()){

            myLine = Integer.parseInt(matcher.group(1));
            myType = matcher.group(2);

            if(myType.equals("I")||myType.equals("O")){

                myIndicator = matcher.group(3);

                // Unused in this app but available for other custom apps
                myDuration = matcher.group(4);
                myCheckSum = matcher.group(5);
                myRings = matcher.group(6);
                //------------------------------------------------------

                myDateTime = matcher.group(8);
                myNumber = matcher.group(9);
                myName = matcher.group(10);

                String theLineNumber = myLine.toString();
                if(theLineNumber.length() == 1) theLineNumber = "0" + theLineNumber;

                // Add to log
                addCallToLog(theLineNumber,myType,myIndicator,myDuration,myCheckSum,myRings,myDateTime,myNumber,myName);

            }

        }

        // Check to see if call information is from a DETAILED record
        Pattern myPatternDetailed = Pattern.compile(".*(\\d\\d) ([NFR]) {13}(\\d\\d/\\d\\d \\d\\d:\\d\\d:\\d\\d)");
        Matcher matcherDetailed = myPatternDetailed.matcher(myData);

        if(matcherDetailed.find()){

            myLine = Integer.parseInt(matcherDetailed.group(1));
            myType = matcherDetailed.group(2);

            if(myType.equals("N")||myType.equals("F")||myType.equals("R")){
                myDateTime = matcherDetailed.group(3);
            }

            String theLineNumber = myLine.toString();
            if(theLineNumber.length() == 1) theLineNumber = "0" + theLineNumber;

            // Add to log
            addCallToLog(theLineNumber,myType,"","","","",myDateTime,"","");

        }

    }


}
