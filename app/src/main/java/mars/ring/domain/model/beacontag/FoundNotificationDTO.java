package mars.ring.domain.model.beacontag;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.TimeZone;

import mars.ring.application.util.TextUtil;

/**
 * Created by developer on 05/02/18.
 */

public class FoundNotificationDTO {

    private Double lat;
    private Double lon;
    private Double distance;
    private ZonedDateTime recordedAt;
    private ZonedDateTime receivedAt;
    private String tagName;
    private Category category;

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public Double getDistance() {
        return distance;
    }

    public ZonedDateTime getRecordedAt() {
        return recordedAt;
    }

    public ZonedDateTime getReceivedAt() {
        return receivedAt;
    }

    public String getTagName() {
        return tagName;
    }

    public Category getCategory() {
        return category;
    }

    public String snippet() {
        return TextUtil.format(
            "Recorded distance: %s\nRecord time: %s\nReceive time: %s",
                distance(), recordedAt(), receivedAt());
    }

    public String distance() {
        DecimalFormat df = new DecimalFormat("#.#");
        if (distance > 99.9) {
            df = new DecimalFormat("###");
        }
        df.setRoundingMode(RoundingMode.CEILING);
        return df.format(distance);
    }

    public String recordedAt() {
        return DateTimeFormatter.ofPattern("hh:mm E d MMM").format(recordedAt.withZoneSameInstant(ZoneId.systemDefault()));
    }

    public String receivedAt() {
        return DateTimeFormatter.ofPattern("hh:mm E d MMM").format(receivedAt.withZoneSameInstant(ZoneId.systemDefault()));
    }

}