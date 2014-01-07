package com.pwc.beacon;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BeaconActivity extends Activity implements BluetoothAdapter.LeScanCallback {

    private static final String TAG = "BeaconActivity";
//    private static final UUID ESTIMOTE_UUID = UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");
    private static final UUID ESTIMOTE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");


    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, Beacon> mBeacons;
    private BeaconAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setProgressBarIndeterminate(true);

//        setContentView(R.layout.activity_beacon);

//        if (savedInstanceState == null) {
//            getFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }

        ListView list = new ListView(this);
        mAdapter = new BeaconAdapter(this);
        list.setAdapter(mAdapter);
        setContentView(list);

        BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        mBluetoothAdapter = manager.getAdapter();

        mBeacons = new HashMap<String, Beacon>();

    }

    @Override
    protected void onResume(){
        super.onResume();

        // check if Bluetooth is enable if not take the user to settings
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            // Bluetooth is disabled
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            finish();
            return;
        }

        // Check for Bluetooth LE support.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "No LE Support.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        startScan();
    }

    protected void onPause() {
        super.onPause();

        // Cancel any scans in progress
        mHandler.removeCallbacks(mStopRunnable);
        mHandler.removeCallbacks(mStartRunnable);
        mBluetoothAdapter.startLeScan(this);
    }

    private Runnable mStartRunnable = new Runnable() {
        public void run() {
            startScan();
        }
    };

    private Runnable mStopRunnable = new Runnable() {
        @Override
        public void run() {
            stopScan();
        }
    };

    private void startScan() {
        // Scan for devices advertising the beacon service
        mBluetoothAdapter.startLeScan(new UUID[] {ESTIMOTE_UUID}, this);
        setProgressBarIndeterminateVisibility(true);

        mHandler.postDelayed(mStopRunnable, 5000);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(this);
        setProgressBarIndeterminateVisibility(false);

        mHandler.postDelayed(mStartRunnable, 2500);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.beacon, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.i(TAG, "New LE Device: " + device.getName() + " @ " + rssi);

        // Parse Advertisement Data from the scan record
        List<AdRecord> records = AdRecord.parseScanRecord(scanRecord);
        if (records.size() == 0) {
            Log.i(TAG, "Scan Record Empty");

        } else {
            Log.i(TAG, "Scan Record: " + TextUtils.join(",", records ));
        }

        Beacon beacon = new Beacon(records, device.getAddress(), rssi);
        mHandler.sendMessage(Message.obtain(null, 0, beacon));

    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Beacon beacon = (Beacon) msg.obj;
            mBeacons.put(beacon.getAddress(), beacon);

            mAdapter.setNotifyOnChange(false);
            mAdapter.clear();
            mAdapter.addAll(mBeacons.values());
            mAdapter.notifyDataSetChanged();
        }

    };

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_beacon, container, false);
            return rootView;
        }
    }


    /**
     * A custom adapter to display Beacon data in columns
     *
     */
    private static class BeaconAdapter extends ArrayAdapter<Beacon> {

        public BeaconAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_beacon_list, parent, false);
            }

            Beacon beacon = getItem(position);

            TextView nameView = (TextView) convertView.findViewById(R.id.text_name);
            nameView.setText(beacon.getName());

            TextView addressView = (TextView) convertView.findViewById(R.id.text_address);
            addressView.setText(beacon.getAddress());

            TextView rssiView = (TextView) convertView.findViewById(R.id.text_rssi);
            rssiView.setText(String.format("%ddBm", beacon.getSignal()));

            return convertView;
        }
    }

}
