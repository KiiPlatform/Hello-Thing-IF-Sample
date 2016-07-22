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

import android.app.Application;

import com.kii.cloud.storage.Kii;
import com.kii.thingif.Site;

// MobileApp class should be declared in your application's AndroidManifest.xml
public class HelloThingIF extends Application {
    public static final String APP_ID = "___APP_ID___";
    public static final String APP_KEY = "___APP_KEY___";
    public static final Kii.Site APP_SITE_CLOUD = Kii.Site.US;
    public static final Site APP_SITE_THING = Site.US;

    public static final String THING_TYPE = "HelloThingIF-SmartLED";
    public static final String SCHEMA_NAME = "HelloThingIF-Schema";
    public static final int SCHEMA_VERSION = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        Kii.initialize(getApplicationContext(), APP_ID, APP_KEY, APP_SITE_CLOUD, true);
    }
}
