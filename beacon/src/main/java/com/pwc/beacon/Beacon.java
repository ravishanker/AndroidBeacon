package com.pwc.beacon;

import java.util.List;

/**
 * Created by ravi on 6/01/2014.
 */
public class Beacon {

    private static final int UUID_SERVICE_BEACON = 0x1809;

    private String mName;
    private int mSignal;
    private String mAddress;

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
            }
        }
    }

    public String getName() {
        return mName;
    }

    public int getSignal() {
        return mSignal;
    }


    public String getAddress() {
        return mAddress;
    }

    @Override
    public String toString() {
        return String.format("%s (%ddBm): ", mName, mSignal);
    }
}
