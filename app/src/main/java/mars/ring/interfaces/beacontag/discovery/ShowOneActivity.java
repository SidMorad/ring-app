package mars.ring.interfaces.beacontag.discovery;

import android.Manifest;
import android.annotation.TargetApi;
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
import android.widget.ListView;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;

import mars.ring.R;
import mars.ring.domain.model.beacontag.Beacon;
import mars.ring.domain.model.beacontag.BeaconDTO;
import mars.ring.interfaces.beacontag.BeaconsAdapter;

/**
 * Created by developer on 14/12/17.
 */

public class ShowOneActivity extends AppCompatActivity implements BeaconConsumer, RangeNotifier {

    final private BeaconsAdapter mAdapter = new BeaconsAdapter();
    private BeaconManager beaconManager;

    private static final String TAG = ShowOneActivity.class.getSimpleName();
    private String theOneWithMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_one_beacon);

        theOneWithMac = getIntent().getStringExtra(BeaconDTO.MAC);
        String tagName = getIntent().getStringExtra(BeaconDTO.TAG_NAME);
        setTitle(getTitle() + ": " + tagName);

        ListView lView = (ListView) findViewById(R.id.list_view);
        lView.setAdapter(mAdapter);
        TextView tv = (TextView) findViewById(R.id.empty);
        lView.setEmptyView(tv);

        checkLocationPermissionGranted();
        if (!isBluetoothAvailableAndEnabled()) {
            requestForBluetooth();
        }
        beaconManager = BeaconManager.getInstanceForApplication(this.getApplicationContext());
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
        Log.d(TAG, "didRangeBeaconsInRegion event occurred! " + beacons.size() + " " + region.getUniqueId());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (beacons.size() > 0) {
                    if (mAdapter.getCount() > 0) {
                        Beacon current = mAdapter.getItem(0);
                        mAdapter.clear();
                        mAdapter.addNewBeacon(new Beacon(current));
                    }
                    for (org.altbeacon.beacon.Beacon b: beacons) {
                        if (b.getBluetoothAddress().equals(theOneWithMac)) {
                            mAdapter.clear();
                            mAdapter.addNewBeacon(Beacon.toModel(b));
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    final private static int BT_REQUEST_ID = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BT_REQUEST_ID) {
            if (!isBluetoothAvailableAndEnabled()) {
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
        if (beaconManager.isBound(this)) {
            beaconManager.setBackgroundMode(false);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (beaconManager.isBound(this)) {
            beaconManager.setBackgroundMode(true);
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
