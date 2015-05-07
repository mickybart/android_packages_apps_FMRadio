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
 * This class define FM Sony Ericsson interface, (FM native interface)
 */
public interface IFmDevice {

    /**
     * Open FM device, call before power up
     *
     * @return (true,success; false, failed)
     */
    public boolean openDev(Context context);

    /**
     * Close FM device, call after power down
     *
     * @return (true, success; false, failed)
     */
    public boolean closeDev();

    /**
     * power up FM with frequency use long antenna
     *
     * @param frequency frequency(50KHZ, 87.55; 100KHZ, 87.5)
     *
     * @return (true, success; false, failed)
     */
    public boolean powerUp(int frequency);

    /**
     * Power down FM
     *
     * @param type (0, FMRadio; 1, FMTransimitter)
     *
     * @return (true, success; false, failed)
     */
    public boolean powerDown(int type);

    /**
     * tune to frequency
     *
     * @param frequency frequency(50KHZ, 87.55; 100KHZ, 87.5)
     *
     * @return (true, success; false, failed)
     */
    public boolean tune(int frequency);

    /**
     * seek with frequency in direction
     *
     * @param frequency frequency(50KHZ, 87.55; 100KHZ, 87.5)
     * @param isUp (true, next station; false previous station)
     *
     * @return frequency(float)
     */
    public int seek(int frequency, boolean isUp);

    /**
     * Auto scan(from 87.50-108.00)
     *
     * @return The scan station array(short)
     */
    public short[] autoScan();

    /**
     * Stop scan, also can stop seek, other native when scan should call stop
     * scan first, else will execute wait auto scan finish
     *
     * @return (true, can stop scan process; false, can't stop scan process)
     */
    public boolean stopScan();

    /**
     * Open or close rds fuction
     *
     * @param rdson The rdson (true, open; false, close)
     *
     * @return rdsset
     */
    public int setRds(boolean rdson);

    /**
     * Read rds events
     *
     * @return rds event type
     */
    public short readRds();

    /**
     * Get program service(program name)
     *
     * @return The program name
     */
    public byte[] getPs();

    /**
     * Get radio text, RDS standard does not support Chinese character
     *
     * @return The LRT (Last Radio Text) bytes
     */
    public byte[] getLrText();

    /**
     * Active alternative frequencies
     *
     * @return The frequency(float)
     */
    public short activeAf();

    /**
     * Mute or unmute FM voice
     *
     * @param mute (true, mute; false, unmute)
     *
     * @return (1, success; -1, failed)
     */
    public int setMute(boolean mute);

    /**
     * Inquiry if RDS is support in driver
     *
     * @return (1, support; 0, NOT support; -1, error)
     */
    public int isRdsSupport();

    /**
     * Switch antenna
     *
     * @param antenna antenna (0, long antenna, 1 short antenna)
     *
     * @return (0, success; 1 failed; 2 not support)
     */
    public int switchAntenna(int antenna);

    /**
     * Fm Band support
     * @return (true, yes; false, no)
     */
    public boolean isFmBandSupport();

    /**
     * Set Fm Band
     * @param band
     * @return (true, success; false, failed)
     */
    public boolean setFmBandSupport(int band);
}
