/*
 * Copyright (C) 2017 VNG IoT Lab
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.iot.nima.blelib.core;

import java.util.HashMap;

/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 */
public class LedButtonGattAttributes {

    public static final String SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    public static final String WRITE_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";  //WRITE-uchar
    public static final String NOTIFY_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";  //NOTIFY-uchar

    private static HashMap<String, String> attributes = new HashMap<>();
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    static {
        attributes.put(SERVICE_UUID, "LED_BUTTON_SERVICE");
        attributes.put(WRITE_UUID, "LED");
        attributes.put(NOTIFY_UUID, "BUTTON");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
}
