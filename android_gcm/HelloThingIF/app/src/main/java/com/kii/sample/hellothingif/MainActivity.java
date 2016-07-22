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
package com.kii.sample.hellothingif;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.kii.sample.hellothingif.gcm.RegistrationIntentService;
import com.kii.thingif.ThingIFAPI;
import com.kii.thingif.exception.StoredThingIFAPIInstanceNotFoundException;

public class MainActivity extends AppCompatActivity implements LoginFragment.OnFragmentInteractionListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String BUNDLE_KEY_THING_IF_API = "ThingIFAPI";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ThingIFAPI mApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkPlayServices()) {
            Toast.makeText(MainActivity.this, "This application needs Google Play services", Toast.LENGTH_LONG).show();
            return;
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String errorMessage = intent.getStringExtra(RegistrationIntentService.PARAM_ERROR_MESSAGE);
                Log.e("GCMTest", "Registration completed:" + errorMessage);
                if (errorMessage != null) {
                    Toast.makeText(MainActivity.this, "Error push registration:" + errorMessage, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "Succeeded push registration", Toast.LENGTH_LONG).show();
                }
            }
        };

        if (savedInstanceState != null) {
            // restore ThingIFAPI from the Bundle
            mApi = savedInstanceState.getParcelable(BUNDLE_KEY_THING_IF_API);
        }
        if (mApi == null) {
            // restore ThingIFAPI from the storage
            try {
                mApi = ThingIFAPI.loadFromStoredInstance(getApplicationContext());
            } catch (StoredThingIFAPIInstanceNotFoundException e) {
                mApi = null;
            }
        }
        if (savedInstanceState == null) {
            // create ui elements
            if (mApi == null) {
                // if mApi has not been set, restart the login page
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main, LoginFragment.newInstance());
                transaction.commit();
            } else {
                // if mApi has already been set, skip the login page
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main, CommandFragment.newInstance(mApi));
                transaction.commit();

                Intent intent = new Intent(this, RegistrationIntentService.class);
                intent.putExtra(RegistrationIntentService.PARAM_THING_IF_API, mApi);
                startService(intent);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_KEY_THING_IF_API, mApi);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(RegistrationIntentService.INTENT_PUSH_REGISTRATION_COMPLETED));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("PushTest", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.main, LoginFragment.newInstance());
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onThingIFInitialized(ThingIFAPI api) {
        mApi = api;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.main, CommandFragment.newInstance(mApi));
        transaction.commit();

        Intent intent = new Intent(this, RegistrationIntentService.class);
        intent.putExtra(RegistrationIntentService.PARAM_THING_IF_API, mApi);
        startService(intent);
    }
}
