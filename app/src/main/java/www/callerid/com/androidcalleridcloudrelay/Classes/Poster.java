package www.callerid.com.androidcalleridcloudrelay.Classes;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Base64;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

import www.callerid.com.androidcalleridcloudrelay.actMain;


/*
        |  Poster.java  |
        ----------------------------------------------------------------------
        This Class is for posting to Cloud Server using it's own thread.
        This is required since Android does not allow Network on Main Thread.
        ----------------------------------------------------------------------
 */
public class Poster implements Runnable {

    Context context;

    Boolean isSecure;
    String postType;

    String urlFull;
    String line;
    String dateTime;
    String number;
    String name;
    String io;
    String se;
    String status;
    String duration;
    String ring;
    String username;
    String password;

    String userVarLine;
    String userVarDateTime;
    String userVarNumber;
    String userVarName;
    String userVarIO;
    String userVarSE;
    String userVarStatus;
    String userVarDuration;
    String userVarRingNumber;
    String userVarRingType;

    Boolean requireAuth;

    public Poster(Boolean pIsSecure,

                  String pUrlFull, String pLine, String pDateTime,
                  String pNumber, String pName, String pIo,
                  String pSe, String pStatus, String pDuration,
                  String pRing,

                  String pUserVarLine, String pUserVarDateTime,
                  String pUserVarNumber, String pUserVarName,
                  String pUserVarIO, String pUserVarSE,
                  String pUserVarStatus, String pUserVarDuration,
                  String pUserVarRingNumber, String pUserVarRingType,

                  Boolean pRequireAuth, String pUsername, String pPassword,
                  Context pContext, String pPostType)
    {

        context = pContext;

        isSecure = pIsSecure;
        postType = pPostType;

        urlFull = pUrlFull;
        line = pLine;
        dateTime = pDateTime;
        number = pNumber;
        name = pName;
        io = pIo;
        se = pSe;
        status = pStatus;
        duration = pDuration;
        ring = pRing;

        userVarLine = pUserVarLine;
        userVarDateTime = pUserVarDateTime;
        userVarNumber = pUserVarNumber;
        userVarName = pUserVarName;
        userVarIO = pUserVarIO;
        userVarSE = pUserVarSE;
        userVarStatus = pUserVarStatus;
        userVarDuration = pUserVarDuration;
        userVarRingNumber = pUserVarRingNumber;
        userVarRingType = pUserVarRingType;

        requireAuth = pRequireAuth;
        username = pUsername;
        password = pPassword;

    }

    private Handler mHandlerFinished = new Handler(){

        public void handleMessage(Message msg)
        {
            String title;
            String message;

            if(postType == "Supplied"){

                title = "Example Call Sent to Supplied URL";
                message = "An example Start of call record was sent to the Supplied URL.";

            }
            else if(postType == "Built"){

                title = "Example Call Sent to Built URL";
                message = "An example Start of call record was sent to your custom built URL.";

            }
            else{
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);

            // add a button
            builder.setPositiveButton("OK", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    private Handler mHandlerUrlError = new Handler()
    {
        public void handleMessage(Message msg)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Posting Error");
            builder.setMessage("Url not in correct format.");

            // add a button
            builder.setPositiveButton("OK", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    private Handler mHandlerUrlNotFoundError = new Handler()
    {
        public void handleMessage(Message msg)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("URL Error");
            builder.setMessage("Url not found.");

            // add a button
            builder.setPositiveButton("OK", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    private Handler mHandlerPostingError = new Handler()
    {
        public void handleMessage(Message msg)
        {
            // setup the alert builder
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Posting Error");
            builder.setMessage("Error posting to URL. Double check URL string.");

            // add a button
            builder.setPositiveButton("OK", null);

            // create and show the alert dialog
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    };

    public void run() {

        if(!urlFull.contains("?")){

            // setup the alert builder
            mHandlerUrlError.sendEmptyMessage(0);
            return;

        }

        String errors = "";

        try{

            Uri.Builder builder = new Uri.Builder();

            if(userVarLine.length()>0){
                builder.appendQueryParameter(userVarLine, line);
            }

            if(userVarDateTime.length()>0){
                builder.appendQueryParameter(userVarDateTime, dateTime);
            }

            if(userVarNumber.length()>0){
                builder.appendQueryParameter(userVarNumber, number);
            }

            if(userVarName.length()>0){
                builder.appendQueryParameter(userVarName, name);
            }

            if(userVarIO.length()>0){
                builder.appendQueryParameter(userVarIO, io);
            }

            if(userVarSE.length()>0){
                builder.appendQueryParameter(userVarSE, se);
            }

            if(userVarStatus.length()>0){
                builder.appendQueryParameter(userVarStatus, status);
            }

            if(userVarDuration.length()>0) {
                builder.appendQueryParameter(userVarDuration, duration);
            }

            if(userVarRingType.length()>1){
                if(ring.length()>0){
                    builder.appendQueryParameter(userVarRingType, ring.substring(0,1));
                }
            }

            if(userVarRingNumber.length()>1){
                if(ring.length()>1){
                    builder.appendQueryParameter(userVarRingNumber, ring.substring(1,2));
                }
            }

            String query = builder.build().getEncodedQuery();

            String[] urlParts = urlFull.split("\\?");
            urlFull = urlParts[0] + "?" + query;

            URL url = new URL(urlFull);

            String authentication = username+":"+password;
            String encodedAuthentication = Base64.encodeToString(authentication.getBytes(), Base64.NO_WRAP);

            if(isSecure){

                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

                if(requireAuth){
                    conn.setRequestProperty("Authorization", "Basic " + encodedAuthentication);
                }

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                String response = conn.getResponseMessage();
                System.out.println(response);
                errors+=response;

            }
            else{

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if(requireAuth){
                    conn.setRequestProperty("Authorization", "Basic " + encodedAuthentication);
                }

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setInstanceFollowRedirects( false );
                conn.setRequestMethod( "POST" );
                conn.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();

                conn.connect();

                String response = conn.getResponseMessage();
                System.out.println(response);
                errors+=response;

            }

        }catch (Exception ex){

            mHandlerPostingError.sendEmptyMessage(0);
            return;

        }

        // Finished message for postType == supplied or built
        if(errors.contains("Not Found")){

            mHandlerUrlNotFoundError.sendEmptyMessage(0);
            return;

        }

        mHandlerFinished.sendEmptyMessage(0);

    }

}
