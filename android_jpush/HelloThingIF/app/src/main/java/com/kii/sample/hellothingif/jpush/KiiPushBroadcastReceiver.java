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
package com.kii.sample.hellothingif.jpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.kii.cloud.storage.DirectPushMessage;
import com.kii.cloud.storage.KiiUser;
import com.kii.cloud.storage.PushMessageBundleHelper;
import com.kii.cloud.storage.PushToAppMessage;
import com.kii.cloud.storage.PushToUserMessage;
import com.kii.cloud.storage.ReceivedMessage;

import cn.jpush.android.api.JPushInterface;

public class KiiPushBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "KiiPushBroadcastRecv";
    public static final String INTENT_COMMAND_RESULT_RECEIVED = "com.kii.sample.hellothingif.COMMAND_RESULT_RECEIVED";
    public static final String PARAM_COMMAND_ID = "CommandID";

    @Override
    public void onReceive(Context context, Intent intent) {
        String jpushMessageType = intent.getAction();
        if (JPushInterface.ACTION_MESSAGE_RECEIVED.equals(jpushMessageType)) {
            Bundle extras = intent.getExtras();
            ReceivedMessage message = PushMessageBundleHelper.parse(extras);
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
                    String commandID = directMsg.getMessage().getString("commandID");
                    if (commandID != null) {
                        Intent registrationComplete = new Intent(INTENT_COMMAND_RESULT_RECEIVED);
                        registrationComplete.putExtra(PARAM_COMMAND_ID, commandID);
                        LocalBroadcastManager.getInstance(context).sendBroadcast(registrationComplete);
                    }
                    break;
            }
        }
    }
}