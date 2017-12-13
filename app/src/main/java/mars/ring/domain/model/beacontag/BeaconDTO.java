package mars.ring.domain.model.beacontag;

import android.content.Intent;

/**
 * Created by developer on 12/12/17.
 */

public class BeaconDTO {
    public static final String TAG_NAME = "tagName";
    public static final String IDENTIFIER = "identifier";
    public static final String MAJOR = "major";
    public static final String MINOR = "minor";
    public static final String MAC = "mac";
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String TX_POWER = "txPower";

    private Long id;
    private String tagName;
    private String identifier;
    private Integer major;
    private Integer minor;
    private String mac;
    private Integer batteryLevel;
    private Integer txPower;

    public BeaconDTO() { }

    public BeaconDTO(Intent data) {
        this.tagName = data.getStringExtra(TAG_NAME);
        this.identifier = data.getStringExtra(IDENTIFIER);
        this.major = data.getIntExtra(MAJOR, 0);
        this.minor = data.getIntExtra(MINOR, 0);
        this.mac = data.getStringExtra(MAC);
        this.batteryLevel = data.getIntExtra(BATTERY_LEVEL, 100);
        this.txPower = data.getIntExtra(TX_POWER, 0);
    }

    public Long getId() {
        return id;
    }

    public String getTagName() {
        return tagName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Integer getMajor() {
        return major;
    }

    public Integer getMinor() {
        return minor;
    }

    public String getMac() {
        return mac;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public Integer getTxPower() {
        return txPower;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public void setTxPower(Integer txPower) {
        this.txPower = txPower;
    }

}
