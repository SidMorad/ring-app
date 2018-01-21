package mars.ring.domain.model.beacontag;

import org.threeten.bp.ZonedDateTime;

import java.util.Date;

/**
 * BeaconLTCommand stands for Beacon Location and Time
 *
 * This model will be useful for sending and receiving last seen location of a beacon.
 *
 * Created by developer on 15/01/18.
 */
public class BeaconLTCommand {

    private String mac;
    private Double lat;
    private Double lon;
    private Double distance;
    private String recordTime;

    public BeaconLTCommand() { }

    public BeaconLTCommand(org.altbeacon.beacon.Beacon beacon, Double latitude, Double longitude) {
        mac = beacon.getBluetoothAddress();
        distance = beacon.getDistance();
        lat = latitude;
        lon = longitude;
        recordTime = ZonedDateTime.now().toString();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof BeaconLTCommand)) {
            return false;
        }
        BeaconLTCommand aBeaconLT = (BeaconLTCommand) other;
        return mac != null &&
                aBeaconLT.mac != null &&
                aBeaconLT.mac.equals(mac);
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }

    public String getMac() {
        return mac;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public Double getDistance() {
        return distance;
    }

    public String getRecordTime() {
        return recordTime;
    }

}
