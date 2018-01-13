package mars.ring.interfaces.beacontag;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import mars.ring.BuildConfig;
import mars.ring.domain.model.beacontag.Beacon;

/**
 * Beacons Adapter
 *
 * Created by developer on 23/10/17.
 */

public class BeaconsAdapter extends BaseAdapter {

    private final ArrayList<Beacon> mBeacons = new ArrayList<Beacon>();

    @Override
    public int getCount() {
        return mBeacons.size();
    }

    @Override
    public Beacon getItem(int position) {
        return mBeacons.get(position);
    }

    public void addNewBeacon(final Beacon beacon) {
        mBeacons.add(beacon);
        notifyDataSetChanged();
    }

    public void clear() {
        mBeacons.clear();
    }

    public void setAll(ArrayList<Beacon> beaconList) {
        clear();
        mBeacons.addAll(beaconList);
    }

    public Beacon findBeaconWithId(final String id) {
        for (final Beacon beacon: mBeacons) {
            if (beacon.mac.equals(id)) return beacon;
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), android.R.layout.two_line_list_item, null);
        }
        ViewHolder holder = (ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder.updateAccordingToBeacon(getItem(position));
        return convertView;
    }

    private class ViewHolder {
        public TextView text1;
        public TextView text2;
        public ViewHolder(final View target) {
            text1 = (TextView) target.findViewById(android.R.id.text1);
            text2 = (TextView) target.findViewById(android.R.id.text2);
            text1.setTextSize(120);
        }
        public void updateAccordingToBeacon(final Beacon beacon) {
            text1.setText(beacon.distance());
            String secondLine = String.format(
                    "Identifier: %d, rssi: %d",
                    beacon.identifierHashCode(),
                    beacon.rssi);
            if (BuildConfig.DEBUG) {
                secondLine += String.format("\n Mac: %s \n Id1: %s \n Major: %d \n Minor: %d \n TxPower: %d \n",
                        beacon.mac, beacon.identifier, beacon.major, beacon.minor, beacon.txPower);
            }
            text2.setText(secondLine);
        }
    }

    private final static String TAG = "BA";
}
