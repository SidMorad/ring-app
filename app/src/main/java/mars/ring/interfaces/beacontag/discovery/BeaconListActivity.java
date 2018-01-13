package mars.ring.interfaces.beacontag.discovery;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import mars.ring.R;
import mars.ring.application.RingApp;
import mars.ring.domain.model.beacontag.Beacon;
import mars.ring.domain.model.beacontag.BeaconDTO;
import mars.ring.interfaces.beacontag.BeaconsAdapter;

/**
 * BeaconListActivity a class that shows list of (known/unknown)beacons.
 *
 * Created by a developer on 23/10/17.
 */

public class BeaconListActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    final private BeaconsAdapter mAdapter = new BeaconsAdapter();
    private BeaconManager beaconManager;
    private Region discoveryRegion = new Region("myDiscoveryUniqueId", null, null, null);

    private static final String TAG = BeaconListActivity.class.getSimpleName() + "1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discovery_beacon_list);

        ListView lView = (ListView) findViewById(R.id.list_view);
        lView.setAdapter(mAdapter);
        TextView tv = (TextView) findViewById(R.id.empty);
        lView.setEmptyView(tv);
        lView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(TAG, "onItemClick: " + i + ": " + l + " view: " + view + " data: " + mAdapter.getItem(i));
                Intent resultIntent = new Intent();
                resultIntent.putExtra(BeaconDTO.MAC, mAdapter.getItem(i).mac);
                resultIntent.putExtra(BeaconDTO.IDENTIFIER, mAdapter.getItem(i).identifier);
                resultIntent.putExtra(BeaconDTO.MAJOR, mAdapter.getItem(i).major);
                resultIntent.putExtra(BeaconDTO.MINOR, mAdapter.getItem(i).minor);
                resultIntent.putExtra(BeaconDTO.TX_POWER, mAdapter.getItem(i).txPower);
                resultIntent.putExtra(BeaconDTO.BATTERY_LEVEL, mAdapter.getItem(i).batteryLevel);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());

        checkLocationPermissionGranted();
        if (isBluetoothAvailableAndEnabled()) {
            beaconManager.bind(this);
        } else {
            requestForBluetooth();
        }
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(discoveryRegion);
        } catch (RemoteException e) {
            Log.e(TAG, "onStartRangingBeaconsInRegion", e);
        }
    }

    @Override
    public void didRangeBeaconsInRegion(final Collection<org.altbeacon.beacon.Beacon> beacons, Region region) {
        Log.d(TAG, "didRangeBeaconsInRegion event occurred! " + beacons.size() + " " + region.getUniqueId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.setAll(Beacon.toList(beacons));
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    final private static int BT_REQUEST_ID = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_REQUEST_ID) {
            if (!isBluetoothAvailableAndEnabled()) {
                super.onActivityResult(requestCode, resultCode, data);
            } else {
                beaconManager.bind(this);
            }
        }
    }

    private void requestForBluetooth() {
        Intent request = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(request, BT_REQUEST_ID);
    }

    private boolean isBluetoothAvailableAndEnabled() {
        BluetoothManager btManager = null;
        btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        BluetoothAdapter mBtAdapter = btManager.getAdapter();
        return mBtAdapter != null && mBtAdapter.isEnabled();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (beaconManager.isBound(this)) {
            beaconManager.setBackgroundMode(false);
            beaconManager.setForegroundBetweenScanPeriod(0l);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) {
            beaconManager.setBackgroundMode(true);
            beaconManager.setForegroundBetweenScanPeriod(RingApp.foregroundBetweenScanPeriod);
            try {
                beaconManager.stopRangingBeaconsInRegion(discoveryRegion);
            } catch (RemoteException e) {
                Log.e(TAG, "OnStopRangingBeaconsInRegion", e);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (beaconManager != null) {
            beaconManager.unbind(this);
        }
    }

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 2;

    public void onRequestPermissionResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_COARSE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Coarse location permission granted.");
                beaconManager.bind(this);
            }
            else {
                checkLocationPermissionGranted();
            }
        }
    }

    public void checkLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android Marshmello(6) permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("This app needs Location access");
                builder.setMessage("Please grant location access to this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }

}
