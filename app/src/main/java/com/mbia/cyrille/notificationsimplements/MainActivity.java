package com.mbia.cyrille.notificationsimplements;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.fragment.app.FragmentTransaction;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    public static String id = "test_channel_01";
    fragment_my myFrag;
    String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            myFrag = new fragment_my();
            transaction.replace(R.id.container, myFrag);
            transaction.commit();
        }
        createchannel(); //configuration des canaux si nécessaire.
    }

    protected static final String ACTION_NOTIFICATION_DELETE = "com.mbia.cyrille.notificationsimplements.delete";
    public static final String READ_ACTION = "com.mbia.cyrille.notificationsimplements.ACTION_MESSAGE_READ";
    public static final String REPLY_ACTION = "com.mbia.cyrille.notificationsimplements.ACTION_MESSAGE_REPLY";
    public static final String CONVERSATION_ID = "conversation_id";
    public static final String EXTRA_REMOTE_REPLY = "extra_remote_reply";

    // pour les mises à jour lorsqu'une notification a été supprimée.
    private BroadcastReceiver mDeleteReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            myFrag.updateNumberOfNotifications();
        }
    };

    // Quand un message a été lu
    private BroadcastReceiver mReadReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            Log.d(TAG, "onReceiveRead");
            int conversationId = intent.getIntExtra(CONVERSATION_ID, -1);
            if (conversationId != -1){
                myFrag.NotificationRead(conversationId);
            }
        }
    };

    //Quand un message a été répondu.
    private BroadcastReceiver mReplyReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent){
            Log.d(TAG, "onReceiveReply");
            int conversationId = intent.getIntExtra(CONVERSATION_ID, -1);
            if (conversationId != -1){
                Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                if (remoteInput != null){
                    String replyMessage = remoteInput.getCharSequence(EXTRA_REMOTE_REPLY).toString();
                    Log.d(TAG, "Notification " + conversationId + " la réponse est " + replyMessage);

                    // Mettre à jour la notification pour arrêter la progression spinner.
                    NotificationManagerCompat notificationManager =
                            NotificationManagerCompat.from(context);
                    Notification repliedNotification = new NotificationCompat.Builder(context, id)
                            .setSmallIcon(R.mipmap.notification_icon)
                            .setLargeIcon(BitmapFactory.decodeResource(
                                    context.getResources(), R.mipmap.android_contact))
                            .setDeleteIntent(myFrag.mDeletePendingIntent)  //Nous savons donc s'ils l'ont supprimé.
                            .setContentText("Déjà répondu")
                            .setChannelId(MainActivity.id)
                            .setOnlyAlertOnce(true)  //ne sonne pas/ne vibre pas/ne s'allume plus!
                            .build();
                    notificationManager.notify(conversationId, repliedNotification);
                    myFrag.NotificationReply(conversationId, replyMessage);
                }
            }
        }
    };

    @Override
    protected void onStart(){
        super.onStart();
        registerReceiver(mDeleteReceiver, new IntentFilter(ACTION_NOTIFICATION_DELETE));
        registerReceiver(mReadReceiver, new IntentFilter(READ_ACTION));
        registerReceiver(mReplyReceiver, new IntentFilter(REPLY_ACTION));
    }

    @Override
    protected void onStop(){
        super.onStop();
        unregisterReceiver(mDeleteReceiver);
        unregisterReceiver(mReadReceiver);
        unregisterReceiver(mReplyReceiver);
    }

    /*
       * pour API 26+ créer des canaux de notification
     */
    private void createchannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel mChannel = new NotificationChannel(id,
                    getString(R.string.channel_name),  //nom de la chaîne
                    NotificationManager.IMPORTANCE_DEFAULT);   //niveau d'importance
            //niveau d'importance: la valeur par défaut est élevée sur le téléphone. haut est urgent au téléphone.
            // bas est moyen, alors aucun n'est bas?
            // Configure le canal de notification.
            mChannel.setDescription(getString(R.string.channel_description));
            mChannel.enableLights(true);
            // Définit la couleur du voyant de notification pour les notifications publiées sur ce canal,
            // si le périphérique prend en charge cette fonctionnalité.
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setShowBadge(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            nm.createNotificationChannel(mChannel);

        }
    }
}
