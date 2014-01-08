package com.pwc.beacon;

import android.util.Log;

import java.util.List;

/**
 * Created by ravi on 6/01/2014.
 */
public class Beacon {

    private static final String TAG = "Beacon";

    private static final int UUID_SERVICE_BEACON = 0x1809;

    private String mName;
    private int mServiceData;
    private int mMajor;
    private int mMinor;
    private int mSignal;
    private String mAddress;
    protected int startByte;


    public Beacon(List<AdRecord> records, String deviceAddress, int rssi) {
        mSignal = rssi;
        mAddress = deviceAddress;

        for (AdRecord packet : records) {
            // Find the device name record
            if (packet.getType() == AdRecord.TYPE_NAME) {
                mName = AdRecord.getName(packet);
            }

            // Find the service data record that contains service's UUID
            if (packet.getType() == AdRecord.TYPE_SERVICEDATA && AdRecord.getServiceDataUuid(packet) == UUID_SERVICE_BEACON) {
                byte[] data = AdRecord.getServiceData(packet);

                mServiceData = (data[0] & 0xFF);
                mMajor = (data[startByte + 20] & 0xff) * 0x100 + (data[startByte + 21] & 0xff);
                mMinor = (data[startByte+22] & 0xff) * 0x100 + (data[startByte+23] & 0xff);
                Log.i(TAG, "Major: " + mMajor);
                Log.i(TAG, "Minor: " + mMinor);

            }
        }
    }

    public String getName() {
        return mName;
    }

    public int getSignal() {
        return mSignal;
    }

    public int getServiceData () { return mServiceData; }

    public String getAddress() {
        return mAddress;
    }

    public int getMajor() { return mMajor; }

    @Override
    public String toString() {
        return String.format("%s (%ddBm): ", mName, mSignal);
    }
}
