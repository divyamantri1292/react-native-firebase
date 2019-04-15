package io.invertase.firebase.messaging;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.facebook.react.HeadlessJsTaskService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Random;

import io.invertase.firebase.R;
import io.invertase.firebase.Utils;

public class RNFirebaseMessagingService extends FirebaseMessagingService {
  private static final String TAG = "RNFMessagingService";
  public static final String MESSAGE_EVENT = "messaging-message";
  public static final String REMOTE_NOTIFICATION_EVENT = "notifications-remote-notification";

  String lastMessageId="";


  private void sendBroadCast(String message){
    Intent bIntent = new Intent("com.yakazi.serviceprovider");
    bIntent.putExtra("Message", message);
    //bIntent.putExtra(Tags.KEY_ACTION, notifType);
    sendBroadcast(bIntent);
  }


  private void sendBroadCastThroughService(String message){

  //  ComponentName cn = new ComponentName("host.exp.exponent", "host.exp.exponent.NotificationService");
    Intent intent = null;
    try {
      intent = new Intent(this,Class.forName("host.exp.exponent.NotificationService"));
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
    intent.putExtra("Message", message);
    startService(intent);
  }


  @Override
  public void onMessageReceived(RemoteMessage message) {
    Log.d(TAG, "onMessageReceived event received "+message.getData());

    if (message.getNotification() != null) {
      // It's a notification, pass to the Notifications module
      Intent notificationEvent = new Intent(REMOTE_NOTIFICATION_EVENT);
      notificationEvent.putExtra("notification", message);

      // Broadcast it to the (foreground) RN Application
      LocalBroadcastManager.getInstance(this).sendBroadcast(notificationEvent);
    } else {
      // It's a data message
      // If the app is in the foreground we send it to the Messaging module
      if (Utils.isAppInForeground(this.getApplicationContext())) {
        Intent messagingEvent = new Intent(MESSAGE_EVENT);
        messagingEvent.putExtra("message", message);
        // Broadcast it so it is only available to the RN Application
        LocalBroadcastManager.getInstance(this).sendBroadcast(messagingEvent);
      } else {
//        try {
//          // If the app is in the background we send it to the Headless JS Service
//          Intent headlessIntent = new Intent(this.getApplicationContext(), RNFirebaseBackgroundMessagingService.class);
//          headlessIntent.putExtra("message", message);
//          this.getApplicationContext().startService(headlessIntent);
//          HeadlessJsTaskService.acquireWakeLockNow(this.getApplicationContext());
//        } catch (IllegalStateException ex) {
//          Log.e(TAG, "Background messages will only work if the message priority is set to 'high'", ex);
//        }



          try {
            String mId = message.getData().get("sendbird");
            if (mId != null) {
              JSONObject obj = new JSONObject(mId);
              if (!lastMessageId.equalsIgnoreCase(obj.getString("message_id"))) {
                lastMessageId = obj.getString("message_id");
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                  sendBroadCast(message.getData().get("message"));
                }
                else {
                   //-Handle code above oreo------
                  sendBroadCastThroughService(message.getData().get("message"));
                }

              }
            } else {
              String body = message.getData().get("body");
              if (body != null) {
                JSONObject obj = new JSONObject(body);
                if (!lastMessageId.equalsIgnoreCase(obj.getString("order_id"))) {
                  lastMessageId = obj.getString("order_id");

                  if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    sendBroadCast(message.getData().get("message"));
                  }
                  else {
                    //-Handle code above oreo------
                    sendBroadCastThroughService(message.getData().get("message"));
                  }

                }
              }
            }


          } catch (Exception e) {
            e.printStackTrace();
          }
        }

    }
  }

}
