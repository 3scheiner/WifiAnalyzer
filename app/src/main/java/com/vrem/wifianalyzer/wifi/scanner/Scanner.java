/*
 *    Copyright (C) 2015 - 2016 VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.vrem.wifianalyzer.wifi.scanner;

import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.wifi.model.WiFiData;

import java.util.Map;
import java.util.TreeMap;

public class Scanner {
    private final MainContext mainContext = MainContext.INSTANCE;
    private final PeriodicScan periodicScan;
    private final Map<String, UpdateNotifier> updateNotifiers;
    private Cache cache;
    private Transformer transformer;

    public Scanner() {
        this.periodicScan = new PeriodicScan(this);
        this.updateNotifiers = new TreeMap<>();
        setTransformer(new Transformer());
        setCache(new Cache());
    }

    public void update() {
        mainContext.getLogger().info(this, "running update...");
        WifiManager wifiManager = mainContext.getWifiManager();
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        if (wifiManager.startScan()) {
            cache.add(wifiManager.getScanResults());
            WiFiData wiFiData = transformer.transformToWiFiData(wifiManager.getScanResults(), wifiManager.getConnectionInfo(), wifiManager.getConfiguredNetworks());
            for (String key : updateNotifiers.keySet()) {
                UpdateNotifier updateNotifier = updateNotifiers.get(key);
                mainContext.getLogger().info(this, "running notifier: " + key);
                updateNotifier.update(wiFiData);
            }
        }
    }

    public void addUpdateNotifier(@NonNull UpdateNotifier updateNotifier) {
        String key = updateNotifier.getClass().getName();
        mainContext.getLogger().info(this, "register notifier: " + key);
        updateNotifiers.put(key, updateNotifier);
    }

    PeriodicScan getPeriodicScan() {
        return periodicScan;
    }

    void setCache(@NonNull Cache cache) {
        this.cache = cache;
    }

    void setTransformer(@NonNull Transformer transformer) {
        this.transformer = transformer;
    }

    Map<String, UpdateNotifier> getUpdateNotifiers() {
        return updateNotifiers;
    }
}
