package com.mbia.cyrille.notificationsimplements;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class fragment_my extends Fragment {
    private NotificationManager mNotificationManager;
    private NotificationManagerCompat mNotificationManagerCompat;

    String TAG = "myFrag";

    public PendingIntent mDeletePendingIntent;
    private static final int REQUEST_CODE = 2323;

    TextView mNumberOfNotifications, logger;

    int NotificationNum = 1;


    public fragment_my() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_my, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateNumberOfNotifications();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // pour le nombre de notifications actives
        mNotificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);

        // pour envoyer des notifications et tout le reste.
        mNotificationManagerCompat = NotificationManagerCompat.from(getActivity().getApplicationContext());

        // Créer un PendingIntent à déclencher lors de la suppression d'une notification.
        Intent deleteIntent = new Intent(MainActivity.ACTION_NOTIFICATION_DELETE);
        mDeletePendingIntent = PendingIntent.getBroadcast(getActivity(),
                REQUEST_CODE, deleteIntent, 0);

        mNumberOfNotifications = view.findViewById(R.id.numNoti);

        logger = view.findViewById(R.id.logger);

        // Fournit des actions au bouton affiché à l'écran.
        view.findViewById(R.id.addbutton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                createNotification();
            }
        });
    }

    /**
     * Demande le nombre actuel de notifications du {@link NotificationManager} et
     * les afficher à l'utilisateur.
     */
     protected void updateNumberOfNotifications(){
         // obtient la longueur, mais getActiveNotifications () renvoie un tableau des notifications,
         // dans lequel vous pouvez trouver des informations sur les notifications.
         int numberOfNotifications = mNotificationManager.getActiveNotifications().length;
         mNumberOfNotifications.setText("Le nombre de notifications actives est: " + numberOfNotifications);
         Log.i(TAG, "Le nombre de notifications actives est: " + numberOfNotifications);
     }


    // Crée une intention qui sera déclenchée lorsqu'un message est marqué comme lu.

    private Intent getMessageReadIntent(int id){
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(MainActivity.READ_ACTION)
                .putExtra(MainActivity.CONVERSATION_ID, id);
    }

    // Crée une intention qui sera déclenchée lors de la réception d'une réponse vocale.
    private Intent getMessageReplyIntent(int conversationId) {
        return new Intent()
                .addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                .setAction(MainActivity.REPLY_ACTION)
                .putExtra(MainActivity.CONVERSATION_ID, conversationId);
    }

    void createNotification(){
        // Une intention en attente pour les lectures
        PendingIntent readPendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                NotificationNum,
                getMessageReadIntent(NotificationNum),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Une liste de choix.
        String[] choices = new String[]{"Non", "Oui", "Peut être", "Faire disparaître"};

        // Construire un RemoteInput pour recevoir une entrée vocale dans une notification de voiture ou une entrée de texte sur
        // appareils prenant en charge la saisie de texte (comme les appareils sous Android N et versions ultérieures).

        RemoteInput remoteInput = new RemoteInput.Builder(MainActivity.EXTRA_REMOTE_REPLY)
                .setLabel("Répondre")
                .setChoices(choices)
                .build();

        // Construction d'une intention en attente pour l'action de réponse à déclencher
        PendingIntent replyIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(),
                NotificationNum,
                getMessageReplyIntent(NotificationNum),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Création d'une action compatible avec l'entrée Android compatible avec Android N.
        NotificationCompat.Action actionReplyByRemoteInput = new NotificationCompat.Action.Builder(
                R.mipmap.notification_icon, "Répondre", replyIntent)
                .addRemoteInput(remoteInput)
                .build();

        // Crée la UnreadConversation et le remplit avec le nom du participant,
        // lire et répondre aux intentions.
        NotificationCompat.CarExtender.UnreadConversation.Builder unreadConvBuilder =
                new NotificationCompat.CarExtender.UnreadConversation.Builder("Cyrille ")
                        .setLatestTimestamp(System.currentTimeMillis())
                        .setReadPendingIntent(readPendingIntent)
                        .setReplyAction(replyIntent, remoteInput);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getActivity().getApplicationContext(), MainActivity.id)
                .setSmallIcon(R.mipmap.notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(
                        getActivity().getApplicationContext().getResources(), R.mipmap.android_contact))
                .setContentText("Est-ce que vous travaillez?")
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Cyrille ")
                .setContentIntent(readPendingIntent)
                .setDeleteIntent(mDeletePendingIntent)
                .setChannelId(MainActivity.id)
                .extend(new NotificationCompat.CarExtender()
                        .setUnreadConversation(unreadConvBuilder.build())
                        .setColor(ContextCompat.getColor(getContext(), R.color.default_color_light))
                )
                .addAction(actionReplyByRemoteInput);

        logger.append("Envoi de notification " + NotificationNum + "\n");

        mNotificationManagerCompat.notify(NotificationNum, builder.build());
        NotificationNum++;
        // met à jour le nombre de notifications.
        updateNumberOfNotifications();
    }


        /*
             * méthode d'assistance que l'activité principale peut appeler pour mettre à jour le journal qu'un message a été lu.
        */

    public void NotificationRead(int id) {
        logger.append("La notification " + id + "a été lue\n");
    }

        /*
                * méthode d'assistance que l'activité principale peut utiliser pour mettre à jour l'enregistreur avec la réponse à la notification.
         */

    public void NotificationReply(int id, String message) {
        logger.append("Notification " + id + ": la réponse est " + message + "\n");
    }
}
