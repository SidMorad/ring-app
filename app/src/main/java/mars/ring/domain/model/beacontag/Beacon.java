package mars.ring.domain.model.beacontag;

import android.bluetooth.BluetoothDevice;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

/**
 * Beacon model
 *
 * Created by developer on 23/10/17.
 */

public class Beacon {
    public String identifier;         // UUID of beacon
    public Integer major;
    public Integer minor;
    public int txPower;         // reference power
    public int rssi;            // current RSSI
    public int batteryLevel;
    public double distance;
    @Deprecated
    public String arguments;    // string representing arguments inside AltBeacon

    public long timestamp;      // timestamp when this beacon was last time scanned
    public String mac;           // ID of the beacon, in case of android it will be BT MAC address

    private static final int PROTOCOL_OFFSET = 3;
    private static final int AD_LENGTH_INDEX = PROTOCOL_OFFSET;
    private static final int AD_TYPE_INDEX = 1 + PROTOCOL_OFFSET;
    private static final int BEACON_CODE_INDEX = 4 + PROTOCOL_OFFSET;
    private static final int UUID_START_INDEX = 6 + PROTOCOL_OFFSET;
    private static final int UUID_STOP_INDEX  = UUID_START_INDEX + 15;
    private static final int ARGS_START_INDEX = UUID_STOP_INDEX + 1;
    private static final int TXPOWER_INDEX = ARGS_START_INDEX + 4;

    private static final int AD_LENGTH_VALUE = 0x1b;
    private static final int AD_TYPE_VALUE = 0xff;
    private static final int BEACON_CODE_VALUE = 0xbeac;

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
    }

    public Beacon(Beacon other) {
        mac = other.mac;
        identifier = other.identifier;
        major = other.major;
        minor = other.minor;
        txPower = other.txPower;
        rssi = other.rssi;
        distance = 0;
    }

    public static ArrayList<Beacon> toList(Collection<org.altbeacon.beacon.Beacon> beacons) {
        ArrayList<Beacon> result = new ArrayList<Beacon>();
        for (org.altbeacon.beacon.Beacon b : beacons) {
            result.add(new Beacon(b));
        }
        return result;
    }

    public static Beacon toModel(org.altbeacon.beacon.Beacon beacon) {
        return new Beacon(beacon);
    }

    public static boolean isAltBeacon(final byte[] data) {
        if ((data[AD_LENGTH_INDEX] & 0xff) != AD_LENGTH_VALUE) return false;

        if ((data[AD_TYPE_INDEX] & 0xff) != AD_TYPE_VALUE) return false;

        final int code = ((data[BEACON_CODE_INDEX] << 8) & 0x0000ff00) | ((data[BEACON_CODE_INDEX + 1]) & 0x000000ff);
        if(code != BEACON_CODE_VALUE) return false;

        return true;
    }

    public String distance() {
        if (distance == 0) {
            return "-.-";
        }
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(distance);
    }

    public int identifierHashCode() {
        return Math.abs((mac + identifier + major + minor).hashCode());
    }

    @Deprecated // in in favour of using Beacon Library
    public void updateFrom(final BluetoothDevice device,
                           final int rssi,
                           final byte[] advertisement) {
        this.rssi = rssi;
        this.mac = device.getAddress();
        this.timestamp = new Date().getTime();
        this.txPower = (int) advertisement[TXPOWER_INDEX];
        this.arguments = String.format("arg1: %02x %02x  arg2: %02x %02x",
                advertisement[ARGS_START_INDEX],
                advertisement[ARGS_START_INDEX + 1],
                advertisement[ARGS_START_INDEX + 2],
                advertisement[ARGS_START_INDEX + 3]);

        StringBuilder sb = new StringBuilder();
        for(int i = UUID_START_INDEX, offset = 0; i <= UUID_STOP_INDEX; ++i, ++offset) {
            sb.append(String.format("%02x", (int)(advertisement[i] & 0xff)));
            if (offset == 3 || offset == 5 || offset == 7 || offset == 9) {
                sb.append("-");
            }
        }
        this.identifier = sb.toString();
    }

}
