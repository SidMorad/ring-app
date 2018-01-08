package mars.ring.interfaces.beacontag;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import mars.ring.R;
import mars.ring.application.RingApp;
import mars.ring.domain.model.beacontag.BeaconDTO;
import mars.ring.domain.model.beacontag.BeaconListStorage;
import mars.ring.interfaces.beacontag.discovery.ShowOneActivity;

/**
 * BeaconTagActivity that displays 3 tabs: list of registered beacons | Map | Notifications.
 *
 * Created by a developer on 21/11/17.
 */

public class BeaconTagActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    private RingApp app;
    private BeaconListStorage beaconListStorage;
    private final BeaconModelAdapter beaconsAdapter = new BeaconModelAdapter();
    private GoogleMap map;
    private SupportMapFragment mapFragment;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionGranted = false;
    private Location mLastKnownLocation;
    private Menu mMenu;
    private Button retryButton;
    private View listContainer;
    private View mapContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_tag_activity);

        app = (RingApp) getApplication();
        beaconListStorage = BeaconListStorage.getInstance(this);
        beaconsAdapter.setAll(beaconListStorage.getCurrent());

        listContainer = findViewById(R.id.beacon_list_container);
        mapContainer = findViewById(R.id.beacon_map_container);
        mapContainer.setVisibility(View.GONE);

        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(beaconsAdapter);
        TextView tv = (TextView) findViewById(R.id.empty);
        lv.setEmptyView(tv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(BeaconTagActivity.this, ShowOneActivity.class);
                intent.putExtra(BeaconDTO.IDENTIFIER, beaconsAdapter.getItem(i).getIdentifier());
                intent.putExtra(BeaconDTO.MAJOR, beaconsAdapter.getItem(i).getMajor());
                intent.putExtra(BeaconDTO.MINOR, beaconsAdapter.getItem(i).getMinor());
                intent.putExtra(BeaconDTO.TAG_NAME, beaconsAdapter.getItem(i).getTagName());
                startActivity(intent);
            }
        });
        retryButton = (Button) findViewById(R.id.retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBeacons();
            }
        });
        hideRetryButton();

        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_view);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        BottomNavigationView tabView = (BottomNavigationView) findViewById(R.id.navigation);
        tabView.setOnNavigationItemSelectedListener(this);

        if (RingApp.isOnline()) {
            getBeacons();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (EXPECTED_RESULT_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                aBuilder.setView(input);
                aBuilder.setTitle(getString(R.string.name_your_tag));
                aBuilder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String tagName = input.getText().toString();
                        Log.d(TAG, "Entered tagName is " + tagName);
                        createBeaconTag(tagName, data);
                    }
                });
                aBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
                aBuilder.show();
            }
        }
    }

    private void createBeaconTag(String tagName, Intent data) {
        data.putExtra(BeaconDTO.TAG_NAME, tagName);
        BeaconDTO bModel = new BeaconDTO(data);
        app.getBeaconsRepo().createBeacon(bModel, (ex) -> {
            if (ex == null) {
                getBeacons();
            } else {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getBeacons() {
        hideRetryButton();
        app.getBeaconsRepo().getBeacons((beacons, ex) -> {
            if (ex == null) {
                runOnUiThread(() -> {
                    beaconsAdapter.setAll(beacons);
                    beaconsAdapter.notifyDataSetChanged();
                });
                beaconListStorage.replace((ArrayList<BeaconDTO>) beacons);
            } else {
                Toast.makeText(this, "Error on getting tag list", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error on getting beacon list", ex);
                showRetryButton();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setMapToolbarEnabled(false); // Hide Navigation and Gps Pointer buttons.

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();
    }

    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                checkLocationPermissionGranted();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException", e);
        }
    }

    private void getDeviceLocation() {
        /**
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device
                            mLastKnownLocation = (Location) task.getResult();
                            if (mLastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), 11));
                            } else {
                                Log.d(TAG, "Current location is null. Using defaults.");
                            }

                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException", e);
        }
    }

    private void showList() {
        setTitle(getString(R.string.my_tags));
        listContainer.setVisibility(View.VISIBLE);
        mapContainer.setVisibility(View.GONE);
        getMenuInflater().inflate(R.menu.menu, mMenu);
        hideRetryButton();
    }

    private void showMap() {
        setTitle(getString(R.string.tag_locations));
        mapContainer.setVisibility(View.VISIBLE);
        listContainer.setVisibility(View.GONE);
        mMenu.clear();
    }

    private void showNotifications() {
        setTitle(getString(R.string.tag_notifications));
        mMenu.clear();
        listContainer.setVisibility(View.GONE);
        mapContainer.setVisibility(View.GONE);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.add_new_item == item.getItemId()) {
            if (app.isNetworkAvailable()) {
                startActivityForResult(new Intent(this, mars.ring.interfaces.beacontag.discovery.BeaconListActivity.class), EXPECTED_RESULT_CODE);
            } else {
                Toast.makeText(this, getString(R.string.no_internet_access), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_beacon_list:
                showList();
                return true;
            case R.id.navigation_beacon_map:
                showMap();
                return true;
            case R.id.navigation_notifications:
                showNotifications();
                return true;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_RESULT_CODE) {
            mLocationPermissionGranted = false;
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                updateLocationUI();
                getDeviceLocation();
            }
        }
    }

    public void checkLocationPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("This app needs Location access");
            builder.setMessage("Please grant location access to this app detect last seen location of your tags.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @TargetApi(Build.VERSION_CODES.M)
                public void onDismiss(DialogInterface dialog) {
                    ActivityCompat.requestPermissions(BeaconTagActivity.this,
                            new String[]{ Manifest.permission.ACCESS_FINE_LOCATION },
                            LOCATION_PERMISSION_RESULT_CODE);
                }
            });
            builder.show();
        } else {
            mLocationPermissionGranted = true;
        }
    }

    private void hideRetryButton() {
        retryButton.setVisibility(View.INVISIBLE);
    }

    private void showRetryButton() {
        retryButton.setVisibility(View.VISIBLE);
    }

    private static final String TAG = BeaconTagActivity.class.getSimpleName() + "1";

    public static final int EXPECTED_RESULT_CODE = 2;

    public static final int LOCATION_PERMISSION_RESULT_CODE = 3;
}
