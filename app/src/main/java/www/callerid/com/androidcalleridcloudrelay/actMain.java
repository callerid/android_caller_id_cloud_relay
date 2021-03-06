package www.callerid.com.androidcalleridcloudrelay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import www.callerid.com.androidcalleridcloudrelay.Classes.Poster;
import www.callerid.com.androidcalleridcloudrelay.Classes.ServiceCallbacks;
import www.callerid.com.androidcalleridcloudrelay.Classes.UDPListen;

public class actMain extends Activity implements ServiceCallbacks {

    // Debug-----------
    private int CallLogEntries = 0;
    private boolean debug = false;
    //-----------------

    // UDP listen requirements
    private Map<String, Integer> previousReceptions;
    private UDPListen mService;
    private boolean mBound = false;
    private String inString = "Waiting for Calls.";

    // Database
    static public SQLiteDatabase myDatabase;

    // Popup builder
    AlertDialog.Builder dlgAlert;

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
    private NestedScrollView svCallLog;

    private Button btnClearLog;

    // ---------------------------------------------------------------------------------------------------- Activity Functions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_main);

        // Database startup
        startupDatabase();

        // Link UI variables
        LinkAllUIControls();

        // Prepare popup messenger
        dlgAlert  = new AlertDialog.Builder(this);

        // Load database
        loadUpLog();

        // Load all settings variables
        restoreSettings();

        // Attach event handlers
        ckbRequiresAuth.setOnClickListener(ckbRequiresAuth_Click);
        rbUseBuiltURL.setOnClickListener(rbChangeToBuilt);
        rbUseSuppliedURL.setOnClickListener(rbChangeToSupplied);
        rbDeluxeUnit.setOnClickListener(rbChangeToDeluxe);
        rbBasicUnit.setOnClickListener(rbChangeToBasic);
        btnGenerateURL.setOnClickListener(btnGenerateURL_Click);
        btnPaste.setOnClickListener(btnPaste_Click);
        btnTestURL.setOnClickListener(btnTestURL_Click);
        btnCopyGenURL.setOnClickListener(btnCopyGenURL_Click);
        btnClearLog.setOnClickListener(btnClearLog_Click);

        // Update UI based on restored settings values
        ckbRequiresAuth_Click.onClick(new View(this));
        changeURL(rbUseSuppliedURL.isChecked());
        changeUnit(rbDeluxeUnit.isChecked());


        // Duplicates
        previousReceptions = new HashMap<String, Integer>();

        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {

            @Override
            public void run() {
                actMain.this.runOnUiThread(new Runnable() {
                    public void run() {
                        previousReceptions_timer_tick();
                    }
                });
            }
        },0, 1000);

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

    @Override
    protected void onStop(){
        super.onStop();

        // Save user prefs
        saveSettings();

    }

    // ------------------------------------------------------------------------------------------------------ Popup Functions

    public void popupMessage(String message,String title){
        dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //dismiss the dialog
                    }
                });
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    // --------------------------------------------------------------------------------------------------- Database Functions

    private void startupDatabase(){

        myDatabase = openOrCreateDatabase("cloud_relay_db", MODE_PRIVATE, null);

        // Create tables with needed formats
        String creationQuery = "CREATE TABLE IF NOT EXISTS callLog (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                "datetime TEXT," +
                "line TEXT," +
                "io TEXT," +
                "se TEXT," +
                "checksum TEXT," +
                "duration TEXT," +
                "ring TEXT," +
                "number TEXT," +
                "name TEXT" +
                ");";

        myDatabase.execSQL(creationQuery);

    }

    private void loadUpLog(){

        Cursor results = myDatabase.rawQuery("SELECT * FROM callLog ORDER BY id ASC LIMIT 100;",null);

        if(results.getCount()<1) {
            return;
        }

        while(results.moveToNext()){

            String line = results.getString(results.getColumnIndex("line"));
            String datetime = results.getString(results.getColumnIndex("datetime"));
            String phone = results.getString(results.getColumnIndex("number"));
            String name = results.getString(results.getColumnIndex("name"));
            String io = results.getString(results.getColumnIndex("io"));
            String cksum = results.getString(results.getColumnIndex("checksum"));
            String se = results.getString(results.getColumnIndex("se"));
            String status = results.getString(results.getColumnIndex("io"));
            String duration = results.getString(results.getColumnIndex("duration"));
            String ring = results.getString(results.getColumnIndex("ring"));

            Boolean isDetailed = (io.equals("R") || io.equals("F") || io.equals("N"));

            if(isDetailed){
                addCallToLog(line,status,"","","","",datetime,"","");
            }
            else{
                addCallToLog(line,io,se,duration,cksum,ring,datetime,phone,name);
            }
        }
    }

    private void insertIntoDatabase(String myLine,String myType,
                                    String myIndicator,String myDuration,
                                    String myCheckSum,String myRings,
                                    String myDateTime,String myNumber,
                                    String myName){

        String insertQuery = "INSERT INTO callLog (" +

                "line," +
                "io," +
                "se," +
                "checksum," +
                "duration," +
                "ring," +
                "datetime," +
                "number," +
                "name " +

                ") VALUES (" +

                "'" + myLine + "'," +
                "'" + myType + "'," +
                "'" + myIndicator + "'," +
                "'" + myCheckSum + "'," +
                "'" + myDuration + "'," +
                "'" + myRings + "'," +
                "'" + myDateTime + "'," +
                "'" + myNumber + "'," +
                "'" + myName + "'" +

                ");";

        myDatabase.execSQL(insertQuery);

    }

    private void clearDatabase(){

        myDatabase.execSQL("DELETE FROM callLog;");

    }

    // ---------------------------------------------------------------------------------------------------- Setting Variables

    // Setting vars
    public static final String PREFS_NAME = "Cloud_Relay_Saved_Prefs";

    private String savedRequireAuth = "spRequiresAuth";
    private String savedUserName = "spUsername";
    private String savedPassword = "spPassword";

    private String savedIsSupplied = "spIsSupplied";
    private String savedIsDeluxe = "spIsDeluxe";

    private String savedBuiltURL = "spBuiltURL";
    private String savedSuppliedURL = "spSuppliedURL";
    private String savedServer = "spServer";

    private String savedParam_Line = "spParamLine";
    private String savedParam_Time = "spParamTime";
    private String savedParam_Phone = "spParamPhone";
    private String savedParam_Name = "spParamName";
    private String savedParam_IO = "spParamIO";
    private String savedParam_SE = "spParamSE";
    private String savedParam_Status = "spParamStatus";
    private String savedParam_Duration = "spParamDuration";
    private String savedParam_RingNumber = "spParamRingNumber";
    private String savedParam_RingType = "spParamRingType";

    // ---------------------------------------------------------------------------------------------------- Setting Functions

    private void saveSettings(){

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putBoolean(savedRequireAuth, ckbRequiresAuth.isChecked());
        editor.putString(savedUserName, tbUsername.getText().toString());
        editor.putString(savedPassword,tbPassword.getText().toString());

        editor.putBoolean(savedIsSupplied,rbUseSuppliedURL.isChecked());
        editor.putBoolean(savedIsDeluxe,rbDeluxeUnit.isChecked());

        editor.putString(savedSuppliedURL,tbSuppliedURL.getText().toString());
        editor.putString(savedBuiltURL,lbGeneratedURL.getText().toString());
        editor.putString(savedServer,tbServer.getText().toString());
        editor.putString(savedParam_Line,tbLine.getText().toString());
        editor.putString(savedParam_Time,tbTime.getText().toString());
        editor.putString(savedParam_Phone,tbPhone.getText().toString());
        editor.putString(savedParam_Name,tbName.getText().toString());
        editor.putString(savedParam_IO,tbIO.getText().toString());
        editor.putString(savedParam_SE,tbSE.getText().toString());
        editor.putString(savedParam_Status,tbStatus.getText().toString());
        editor.putString(savedParam_Duration,tbDuration.getText().toString());
        editor.putString(savedParam_RingNumber,tbRingNumber.getText().toString());
        editor.putString(savedParam_RingType,tbRingType.getText().toString());

        // Commit the edits!
        editor.commit();
    }

    private void restoreSettings(){

        // Restore preferences
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        // Auth
        ckbRequiresAuth.setChecked(settings.getBoolean(savedRequireAuth,true));
        tbUsername.setText(settings.getString(savedUserName,""));
        tbPassword.setText(settings.getString(savedPassword,""));

        // Unit Type
        rbDeluxeUnit.setChecked(settings.getBoolean(savedIsDeluxe,false));
        rbBasicUnit.setChecked(!settings.getBoolean(savedIsDeluxe,false));

        // Supplied
        rbUseSuppliedURL.setChecked(settings.getBoolean(savedIsSupplied,true));
        rbUseBuiltURL.setChecked(!settings.getBoolean(savedIsSupplied,true));
        tbSuppliedURL.setText(settings.getString(savedSuppliedURL,""));

        // Dev Section
        lbGeneratedURL.setText(settings.getString(savedBuiltURL,""));
        tbServer.setText(settings.getString(savedServer,""));

        // Params
        tbLine.setText(settings.getString(savedParam_Line,""));
        tbTime.setText(settings.getString(savedParam_Time,""));
        tbPhone.setText(settings.getString(savedParam_Phone,""));
        tbName.setText(settings.getString(savedParam_Name,""));
        tbIO.setText(settings.getString(savedParam_IO,""));
        tbSE.setText(settings.getString(savedParam_SE,""));
        tbStatus.setText(settings.getString(savedParam_Status,""));
        tbDuration.setText(settings.getString(savedParam_Duration,""));
        tbRingNumber.setText(settings.getString(savedParam_RingNumber,""));
        tbRingType.setText(settings.getString(savedParam_RingType,""));

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
        svCallLog = (NestedScrollView) findViewById(R.id.svCallLog);

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

    private View.OnClickListener btnGenerateURL_Click = new View.OnClickListener() {
        public void onClick(View v) {
            generateURL();
        }
    };

    private View.OnClickListener btnPaste_Click = new View.OnClickListener() {
        public void onClick(View v) {
            pasteUrl();
        }
    };

    private View.OnClickListener btnTestURL_Click = new View.OnClickListener() {
        public void onClick(View v) {
            testCall();
        }
    };

    private View.OnClickListener btnCopyGenURL_Click = new View.OnClickListener() {
        public void onClick(View v) {
            copyUrl();
        }
    };

    private View.OnClickListener btnClearLog_Click = new View.OnClickListener() {
        public void onClick(View v) {
            // Clear database and display changes
            clearDatabase();
            tableCallLog.removeAllViews();
        }
    };

    // --------------------------------------------------------------------------------------------------------- UI Updating

    private void changeURL(boolean isSupplied){

        if(isSupplied){

            rbUseSuppliedURL.setChecked(true);
            rbUseBuiltURL.setChecked(false);
            toggleDevSection(false);
            rbDeluxeUnit.setEnabled(false);
            rbBasicUnit.setEnabled(false);

        }
        else{

            rbUseSuppliedURL.setChecked(false);
            rbUseBuiltURL.setChecked(true);
            toggleDevSection(true);
            rbDeluxeUnit.setEnabled(true);
            rbBasicUnit.setEnabled(true);

        }

    }

    private void toggleDevSection(boolean isSupplied){

        if(!isSupplied){

            lbSuppliedURL.setTextColor(Color.BLACK);
            tbSuppliedURL.setEnabled(true);
            String testBtnText = "Test Supplied URL";
            btnTestURL.setText(testBtnText);

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

            toggleDeluxe(rbDeluxeUnit.isChecked());

        }
        else{

            lbSuppliedURL.setTextColor(Color.GRAY);
            tbSuppliedURL.setEnabled(false);
            String testBtnText = "Test Built URL";
            btnTestURL.setText(testBtnText);

            if(!lbGeneratedURL.getText().toString().contains("http")){
                lbGeneratedURL.setTextColor(Color.RED);
            }
            else{
                lbGeneratedURL.setTextColor(Color.rgb(23,115,20));
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

        if(rbUseSuppliedURL.isChecked()) return;

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

    private void updateCallLogEntries(){

        if(debug){
            CallLogEntries +=1;
            tbLine.setText("Packets Rec: " + CallLogEntries);
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
        tv.setText(myLine);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // I/O
        tv = new TextView(this);
        tv.setText(myType);
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Start/End
        tv = new TextView(this);
        tv.setText(myIndicator);
        tv.setPadding(0,0,20,0);
        newRow.addView(tv);

        // Duration
        tv = new TextView(this);
        tv.setText(myDuration);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Checksum
        tv = new TextView(this);
        tv.setText(myCheckSum);
        tv.setPadding(0,0,15,0);
        newRow.addView(tv);

        // Ring
        tv = new TextView(this);
        tv.setText(myRings);
        tv.setPadding(0,0,25,0);
        newRow.addView(tv);

        // Date & Time
        tv = new TextView(this);
        tv.setText(myDateTime);
        tv.setPadding(0,0,18,0);
        newRow.addView(tv);

        // Number
        tv = new TextView(this);
        tv.setText(myNumber);
        tv.setPadding(0,0,45,0);
        newRow.addView(tv);

        // Name
        tv = new TextView(this);
        tv.setText(myName);
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

    private void postToCloud(String urlFull, String line, String dateTime, String number, String name, String io,
                             String se, String status, String duration, String ring, String postType){

        Boolean secured = urlFull.contains("https://");

        // Post to Cloud using new thread
        Thread post = new Thread(new Poster(secured,urlFull,line,dateTime,number,name,io,se,status,duration,ring,
                tbLine.getText().toString(),tbTime.getText().toString(),tbPhone.getText().toString(),tbName.getText().toString(),
                tbIO.getText().toString(),tbSE.getText().toString(),tbStatus.getText().toString(),tbDuration.getText().toString(),tbRingNumber.getText().toString(),
                tbRingType.getText().toString(),ckbRequiresAuth.isChecked(),tbUsername.getText().toString(),tbPassword.getText().toString(), this, postType));

        post.start();

    }

    private void testCall(){

        String url = rbUseSuppliedURL.isChecked() ? tbSuppliedURL.getText().toString() : lbGeneratedURL.getText().toString();

        Calendar calendar = Calendar.getInstance();

        int pm = calendar.get(Calendar.AM_PM);

        String am_pm = "AM";
        if(pm==1) am_pm = "PM";

        String month = String.valueOf(calendar.get(Calendar.MONTH));
        if(month.length()==1)month="0"+month;

        String day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if(day.length()==1)day="0"+day;

        String hour = String.valueOf(calendar.get(Calendar.HOUR));
        if(hour.length()==1)hour="0"+hour;

        String minute = String.valueOf(calendar.get(Calendar.MINUTE));
        if(minute.length()==1)minute="0"+minute;

        String date_string = "" + month + "/" +
                day + " " +
                hour + ":" +
                minute + " " +
                am_pm;

        postToCloud(url, "01", date_string, "770-263-7111", "CallerID.com", "I", "S", "n/a", "0000", "A0", (rbUseSuppliedURL.isChecked()?"Supplied":"Built") );
    }

    // ------------------------------------------------------------------------------- Regex Patterns for Parsing Pasted URL

    // Patterns for parsing pasted URL
    Pattern linePattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%Line)");
    Pattern ioPattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%IO)");
    Pattern sePattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%SE)");
    Pattern durationPattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%Duration)");
    Pattern ringTypePattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%RingType)");
    Pattern ringNumberPattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%RingNumber)");
    Pattern timePattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%Time)");
    Pattern phonePattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%Phone)");
    Pattern namePattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%Name)");
    Pattern statusPattern = Pattern.compile("([&]?([A-Za-z0-9_-]+)=%Status)");

    // ---------------------------------------------------------------------------------- Parsing and Building URL functions

    private void pasteUrl(){

        ClipboardManager clipboard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = clipboard.getPrimaryClip();

        if(clipData == null){

            popupMessage("Clipboard does not contain correct format or any text.","Cannot Paste");
            return;
        }

        ClipData.Item item = clipData.getItemAt(0);
        String fullURL = item.getText().toString();

        if (!fullURL.contains("?"))
        {
            popupMessage("The text on clipboard does not contain a '?' which is required.","Incorrect format.");
            return;
        }

        String[] urlParts = fullURL.split("\\?");

        String allParams = urlParts[1];

        if (allParams.isEmpty())
        {
            popupMessage("The text on clipboard does not contain text after '?'.","No Parameters Found.");
            return;
        }

        int parameters = 0;

        Matcher m = linePattern.matcher(allParams);
        if (m.find()) {
            tbLine.setText(m.group(2));
            parameters++;
        }

        m = timePattern.matcher(allParams);
        if (m.find()) {
            tbTime.setText(m.group(2));
            parameters++;
        }

        m = phonePattern.matcher(allParams);
        if (m.find()) {
            tbPhone.setText(m.group(2));
            parameters++;
        }

        m = namePattern.matcher(allParams);
        if (m.find()) {
            tbName.setText(m.group(2));
            parameters++;
        }

        m = ioPattern.matcher(allParams);
        if (m.find()) {
            tbIO.setText(m.group(2));
            parameters++;
        }

        m = sePattern.matcher(allParams);
        if (m.find()) {
            tbSE.setText(m.group(2));
            parameters++;
        }

        m = statusPattern.matcher(allParams);
        if (m.find()) {
            tbStatus.setText(m.group(2));
            parameters++;
        }

        m = durationPattern.matcher(allParams);
        if (m.find()) {
            tbDuration.setText(m.group(2));
            parameters++;
        }

        m = ringNumberPattern.matcher(allParams);
        if (m.find()) {
            tbRingNumber.setText(m.group(2));
            parameters++;
        }

        m = ringTypePattern.matcher(allParams);
        if (m.find()) {
            tbRingType.setText(m.group(2));
            parameters++;
        }

        if (parameters < 1)
        {
            popupMessage("Supplied URL populated.","Paste Complete");

        }
        else
        {
            popupMessage("Supplied URL and developer's section populated.","Paste Complete.");

            tbSuppliedURL.setText(fullURL);
        }
    }

    private void generateURL(){

        StringBuilder genUrl = new StringBuilder();

        if(tbServer.getText().toString().isEmpty()){

            String failed = "[ previous generation failed - fill out server ]";
            lbGeneratedURL.setText(failed);

            tbServer.setHighlightColor(Color.RED);

            popupMessage("Please input your Cloud Server.","Server Cannot be blank.");

            return;

        }

        tbServer.setHighlightColor(Color.WHITE);
        tbServer.setTextColor(Color.BLACK);

        String urlStart = tbServer.getText().toString() + "?";
        genUrl.append(urlStart);

        int parameters = 0;

        // Line
        if (!tbLine.getText().toString().isEmpty())
        {
            parameters++;
            String combo = tbLine.getText().toString() + "=%Line&";
            genUrl.append(combo);
        }

        // Time
        if (!tbTime.getText().toString().isEmpty())
        {
            parameters++;
            String combo = tbTime.getText().toString() + "=%Time&";
            genUrl.append(combo);
        }

        // Phone
        if (!tbPhone.getText().toString().isEmpty())
        {
            parameters++;
            String combo = tbPhone.getText().toString() + "=%Phone&";
            genUrl.append(combo);
        }

        // Name
        if (!tbName.getText().toString().isEmpty())
        {
            parameters++;
            String combo = tbName.getText().toString() + "=%Name&";
            genUrl.append(combo);
        }

        if(rbDeluxeUnit.isChecked()){

            // IO
            if (!tbIO.getText().toString().isEmpty())
            {
                parameters++;
                String combo = tbIO.getText().toString() + "=%IO&";
                genUrl.append(combo);
            }

            // SE
            if (!tbSE.getText().toString().isEmpty())
            {
                parameters++;
                String combo = tbSE.getText().toString() + "=%SE&";
                genUrl.append(combo);
            }

            // Status
            if (!tbStatus.getText().toString().isEmpty())
            {
                parameters++;
                String combo = tbStatus.getText().toString() + "=%Status&";
                genUrl.append(combo);
            }

            // Duration
            if (!tbDuration.getText().toString().isEmpty())
            {
                parameters++;
                String combo = tbDuration.getText().toString() + "=%Duration&";
                genUrl.append(combo);
            }

            // RingNumber
            if (!tbRingType.getText().toString().isEmpty())
            {
                parameters++;
                String combo = tbRingNumber.getText().toString() + "=%RingNumber&";
                genUrl.append(combo);
            }

            // RingType
            if (!tbRingType.getText().toString().isEmpty())
            {
                parameters++;
                String combo = tbRingType.getText().toString() + "=%RingType&";
                genUrl.append(combo);
            }

        }

        if (parameters == 0)
        {
            lbGeneratedURL.setTextColor(Color.RED);
            String failed = "[previous generation failed - use at least one parameter]";
            lbGeneratedURL.setText(failed);

            popupMessage("You must use at least one parameter.","No Parameters Set.");

            return;
        }

        lbGeneratedURL.setTextColor(Color.rgb(23,115,20));
        lbGeneratedURL.setText(genUrl.toString().substring(0,genUrl.toString().length()-1));

    }

    private void copyUrl(){

        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", lbGeneratedURL.getText().toString());
        clipboard.setPrimaryClip(clip);

    }

    // ------------------------------------------------------------------------------------------------ UDP LOWER Level Code

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

    private void previousReceptions_timer_tick(){

        if(previousReceptions.size()<1)return;

        ArrayList<String> keysToRemove = new ArrayList<>();
        ArrayList<String> keysToIncrement = new ArrayList<>();

        for(String key : previousReceptions.keySet()){

            if(previousReceptions.get(key) > 4){
                keysToRemove.add(key);
            }
            else {
                keysToIncrement.add(key);
            }
        }

        for(String key : keysToIncrement){
            previousReceptions.put(key,previousReceptions.get(key)+1);
        }

        for(String key : keysToRemove){
            previousReceptions.remove(key);
        }

    }

    private void handleUDP(String received, boolean isInFront){


        // Code to ignore duplicates
        if(previousReceptions.containsKey(received)) {
            // If duplicate, ignore
            return;
        }
        else{
            // If not duplicate add to check buffer
            if(previousReceptions.size()>30) {
                // If check buffer is full, add one to the end and remove oldest
                previousReceptions.put(received,0);
                previousReceptions.remove(0);
            }
            else{
                // If check buffer not full, simply add to end
                previousReceptions.put(received,0);
            }
        }


        // Handle UDP
        // Setup variables for use
        String myData = received;

        String myLine = "";
        String myType = "";
        String myIndicator = "";
        String myDuration = "";
        String myCheckSum = "";
        String myRings = "";
        String myDateTime = "";
        String myNumber = "";
        String myName = "";

        // Check if matches a call record
        Pattern myPattern = Pattern.compile(".*(\\d\\d) ([IO]) ([ES]) (\\d{4}) ([GB]) (.)(\\d) (\\d\\d/\\d\\d \\d\\d:\\d\\d [AP]M) (.{8,15})(.*)");
        Matcher matcher = myPattern.matcher(myData);

        // Check to see if call information is from a DETAILED record
        Pattern myPatternDetailed = Pattern.compile(".*(\\d\\d) ([NFR]) {13}(\\d\\d/\\d\\d \\d\\d:\\d\\d:\\d\\d)");
        Matcher matcherDetailed = myPatternDetailed.matcher(myData);

        boolean isCallRecord = matcher.find();
        boolean isDetailedRecord = matcherDetailed.find();

        if(debug){
            if(!matcher.find() && !matcherDetailed.find()){
                updateCallLogEntries();
                return;
            }
        }

        if(isCallRecord){

            myLine = matcher.group(1);
            myType = matcher.group(2);

            if(myType.equals("I")||myType.equals("O")){

                myIndicator = matcher.group(3);

                // Unused in this app but available for other custom apps
                myDuration = matcher.group(4);
                myCheckSum = matcher.group(5);
                myRings = matcher.group(6) + matcher.group(7);
                //------------------------------------------------------

                myDateTime = matcher.group(8);
                myNumber = matcher.group(9);
                myName = matcher.group(10);

                String theLineNumber = myLine;
                if(theLineNumber.length() == 1) theLineNumber = "0" + theLineNumber;

                // Add to log
                addCallToLog(theLineNumber,myType,myIndicator,myDuration,myCheckSum,myRings,myDateTime,myNumber,myName);

                // Insert to database
                insertIntoDatabase(theLineNumber,myType,myIndicator,myDuration,myCheckSum,myRings,myDateTime,myNumber,myName);

            }

        }

        boolean isDetailed = false;

        if(isDetailedRecord){

            myLine = matcherDetailed.group(1);
            myType = matcherDetailed.group(2);

            if(myType.equals("N")||myType.equals("F")||myType.equals("R")){
                myDateTime = matcherDetailed.group(3);
            }

            String theLineNumber = myLine;
            if(theLineNumber.length() == 1) theLineNumber = "0" + theLineNumber;

            // Add to log
            addCallToLog(theLineNumber,myType,"","","","",myDateTime,"","");

            // Insert to database
            insertIntoDatabase(theLineNumber,myType,"","","","",myDateTime,"","");

            isDetailed = true;

        }

        if(myLine.length() == 1){
            myLine = "0" + myLine;
        }

        // POST TO CLOUD
        String url = rbUseSuppliedURL.isChecked() ? tbSuppliedURL.getText().toString() : lbGeneratedURL.getText().toString();

        if (rbBasicUnit.isChecked())
        {
            if (myIndicator.equals("S") && !isDetailed)
            {
                postToCloud(url,myLine,myDateTime,myNumber,myName,myType,myIndicator,"",myDuration,myRings, "Post");
            }
        }
        else
        {
            if (isDetailed)
            {
                if (!tbStatus.getText().toString().isEmpty())
                {
                    postToCloud(url,myLine,myDateTime,"","","","",myType,"","", "Post");
                }
            }
            else
            {
                postToCloud(url,myLine,myDateTime,myNumber,myName,myType,myIndicator,"",myDuration,myRings, "Post");
            }
        }

    }


}
