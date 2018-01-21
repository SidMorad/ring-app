package mars.ring.domain.model.beacontag;

import android.util.Log;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import mars.ring.domain.shared.Entity;

/**
 * Beacon model
 *
 * Created by developer on 23/10/17.
 */

public class Beacon implements Entity<Beacon> {
    public String mac;           // ID of the beacon, in case of android it will be BT MAC address
    public String identifier;    // UUID of beacon
    public Integer major;
    public Integer minor;
    public int txPower;         // reference power
    public int rssi;            // current RSSI
    public int batteryLevel;
    public double distance;
    public long timestamp;      // timestamp when this beacon was last time scanned

    private static final Map<Integer, Beacon> mBeaconMap = new ConcurrentHashMap<Integer, Beacon>();

    public Beacon() {}

    public Beacon(org.altbeacon.beacon.Beacon b) {
        mac = b.getBluetoothAddress();
        identifier = b.getId1().toString();
        major = b.getId2().toInt();
        minor = b.getId3().toInt();
        txPower = b.getTxPower();
        rssi = b.getRssi();
//      batteryLevel = (int) (long) b.getDataFields().get(0);
        distance = b.getDistance();
        timestamp = new Date().getTime();
    }

    public static ArrayList<Beacon> toList(Collection<org.altbeacon.beacon.Beacon> beacons) {
        ArrayList<Beacon> result = new ArrayList<Beacon>();
        for (org.altbeacon.beacon.Beacon b : beacons) {
            mBeaconMap.put(toModel(b).hashCode(), toModel(b));
        }
        removeOutdatedBeacons();
        for (Beacon b: mBeaconMap.values()) {
            result.add(b);
        }
        Collections.sort(result, new Comparator<Beacon>() {
            @Override
            public int compare(Beacon b1, Beacon b2) {
                return Double.compare(b1.distance, b2.distance);
            }
        });
        return result;
    }

    public static Beacon toModel(org.altbeacon.beacon.Beacon beacon) {
        return new Beacon(beacon);
    }

    public String distance() {
        if (distance == 0) {
            return "-.-";
        }
        DecimalFormat df = new DecimalFormat("#.#");
        if (distance > 99.9) {
            df = new DecimalFormat("###");
        }
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(distance);
    }

    public static void clearBeaconMap() {
        mBeaconMap.clear();
    }

    private static final long BEACON_LIFE_DURATION = 4000; // 4 seconds

    private static boolean removeOutdatedBeacons() {
        boolean anythingChanged = false;

        final long oldestTimestampAllowed = new Date().getTime() - BEACON_LIFE_DURATION;
        for (Beacon beacon: mBeaconMap.values()) {
            if (beacon.timestamp < oldestTimestampAllowed) {
                mBeaconMap.remove(beacon.hashCode());
                anythingChanged = true;
            }
        }
        Log.d(TAG,"All beacon validated, anythingChanged: " + anythingChanged);
        return anythingChanged;
    }

    @Override
    public boolean sameIdentityAs(Beacon other) {
        return this.mac != null &&
               other.mac != null &&
               other.mac.equals(this.mac);
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof Beacon)) {
            return false;
        }
        Beacon aBeacon = (Beacon) other;
        return sameIdentityAs(aBeacon);
    }

    public int identifierHashCode() {
        return Math.abs(hashCode());
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }

    private final static String TAG = Beacon.class.getSimpleName() + "1";
}
