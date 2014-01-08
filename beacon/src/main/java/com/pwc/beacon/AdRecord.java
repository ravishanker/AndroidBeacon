package com.pwc.beacon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ravi on 6/01/2014.
 */
public class AdRecord {
    // An incomplete list of the Bluetooth General Access Profile (GAP) Advertising Data (AD) Type identifiers
    // https://www.bluetooth.org/en-us/specification/assigned-numbers/generic-access-profile
    public static final int TYPE_FLAGS = 0x1;
    public static final int TYPE_UUID16_INC = 0x2;
    public static final int TYPE_UUID16 = 0x3;
    public static final int TYPE_UUID32_INC = 0x4;
    public static final int TYPE_UUID32 = 0x5;
    public static final int TYPE_UUID128_INC = 0x6;
    public static final int TYPE_UUID128 = 0x7;
    public static final int TYPE_NAME_SHORT = 0x8;
    public static final int TYPE_NAME = 0x9;
    public static final int TYPE_TRANSMITPOWER = 0xA;
    public static final int TYPE_CONNINTERVAL = 0x12;
    public static final int TYPE_SERVICEDATA = 0x16;

    private int mLength;
    private int mType;
    private byte[] mData;


    public AdRecord(int length, int type, byte[] data) {
        mLength = length;
        mType = type;
        mData = data;
    }

    public int getLength() {
        return mLength;
    }

    public int getType() {
        return mType;
    }

    @Override
    public String toString() {
        switch (mType) {
            case TYPE_FLAGS:
                return "Flags";
            case TYPE_NAME_SHORT:
            case TYPE_NAME:
                return "Name";
            case TYPE_UUID16:
            case TYPE_UUID16_INC:
                return "UUIDs";
            case TYPE_TRANSMITPOWER:
                return "Transmit Power";
            case TYPE_CONNINTERVAL:
                return "Connect Interval";
            case TYPE_SERVICEDATA:
                return "Service Data";
            default:
                return "Unknown Structure: "+mType;
        }
    }



    // Read all the Advertising Data structures from the raw scan record
    public static List<AdRecord> parseScanRecord(byte[] scanRecord) {
        List<AdRecord> records = new ArrayList<AdRecord>();

        int index = 0;
        while (index < scanRecord.length) {
            int length = scanRecord[index++];

            // done once we run out of records
            if (length == 0) break;

            int type = scanRecord[index];

            if (type == 0) break;

            byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);
            records.add(new AdRecord(length, type, data));

            // increment
            index += length;
        }

        return records;

    }

    public static String getName(AdRecord nameRecord) {
        return new String(nameRecord.mData);
    }

    public static int getServiceDataUuid(AdRecord serviceData) {
        if (serviceData.mType != TYPE_SERVICEDATA) return  -1;

        byte[] raw = serviceData.mData;

        // find UUID data in byte array
        int uuid = (raw[1] & 0xFF) << 8;
        uuid += (raw[0] & 0xFF);

        return uuid;
    }

    public static byte[] getServiceData(AdRecord serviceData) {
        if (serviceData.mType != TYPE_SERVICEDATA) return null;

        byte[] raw = serviceData.mData;

        // chop off uuid
        return Arrays.copyOfRange(raw, 2, raw.length);
    }

}
