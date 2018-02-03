package mars.ring.domain.model.beacontag;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.common.hash.HashCode;

import java.util.Comparator;

/**
 * Created by developer on 12/12/17.
 */

public class BeaconDTO implements Comparable<BeaconDTO> {
    public static final String TAG_NAME = "tagName";
    public static final String CATEGORY = "category";
    public static final String IDENTIFIER = "identifier";
    public static final String MAJOR = "major";
    public static final String MINOR = "minor";
    public static final String MAC = "mac";
    public static final String BATTERY_LEVEL = "batteryLevel";
    public static final String TX_POWER = "txPower";
    public static final String LOST = "lost";

    private Long id;
    private String tagName;
    private Category category;
    private String identifier;
    private Integer major;
    private Integer minor;
    private String mac;
    private Boolean lost;

    public BeaconDTO() { }

    public BeaconDTO(Intent data) {
        this.tagName = data.getStringExtra(TAG_NAME);
        this.identifier = data.getStringExtra(IDENTIFIER);
        this.major = data.getIntExtra(MAJOR, 0);
        this.minor = data.getIntExtra(MINOR, 0);
        this.mac = data.getStringExtra(MAC);
        this.category = Category.fromIndex(data.getIntExtra(CATEGORY, 0));
        this.lost = data.getBooleanExtra(LOST, false);
    }

    public int identifierHashCode() {
        return Math.abs((mac + identifier + major + minor).hashCode());
    }

    public boolean isMissing() {
        return lost != null && lost;
    }

    @Override
    public int compareTo(@NonNull BeaconDTO beaconDTO) {
        return 0;
    }

    public static class Comparators {
        public static Comparator<BeaconDTO> ID = new Comparator<BeaconDTO>() {
            @Override
            public int compare(BeaconDTO b1, BeaconDTO b2) {
                if (b1.getId() < b2.getId()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        };
    }

    public int hashCode() {
        return mac.hashCode();
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

    public Category getCategory() {
        return category;
    }

    public Boolean getLost() {
        return lost;
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

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setLost(Boolean lost) {
        this.lost = lost;
    }

}