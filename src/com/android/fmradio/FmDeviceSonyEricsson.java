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
import android.util.Log;

import com.stericsson.hardware.fm.FmBand;
import com.stericsson.hardware.fm.FmReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * This class define FM Sony Ericsson interface
 */
public class FmDeviceSonyEricsson implements IFmDevice, FmReceiver.onServiceAvailableListener {
    private static String TAG = "FmSonyEricsson";

    // Handle to the FM radio Band object
    private FmBand mFmBand = null;

    // Handle to the FM radio receiver object
    private FmReceiver mFmReceiver = null;

    // Protects the MediaPlayer and FmReceiver against rapid muting causing errors
    private boolean mPauseMutex = false;

    //Thread
    private Object lock = new Object();
    private short[] mStationListScanned = null;
    private int mFreqNext = 0;

    //State
    private boolean started = false;

    /**
     * Open FM device, call before power up
     *
     * @return (true,success; false, failed)
     */
    public boolean openDev(Context context) {
        Log.i(TAG, "openDev");

        synchronized (lock) {
            if (mFmReceiver == null) {
                mFmReceiver = FmReceiver.createInstance(context, this);
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Log.e(TAG, "Interrupted: " + e.getMessage());
                }
            }

            if (mFmBand == null) {
                setFmBandSupport(FmUtils.FM_BAND_US);
            }
        }

        return (mFmReceiver != null);
    }

    /**
     * Close FM device, call after power down
     *
     * @return (true, success; false, failed)
     */
    public boolean closeDev(){
        Log.i(TAG, "closeDev");

        if (mFmReceiver != null) {
            turnOffRadio();
            mFmReceiver.cleanup();
            mFmReceiver = null;
        }

        return (mFmReceiver == null);
    }

    /**
     * power up FM with frequency use long antenna
     *
     * @param frequency frequency(50KHZ, 87.55; 100KHZ, 87.5)
     *
     * @return (true, success; false, failed)
     */
    public boolean powerUp(int frequency) {
        Log.i(TAG, "powerUp: " + Integer.toString(frequency));

        if (mFmReceiver == null)
            return false;

        if (turnRadioOn()) {
            if (tune(frequency)) {
                return true;
            } else {
                turnOffRadio();
                return false;
            }
        }

        return false;
    }

    /**
     * Power down FM
     *
     * @param type (0, FMRadio; 1, FMTransimitter)
     *
     * @return (true, success; false, failed)
     */
    public boolean powerDown(int type) {
        Log.i(TAG, "powerDown: " + Integer.toString(type));

        if (mFmReceiver == null)
            return true;

        return turnOffRadio();
    }

    /**
     * tune to frequency
     *
     * @param frequency frequency(50KHZ, 87.55; 100KHZ, 87.5)
     *
     * @return (true, success; false, failed)
     */
    public boolean tune(int frequency) {
        Log.i(TAG, "tune: " + Integer.toString(frequency));

        if (mFmReceiver == null)
            return false;

        setFrequency(frequency);

        //TODO: update FMRadio to don't block the user when the frequency is not available on the BAND
        return true;
    }

    /**
     * seek with frequency in direction
     *
     * @param frequency frequency(50KHZ, 87.55; 100KHZ, 87.5)
     * @param isUp (true, next station; false previous station)
     *
     * @return frequency(float)
     */
    public int seek(int frequency, boolean isUp) {
        Log.i(TAG, "seek " + Integer.toString(frequency) + " isUp " + Boolean.toString(isUp));

        if (mFmReceiver == null)
            return frequency;

        synchronized (lock) {
            mFreqNext = 0;

            int freq_next = frequency;
            int limit = 10; //1 Mhz

            do
            {
                limit --;
                if (isUp)
                    freq_next++;
                else
                    freq_next--;
            } while (!setFrequency(freq_next) && limit >= 0);

            if (isUp) {
                mFmReceiver.scanUp();
            } else {
                mFmReceiver.scanDown();
            }

            try {
                lock.wait();
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted: " + e.getMessage());
            }

            return mFreqNext;
        }
    }

    /**
     * Auto scan(from 87.50-108.00)
     *
     * @return The scan station array(short)
     */
    public short[] autoScan() {
        Log.i(TAG, "autoScan");

        if (mFmReceiver == null)
            return null;

        synchronized (lock) {
            try {
                mStationListScanned = null;
                mFmReceiver.startFullScan();
                lock.wait();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Scan error: " + e.getMessage());
            } catch (InterruptedException e) {
                Log.e(TAG, "Interrupted: " + e.getMessage());
            }

            return mStationListScanned;
        }
    }

    /**
     * Stop scan, also can stop seek, other native when scan should call stop
     * scan first, else will execute wait auto scan finish
     *
     * @return (true, can stop scan process; false, can't stop scan process)
     */
    public boolean stopScan() {
        Log.i(TAG, "stopScan");

        if (mFmReceiver == null)
            return true;

        if (mFmReceiver.getState() == FmReceiver.STATE_SCANNING) {
            mFmReceiver.stopScan();
        }

        return true;
    }

    /**
     * Open or close rds fuction
     *
     * @param rdson The rdson (true, open; false, close)
     *
     * @return rdsset
     */
    public int setRds(boolean rdson) {
        Log.i(TAG, "setRds");
        return -1;
    }

    /**
     * Read rds events
     *
     * @return rds event type
     */
    public short readRds() {
        Log.i(TAG, "readRds");
        return 0;
    }

    /**
     * Get program service(program name)
     *
     * @return The program name
     */
    public byte[] getPs() {
        Log.i(TAG, "getPS");
        return null;
    }

    /**
     * Get radio text, RDS standard does not support Chinese character
     *
     * @return The LRT (Last Radio Text) bytes
     */
    public byte[] getLrText() {
        Log.i(TAG, "getLrText");
        return null;
    }

    /**
     * Active alternative frequencies
     *
     * @return The frequency(float)
     */
    public short activeAf() {
        Log.i(TAG, "activeAf");
        return 0;
    }

    /**
     * Mute or unmute FM voice
     *
     * @param mute (true, mute; false, unmute)
     *
     * @return (true, success; false, failed)
     */
    public int setMute(boolean mute) {
        Log.i(TAG, "setMute " + Boolean.toString(mute));

        if (mFmReceiver == null)
            return 1;

        if (!mute && mFmReceiver.getState() == FmReceiver.STATE_PAUSED && !mPauseMutex) {
            try {
                mPauseMutex = true;
                mFmReceiver.resume();
                return 1;
            } catch (Exception e) {
                Log.e(TAG, "Unable to resume. E.: " + e.getMessage());
            } finally {
                mPauseMutex = false;
            }
        } else if (mute && mFmReceiver.getState() == FmReceiver.STATE_STARTED
                && !mPauseMutex) {
            try {
                mPauseMutex = true;
                mFmReceiver.pause();
                return 1;
            } catch (Exception e) {
                Log.e(TAG, "Unable to pause. E.: " + e.getMessage());
            } finally {
                mPauseMutex = false;
            }
        } else {
            return 1;
        }

        return -1;
    }

    /**
     * Inquiry if RDS is support in driver
     *
     * @return (1, support; 0, NOT support; -1, error)
     */
    public int isRdsSupport() {
        Log.i(TAG, "isRdsSupport");
        return 0;
    }

    /**
     * Switch antenna
     *
     * @param antenna antenna (0, long antenna, 1 short antenna)
     *
     * @return (0, success; 1 failed; 2 not support)
     */
    public int switchAntenna(int antenna) {
        Log.i(TAG, "switchAntenna " + Integer.toString(antenna));
        return 2;
    }

    /**
     * Fm Band support
     * @return (true, yes; false, no)
     */
    public boolean isFmBandSupport() {
        return true;
    }

    /**
     * Set Fm Band
     * @param band
     * @return (true, success; false, failed)
     */
    public boolean setFmBandSupport(int band) {
        Log.i(TAG, "setFmBandSupport: " + Integer.toString(band));

        switch (band) {
            case FmUtils.FM_BAND_US:
                mFmBand = new FmBand(FmBand.BAND_US);
                break;
            case FmUtils.FM_BAND_EU:
                mFmBand = new FmBand(FmBand.BAND_EU);
                break;
            case FmUtils.FM_BAND_CH:
                mFmBand = new FmBand(FmBand.BAND_CHINA);
                break;
            case FmUtils.FM_BAND_JA:
                mFmBand = new FmBand(FmBand.BAND_JAPAN);
                break;
            default:
                return false;
        }

        return true;
    }

    /**
     * Private
     */

    private boolean turnOffRadio() {
        Log.i(TAG, "turnOffRadio");

        if (!started)
            return true;

        try {
            mFmReceiver.reset();
            started = false;
        } catch (IOException e) {
            Log.e(TAG, "Unable to reset correctly E.: " + e);
            return false;
        }
        return true;
    }

    private boolean turnRadioOn() {
        Log.i(TAG, "turnRadioOn");

        if(started)
            return true;

        try {
            mFmReceiver.start(mFmBand);
            started = true;
        } catch (Exception e) {
            Log.e(TAG, "turnRadioOn(). E.: " + e.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Sets the frequency for the radio and update UI accordingly
     *
     * @param freq frequency to tune to
     */
    private boolean setFrequency(int freq) {
        Log.i(TAG, "setFrequency " + Integer.toString(freq));
        try {
            mFmReceiver.setFrequency(freq * 100);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Set frequency failed! E.: " + e.getMessage());
        }

        return false;
    }

    /**
     * ScanListener
     */
    private FmReceiver.OnScanListener mReceiverScanListener = new FmReceiver.OnScanListener() {

        // FullScan results
        public void onFullScan(int[] frequency, int[] signalStrength, boolean aborted) {
            Log.i(TAG, "onFullScan(). aborted: " + aborted);

            short[] mStationList = new short[frequency.length];
            for (int i = 0; i < frequency.length; i++) {
                Log.i(TAG, "[item] freq: " + frequency[i] + ", signal: " + signalStrength[i]);

                mStationList[i] = (short)(frequency[i] / 100);
            }

            synchronized (lock) {
                mStationListScanned = mStationList;
                lock.notifyAll();
            }
        }

        // Returns the new frequency.
        public void onScan(int tunedFrequency, int signalStrength, int scanDirection, boolean aborted) {
            Log.i(TAG, "onScan(). freq: " + tunedFrequency + ", signal: " + signalStrength + ", dir: " + scanDirection + ", aborted? " + aborted);

            synchronized (lock) {
                mFreqNext = tunedFrequency / 100;
                lock.notifyAll();
            }
        }
    };

    public void onServiceAvailable() {
        Log.i(TAG, "onServiceAvailable");
        mFmReceiver.addOnScanListener(mReceiverScanListener);

        synchronized (lock){
            lock.notifyAll();
        }
    }
}
