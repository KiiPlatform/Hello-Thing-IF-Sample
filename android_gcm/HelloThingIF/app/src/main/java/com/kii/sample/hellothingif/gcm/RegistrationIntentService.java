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

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.kii.sample.hellothingif.R;
import com.kii.thingif.PushBackend;
import com.kii.thingif.ThingIFAPI;

public class RegistrationIntentService extends IntentService {
    private static final String TAG = "RegIntentService";
    public static final String INTENT_PUSH_REGISTRATION_COMPLETED = "com.kii.sample.hellothingif.COMPLETED";
    public static final String PARAM_ERROR_MESSAGE = "ErrorMessage";
    public static final String PARAM_THING_IF_API = "ThingIFAPI";

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String error = null;
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String senderId = getString(R.string.gcm_defaultSenderId);
                String token = instanceID.getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                ThingIFAPI api = intent.getParcelableExtra(PARAM_THING_IF_API);
                api.installPush(token, PushBackend.GCM);
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            error = e.getLocalizedMessage();
        }
        Intent registrationComplete = new Intent(INTENT_PUSH_REGISTRATION_COMPLETED);
        registrationComplete.putExtra(PARAM_ERROR_MESSAGE, error);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}