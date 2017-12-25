package mars.ring.application;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v7.app.AppCompatDelegate;

import com.google.common.io.BaseEncoding;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import mars.ring.domain.model.user.AuthRepo;
import mars.ring.domain.model.beacontag.BeaconsRepo;

/**
 * Created by developer on 07/12/17.
 */

public final class RingApp extends android.app.Application {

    public final static int RC_FAIL = 0;
    public final static int RC_AUTH = 100;

    private AuthRepo authRepo;
    private BeaconsRepo beaconsRepo;
    private BackgroundPowerSaver backgroundPowerSaver;


    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

        BeaconManager beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this);
        // iBeacon layout
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(
                "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
//              "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));

        // simply constructing this class and holding a reference to it in Application
        // will automatically cause the BeaconLibrary to save battery whenever tha application
        // is not visable. This reduces bluetooth power usage about 60%
        backgroundPowerSaver = new BackgroundPowerSaver(this);
        // Note: above feature need to be use along with BootstrapNotifier otherwise we see this error log: Cannot contact service to set scan periods

        authRepo = new AuthRepo(this);
        beaconsRepo = new BeaconsRepo(this, authRepo);

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
            PackageInfo packageInof = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if (packageInof == null
                    || packageInof.signatures == null
                    || packageInof.signatures.length == 0
                    || packageInof.signatures[0] == null) {
                return null;
            }
            return signatureDigest(packageInof.signatures[0]);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return getColor(color);
        } else {
            return getResources().getColor(color);
        }
    }

}
