package mars.ring.interfaces.beacon.discovery;

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
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import mars.ring.R;
import mars.ring.domain.model.beacontag.Beacon;
import mars.ring.domain.model.beacontag.BeaconDTO;
import mars.ring.interfaces.beacon.BeaconsAdapter;

/**
 * BeaconListActivty a class that shows list of (known/unknown)beacons.
 *
 * Created by a developer on 23/10/17.
 */

public class BeaconListActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    final private BeaconsAdapter mAdapter = new BeaconsAdapter();
    private BeaconManager beaconManager;

    private static final String TAG = "D.BeaconListActivity";

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
                resultIntent.putExtra(BeaconDTO.MAC, mAdapter.getItem(i).id);
                resultIntent.putExtra(BeaconDTO.IDENTIFIER, mAdapter.getItem(i).uuid);
                resultIntent.putExtra(BeaconDTO.MAJOR, mAdapter.getItem(i).major);
                resultIntent.putExtra(BeaconDTO.MINOR, mAdapter.getItem(i).minor);
                resultIntent.putExtra(BeaconDTO.TX_POWER, mAdapter.getItem(i).txPower);
//                resultIntent.putExtra(BeaconDTO.BATTERY_LEVEL, mAdapter.getItem(i).batteryLevel);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        checkLocationPermissionGranted();
        if (isBluetoothAvailableAndEnabled()) {
            initalizeBeaconManager();
        } else {
            requestForBluetooth();
        }
    }

    private void initalizeBeaconManager() {
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
        // Add iBeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
            Log.e(TAG, "onStartRangingBeaconsInRegion", e);
        }
    }

    @Override
    public void didRangeBeaconsInRegion(final Collection<org.altbeacon.beacon.Beacon> beacons, Region region) {
        Log.d(TAG, "didRangeBeaconsInRegion event occured! " + beacons.size() + " " + region.getUniqueId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.clear();
                if (beacons.size() > 0) {
                    mAdapter.setAll(Beacon.toList(beacons));
                }
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    final private static int BT_REQUEST_ID = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_REQUEST_ID) {
            if (isBluetoothAvailableAndEnabled()) {
                initalizeBeaconManager();
            }
            else {
                super.onActivityResult(requestCode, resultCode, data);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
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
