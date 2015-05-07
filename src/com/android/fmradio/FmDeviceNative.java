/*
 * Copyright (C) 2015 mickybart@xda
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

package com.android.fmradio;

import android.content.Context;

/**
 * This class define FM Native interface
 */
public class FmDeviceNative implements IFmDevice {
    private static final float CONVERT_RATE = 10.0f;

    public boolean openDev(Context context) {
        return FmNative.openDev();
    }

    public boolean closeDev() {
        return FmNative.closeDev();
    }

    public boolean powerUp(int frequency) {
        return FmNative.powerUp(frequency/CONVERT_RATE);
    }

    public boolean powerDown(int type) {
        return FmNative.powerDown(type);
    }

    public boolean tune(int frequency) {
        return FmNative.tune(frequency/CONVERT_RATE);
    }

    public int seek(int frequency, boolean isUp) {
        return (int)(FmNative.seek(frequency/CONVERT_RATE, isUp) * CONVERT_RATE);
    }

    public short[] autoScan() {
        return FmNative.autoScan();
    }

    public boolean stopScan() {
        return FmNative.stopScan();
    }

    public int setRds(boolean rdson) {
        return FmNative.setRds(rdson);
    }

    public short readRds() {
        return FmNative.readRds();
    }

    public byte[] getPs() {
        return FmNative.getPs();
    }

    public byte[] getLrText() {
        return FmNative.getLrText();
    }

    public short activeAf() {
        return FmNative.activeAf();
    }

    public int setMute(boolean mute) {
        return FmNative.setMute(mute);
    }

    public int isRdsSupport() {
        return FmNative.isRdsSupport();
    }

    public int switchAntenna(int antenna) {
        return FmNative.switchAntenna(antenna);
    }

    public boolean isFmBandSupport() {
        return false;
    }

    public boolean setFmBandSupport(int band) {
        return false;
    }
}
