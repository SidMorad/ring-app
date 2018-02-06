package mars.ring.application;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.io.BaseEncoding;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import mars.ring.domain.model.beacontag.BeaconLTCommand;
import mars.ring.domain.model.beacontag.BeaconLTStorage;
import mars.ring.domain.model.beacontag.BeaconsPublicRepo;
import mars.ring.domain.model.user.AuthRepo;
import mars.ring.domain.model.beacontag.BeaconsRepo;

/**
 * Created by developer on 07/12/17.
 */
public final class RingApp extends android.app.Application implements BootstrapNotifier, BeaconConsumer, RangeNotifier {

    public static final String RING_UUID = "0be1cc29-2222-4444-8888-00bbee11cc00";
    public static final Identifier RING_ID1 = Identifier.fromUuid(UUID.fromString(RING_UUID));
    public final static int RC_FAIL = 0;
    public final static int RC_AUTH = 100;
    public static boolean offline = false;
    public static boolean authenticated = false;
    public static final long backgroundBetweenScanPeriod = 600000L; // the time between each scan to be 1 hour (3600 seconds)
    public static final long foregroundBetweenScanPeriod = 600000l; // for now same as background

    private AuthRepo authRepo;
    private BeaconsRepo beaconsRepo;
    private BeaconsPublicRepo beaconsPublicRepo;
    private BeaconManager beaconManager;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;
    private Region globalRegion;
    private final HashMap<Integer, BeaconLTCommand> foundBeacons = new HashMap<Integer, BeaconLTCommand>();
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        AndroidThreeTen.init(this);

        authRepo = new AuthRepo(this);
        beaconsRepo = new BeaconsRepo(this, authRepo);
        beaconsPublicRepo = new BeaconsPublicRepo(this);

        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        // iBeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(
                "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        Log.d(TAG, "Setting up background monitoring for beacons and power saving");
        // wakeup the app when a beacon is seen
        globalRegion = new Region("backgroundRegion", RING_ID1, null, null);
        regionBootstrap = new RegionBootstrap(this, globalRegion);

        // simply constructing this class and holding a reference to it in Application
        // will automatically cause the BeaconLibrary to save battery whenever tha application
        // is not visible. This reduces bluetooth power usage about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);

        beaconManager.bind(this);

        BeaconLTStorage beaconLTStorage = BeaconLTStorage.getInstance(this);
        Log.d(TAG, "Number of saved beacons are " + beaconLTStorage.getCurrent().size());
        for (BeaconLTCommand b: beaconLTStorage.getCurrent()) {
            foundBeacons.put(b.hashCode(), b);
            Log.d(TAG, "Put " + b.hashCode() + " into found beacons.");
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        sendBeaconTracesToServer();
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.setBackgroundBetweenScanPeriod(backgroundBetweenScanPeriod);
        beaconManager.setForegroundBetweenScanPeriod(foregroundBetweenScanPeriod);
        try {
            beaconManager.updateScanPeriods();
        } catch(Exception e) {
            Log.e(TAG, "Can't talk to beacon service.");
        }
        beaconManager.addRangeNotifier(this);
        try {
            beaconManager.startRangingBeaconsInRegion(globalRegion);
        } catch (RemoteException e) {
            Log.e(TAG, "RemoteException: ", e);
        }
    }

    @Override
    public void didEnterRegion(Region region) {
        Log.d(TAG, "did enter region " + haveDetectedBeaconsSinceBoot);
        if (!haveDetectedBeaconsSinceBoot) {
            haveDetectedBeaconsSinceBoot = true;
        }
        Log.d(TAG, "Region was " + region.toString());
    }

    @Override
    public void didExitRegion(Region region) {
        Log.d(TAG, "I no longer see a beacon");
        Log.d(TAG, "Region was " + region.toString());
    }

    @Override
    public void didDetermineStateForRegion(int state, Region region) {
        Log.d(TAG, "I have just switched from seeing/not seeing beacons: " + state + " #of found beacons is " + foundBeacons.size());
        Log.d(TAG, "Region was " + region.toString());
        sendBeaconTracesToServer();
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        if (region.equals(globalRegion)) {
            if (beaconManager.getForegroundBetweenScanPeriod() == foregroundBetweenScanPeriod) {   // IMPORTANT check otherwise when foregroundBetweenScanPeriod is decrease in other activity, then battery will drain because of calling location service too frequently!
                Log.d(TAG, "Region: " + region.getUniqueId() + " " + collection.size());
                if (isLocationAvailable()) {
                    if (!collection.isEmpty()) {
                        try {
                            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                            locationResult.addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    if (task.isSuccessful()) {
                                        Location lastLocation = task.getResult();
                                        if (lastLocation != null) {
                                            for (Beacon b : collection) {
                                                BeaconLTCommand blt = new BeaconLTCommand(b, lastLocation.getLatitude(), lastLocation.getLongitude());
                                                foundBeacons.put(blt.hashCode(), blt);
                                            }
                                            saveFoundBeaconBLs();
                                        }
                                    }
                                }
                            });
                        } catch (SecurityException e) {
                            Log.e(TAG, "SecurityException on get last location: ", e);
                        }
                    } else {
                        Log.v(TAG, "Wanting to record last seen location of beacons, but no beacons found!");
                    }
                } else {
                    Log.v(TAG, "Wanting to record last seen location of beacons, but  location service is disabled!");
                }
            }
        }
    }

    public static boolean isOnline() {
        return !offline;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public boolean isLocationAvailable() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean netEnabled = false;
        try {
            gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {}
        try {
            netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {}
        return gpsEnabled || netEnabled;
    }

    public boolean isBluetoothAvailableAndEnabled() {
        BluetoothManager btManager = null;
        btManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        BluetoothAdapter mBtAdapter = btManager.getAdapter();
        return mBtAdapter != null && mBtAdapter.isEnabled();
    }

    @Override
    public void onTerminate() {
        Log.d(TAG, "onTerminate actually called!!!");
        super.onTerminate();
    }


    public AuthRepo getAuthRepo() {
        return authRepo;
    }

    public AuthRepo auth() {
        return authRepo;
    }

    public BeaconsRepo getBeaconsRepo() {
        return beaconsRepo;
    }

    public String getSignature() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if (packageInfo == null
                    || packageInfo.signatures == null
                    || packageInfo.signatures.length == 0
                    || packageInfo.signatures[0] == null) {
                return null;
            }
            return signatureDigest(packageInfo.signatures[0]);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public void sendBeaconTracesToServer() {
        if (isNetworkAvailable()) {
            if (!foundBeacons.isEmpty()) {
                if (authenticated) {
                    beaconsRepo.sendBeaconLT(new HashSet<>(foundBeacons.values()), (ex) -> {
                        if (ex == null) {
                            Log.d(TAG, "Sending " + foundBeacons.size() + " beacon's location successfully.");
                            foundBeacons.clear();
                            BeaconLTStorage.getInstance(this).replace(new HashSet<>());
                        } else {
                            Log.e(TAG, "Sending BeaconLTs failed: " + ex.code() + ", " + ex.getMessage(), ex);
                        }
                    });
                }
                else {
                    beaconsPublicRepo.sendBeaconLT(new HashSet<>(foundBeacons.values()), (ex) -> {
                        if (ex == null) {
                            Log.d(TAG, "Sending " + foundBeacons.size() + " beacon's location successfully.");
                            foundBeacons.clear();
                            BeaconLTStorage.getInstance(this).replace(new HashSet<>());
                        } else {
                            Log.e(TAG, "Sending BeaconLTs failed: " + ex.code() + ", " + ex.getMessage(), ex);
                        }
                    });
                }
            }
        }
    }

    private void saveFoundBeaconBLs() {
        BeaconLTStorage storage = BeaconLTStorage.getInstance(this);
        Log.d(TAG, "Writing " + foundBeacons.size() + " BeaconLTCommand into storage.");
        storage.replace(new HashSet<>(foundBeacons.values()));
    }

    private String signatureDigest(Signature sig) {
        byte[] signature = sig.toByteArray();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] digest = md.digest(signature);
            return BaseEncoding.base16().lowerCase().encode(digest);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static final String TAG = RingApp.class.getSimpleName() + "1";

}
