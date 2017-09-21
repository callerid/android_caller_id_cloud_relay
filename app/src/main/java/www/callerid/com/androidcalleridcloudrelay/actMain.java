package www.callerid.com.androidcalleridcloudrelay;

import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import www.callerid.com.androidcalleridcloudrelay.Classes.Poster;

public class actMain extends AppCompatActivity {

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

        // Add event handlers
        // TODO

    }

    private void LinkAllUIControls(){

        ckbRequiresAuth = (CheckBox) findViewById(R.id.ckbRequiresAuth);
        tbUsername = (TextView) findViewById(R.id.tbUsername);
        tbPassword = (TextView) findViewById(R.id.tbPassword);

        rbUseSuppliedURL = (RadioButton) findViewById(R.id.rbUseSuppliedURL);
        rbUseBuiltURL = (RadioButton)findViewById(R.id.rbUseBuiltURL);
        rbDeluxeUnit = (RadioButton)findViewById(R.id.rbDeluxeUnit);
        rbBasicUnit = (RadioButton)findViewById(R.id.rbBasicUnit);
        btnTestURL = (Button)findViewById(R.id.btnTestURL);

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
        String line = "" + myLine;
        if(line.length()==1){
            line = "0" + line;
        }
        tv.setText(line.trim());
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

}
