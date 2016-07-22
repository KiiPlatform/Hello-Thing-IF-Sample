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

import android.content.Context;

import com.kii.cloud.storage.KiiUser;
import com.kii.sample.hellothingif.command.LEDState;
import com.kii.sample.hellothingif.command.SetBrightness;
import com.kii.sample.hellothingif.command.SetBrightnessResult;
import com.kii.sample.hellothingif.command.TurnPower;
import com.kii.sample.hellothingif.command.TurnPowerResult;
import com.kii.thingif.KiiApp;
import com.kii.thingif.Owner;
import com.kii.thingif.ThingIFAPI;
import com.kii.thingif.ThingIFAPIBuilder;
import com.kii.thingif.TypedID;
import com.kii.thingif.command.Action;
import com.kii.thingif.command.Command;
import com.kii.thingif.schema.Schema;
import com.kii.thingif.schema.SchemaBuilder;

import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.DeferredAsyncTask;
import org.json.JSONObject;

import java.util.List;

public class PromiseAPIWrapper {
    private AndroidDeferredManager mAdm;
    private ThingIFAPI mApi;

    public PromiseAPIWrapper(AndroidDeferredManager adm, ThingIFAPI api) {
        mAdm = adm;
        mApi = api;
    }

    public Promise<KiiUser, Throwable, Void> addUser(final String username, final String password) {
        return mAdm.when(new DeferredAsyncTask<Void, Void, KiiUser>() {
            @Override
            protected KiiUser doInBackgroundSafe(Void... voids) throws Exception {
                KiiUser.Builder builder = KiiUser.builderWithName(username);
                KiiUser user = builder.build();
                user.register(password);
                return user;
            }
        });
    }

    public Promise<ThingIFAPI, Throwable, Void> initializeThingIFAPI(final Context context, final String username, final String userPassword, final String vendorThingID, final String thingPassword) {
        return mAdm.when(new DeferredAsyncTask<Void, Void, ThingIFAPI>() {
            @Override
            protected ThingIFAPI doInBackgroundSafe(Void... voids) throws Exception {
                KiiUser ownerUser = KiiUser.logIn(username, userPassword);
                String userID = ownerUser.getID();
                String accessToken = ownerUser.getAccessToken();

                assert userID != null;
                TypedID typedUserID = new TypedID(TypedID.Types.USER, userID);
                assert accessToken != null;
                Owner owner = new Owner(typedUserID, accessToken);

                SchemaBuilder sb = SchemaBuilder.newSchemaBuilder(HelloThingIF.THING_TYPE, HelloThingIF.SCHEMA_NAME, HelloThingIF.SCHEMA_VERSION, LEDState.class);
                sb.addActionClass(TurnPower.class, TurnPowerResult.class).
                        addActionClass(SetBrightness.class, SetBrightnessResult.class);
                Schema schema = sb.build();

                KiiApp app = new KiiApp(HelloThingIF.APP_ID, HelloThingIF.APP_KEY, HelloThingIF.APP_SITE_THING);
                ThingIFAPIBuilder ib = ThingIFAPIBuilder.newBuilder(context.getApplicationContext(), app, owner);
                ib.addSchema(schema);
                mApi = ib.build();

                JSONObject properties = new JSONObject();
                mApi.onboard(vendorThingID, thingPassword, HelloThingIF.THING_TYPE, properties);

                return mApi;
            }
        });
    }

    public Promise<Command, Throwable, Void> postNewCommand(final String schemaName, final int schemaVersion, final List<Action> actions) {
        return mAdm.when(new DeferredAsyncTask<Void, Void, Command>() {
            @Override
            protected Command doInBackgroundSafe(Void... voids) throws Exception {
                return mApi.postNewCommand(schemaName, schemaVersion, actions);
            }
        });
    }

    public Promise<LEDState, Throwable, Void> getTargetState() {
        return mAdm.when(new DeferredAsyncTask<Void, Void, LEDState>() {
            @Override
            protected LEDState doInBackgroundSafe(Void... voids) throws Exception {
                return mApi.getTargetState(LEDState.class);
            }
        });
    }

    public Promise<Command, Throwable, Void> getCommand(final String commandID) {
        return mAdm.when(new DeferredAsyncTask<Void, Void, Command>() {
            @Override
            protected Command doInBackgroundSafe(Void... voids) throws Exception {
                return mApi.getCommand(commandID);
            }
        });
    }
}
