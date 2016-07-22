/*
 * Copyright 2016 Kii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kii.sample.hellothingif.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.kii.cloud.storage.DirectPushMessage;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushToAppMessage;
import com.kii.cloud.storage.PushToUserMessage;
import com.kii.cloud.storage.ReceivedMessage;

public class MyGcmListenerService extends GcmListenerService {
    private static final String TAG = "MyGcmListenerService";
    public static final String INTENT_COMMAND_RESULT_RECEIVED = "com.kii.sample.hellothingif.COMMAND_RESULT_RECEIVED";
    public static final String PARAM_COMMAND_ID = "CommandID";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        ReceivedMessage message = PushMessageBundleHelper.parse(data);
        KiiUser sender = message.getSender();
        PushMessageBundleHelper.MessageType type = message.pushMessageType();
        switch (type) {
            case PUSH_TO_APP:
                PushToAppMessage appMsg = (PushToAppMessage) message;
                Log.d(TAG, "PUSH_TO_APP Received");
                break;
            case PUSH_TO_USER:
                PushToUserMessage userMsg = (PushToUserMessage) message;
                Log.d(TAG, "PUSH_TO_USER Received");
                break;
            case DIRECT_PUSH:
                DirectPushMessage directMsg = (DirectPushMessage) message;
                Log.d(TAG, "DIRECT_PUSH Received");
                String commandID = data.getString("commandID");
                if (commandID != null) {
                    Intent registrationComplete = new Intent(INTENT_COMMAND_RESULT_RECEIVED);
                    registrationComplete.putExtra(PARAM_COMMAND_ID, commandID);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
                }
                break;
        }
    }
/*
  private void sendNotification(String message) {
    Intent intent = new Intent(this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
        PendingIntent.FLAG_ONE_SHOT);

    Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.ic_stat_ic_notification)
        .setContentTitle("GCM Message")
        .setContentText(message)
        .setAutoCancel(true)
        .setSound(defaultSoundUri)
        .setContentIntent(pendingIntent);

    NotificationManager notificationManager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

    notificationManager.notify(0, notificationBuilder.build());
  }
*/
}