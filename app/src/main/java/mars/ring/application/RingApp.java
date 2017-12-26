package mars.ring.application;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.google.common.io.BaseEncoding;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.BootstrapNotifier;
import org.altbeacon.beacon.startup.RegionBootstrap;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mars.ring.domain.model.user.AuthRepo;
import mars.ring.domain.model.beacontag.BeaconsRepo;

/**
 * Created by developer on 07/12/17.
 */

public final class RingApp extends android.app.Application implements BootstrapNotifier {

    public final static int RC_FAIL = 0;
    public final static int RC_AUTH = 100;

    private AuthRepo authRepo;
    private BeaconsRepo beaconsRepo;
    private BeaconManager beaconManager;
    private RegionBootstrap regionBootstrap;
    private BackgroundPowerSaver backgroundPowerSaver;
    private boolean haveDetectedBeaconsSinceBoot = false;

    private static final String TAG = RingApp.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        authRepo = new AuthRepo(this);
        beaconsRepo = new BeaconsRepo(this, authRepo);

        beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        // iBeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(
                "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        Log.d(TAG, "Setting up background monitoring for beacons and power saving");
        // wakeup the app when a beacon is seen
        Region region = new Region("backgroundRegion", null, null, null);
        regionBootstrap = new RegionBootstrap(this, region);

        // simply constructing this class and holding a reference to it in Application
        // will automatically cause the BeaconLibrary to save battery whenever tha application
        // is not visible. This reduces bluetooth power usage about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        // An example of rather extreme battery saving configuration
//        beaconManager.setBackgroundScanPeriod(1100l);   // set duration of the scan be to be 1.1 seconds
//        beaconManager.setBackgroundBetweenScanPeriod(3600000l); // set the time between each scan to be 1 hour (3600 seconds)

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
        Log.d(TAG, "I have just switched from seeing/not seeing beacons: " + state);
        Log.d(TAG, "Region was " + region.toString());
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

    public int getColorCompat(@ColorRes int color) {
        return ContextCompat.getColor(RingApp.this, color);
    }

}
