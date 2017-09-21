package www.callerid.com.androidcalleridcloudrelay;

import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TableLayout;
import android.widget.TextView;

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

    private TableLayout tableLog;

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

        tableLog = (TableLayout)findViewById(R.id.tableLog);

        btnClearLog = (Button)findViewById(R.id.btnClearLog);

    }
}
