package mars.ring.domain.model.beacontag;

import org.threeten.bp.ZonedDateTime;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * BeaconLTCommand stands for Beacon Location and Time
 *
 * This model will be useful for sending and receiving last seen location of a beacon.
 *
 * Created by developer on 15/01/18.
 */
public class BeaconLTDTO {

    private String mac;
    private Double lat;
    private Double lon;
    private Double distance;
    private ZonedDateTime recordTime;
    private ZonedDateTime receivedTime;
    private String tagName;
    private Category category;

    public BeaconLTDTO() { }

    @Override
    public boolean equals(Object other) {
        if (other == null || !(other instanceof BeaconLTDTO)) {
            return false;
        }
        BeaconLTDTO aBeaconLT = (BeaconLTDTO) other;
        return mac != null &&
                aBeaconLT.mac != null &&
                aBeaconLT.mac.equals(mac);
    }

    @Override
    public int hashCode() {
        return mac.hashCode();
    }

    public Double getLonPlusRandom() {
        return lon + randomVerySmallGeoDiff();
    }

    public Double getLonMinesRandom() {
        return lon - randomVerySmallGeoDiff();
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

    public ZonedDateTime getRecordTime() {
        return recordTime;
    }

    public ZonedDateTime getReceivedTime() {
        return receivedTime;
    }

    public String getTagName() {
        return tagName;
    }

    public Category getCategory() {
        return category;
    }

    private double randomVerySmallGeoDiff() {
        double rangeMin = 0.00001;
        double rangeMax = 0.00004;
        return rangeMin + (rangeMax - rangeMin) * new Random().nextDouble();
    }

}
