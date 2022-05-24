package com.cpe521.mqtttest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String USERNAME = "mulemq";
    private static final String PASSWORD = "!Y2df@35836";
    private static final String CONNECTION_URL = "tcp://192.168.3.150:1883";
    private static final String TOPIC = "soundnotif";
    private static final String MESSAGE_INFO = "Warning! Social Distancing Violators Detected.";
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;


    private static final String TAG = "MainActivity";
    private String topic;
    private MqttAndroidClient client;
    private MqttConnectOptions connOpts = setUpConnectionOptions(USERNAME, PASSWORD);

    private String clientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){

        clientId = MqttClient.generateClientId();
        topic = TOPIC;
        client = new MqttAndroidClient(
                this.getApplicationContext(),
                CONNECTION_URL,
                clientId
        );

        connectMQTT();

    }

    private void connectMQTT(){
        try {
            IMqttToken token = client.connect(connOpts);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "onSuccess");
                    subscribeMQTT();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private static MqttConnectOptions setUpConnectionOptions(String username, String password) {
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(username);
        connOpts.setPassword(password.toCharArray());
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);

        return connOpts;
    }

    private void subscribeMQTT(){

        try{
            client.subscribe(topic,0);

            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(TAG, "cause: " + cause );

                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {

                    String data = new String(message.getPayload());

                    Log.d(TAG, "topic: " + topic );
                    Log.d(TAG, "topic: " + data );
                    JSONObject dataObj = new JSONObject(data);
                    notificationProcess( dataObj.get("channel").toString());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {

                }
            });
        }catch(MqttException e){

        }

    }

    private void notificationProcess(String channel ){

        //Uri sound = Uri. parse (ContentResolver.SCHEME_ANDROID_RESOURCE + File.pathSeparator + File.separator + File.separator + getPackageName() + "/raw/" + R.raw.sound) ;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(MainActivity. this, default_notification_channel_id )
                .setSmallIcon(R.drawable. ic_launcher_foreground )
                .setContentTitle( "Channel " + channel )
          //      .setSound(sound)
                .setContentText( MESSAGE_INFO );

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context. NOTIFICATION_SERVICE );
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes. CONTENT_TYPE_SONIFICATION )
                    .setUsage(AudioAttributes. USAGE_ALARM )
                    .build() ;
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new
                    NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            notificationChannel.enableLights( true ) ;
            notificationChannel.setLightColor(Color. RED ) ;
            notificationChannel.enableVibration( true ) ;
            notificationChannel.setVibrationPattern( new long []{ 100 , 200 , 300 , 400 , 500 , 400 , 300 , 200 , 400 }) ;
            //notificationChannel.setSound(sound , audioAttributes) ;
            mBuilder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
            assert mNotificationManager != null;
            mNotificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert mNotificationManager != null;
        mNotificationManager.notify(( int ) System. currentTimeMillis () ,
                mBuilder.build()) ;


    }
}