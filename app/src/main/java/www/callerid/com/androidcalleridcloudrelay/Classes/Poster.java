package www.callerid.com.androidcalleridcloudrelay.Classes;

import android.net.Uri;
import android.util.Base64;
import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;


/*
        |  Poster.java  |
        ----------------------------------------------------------------------
        This Class is for posting to Cloud Server using it's own thread.
        This is required since Android does not allow Network on Main Thread.
        ----------------------------------------------------------------------
 */
public class Poster implements Runnable {

    Boolean isSecure;

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

                  Boolean pRequireAuth, String pUsername, String pPassword)
    {

        isSecure = pIsSecure;

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
    public void run() {

        if(!urlFull.contains("?")){

            // TODO -- cannot post, show popup

        }

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
                if(ring.length()>1){
                    builder.appendQueryParameter(userVarRingType, ring.substring(0,1));
                }
            }

            if(userVarRingNumber.length()>1){
                if(ring.length()>0){
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

            }

        }catch (Exception ex){

            // TODO -- there was a problem posting

            System.out.println("Exception: " + ex.toString());

        }

    }

}
