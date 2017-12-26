package mars.ring.interfaces.beacontag;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mars.ring.domain.model.beacontag.BeaconDTO;

/**
 * Created by developer on 12/12/17.
 */

public class BeaconModelAdapter extends BaseAdapter {

    List<BeaconDTO> mBeacons = new ArrayList<BeaconDTO>();

    @Override
    public int getCount() {
        return mBeacons.size();
    }

    @Override
    public BeaconDTO getItem(int position) {
        return mBeacons.get(position);
    }

    public void clear() {
        mBeacons.clear();
    }

    public void setAll(List<BeaconDTO> beaconList) {
        clear();
        mBeacons = beaconList;
    }

    public BeaconDTO findBeaconWithId(final String id) {
        for (final BeaconDTO beacon: mBeacons) {
            if (beacon.getMac().equals(id)) return beacon;
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
        BeaconModelAdapter.ViewHolder holder = (BeaconModelAdapter.ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new BeaconModelAdapter.ViewHolder(convertView);
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
        }
        public void updateAccordingToBeacon(final BeaconDTO beacon) {
            text1.setText(beacon.getTagName());
            String secondLine = String.format(
                    "Tag Identifier: %s",
                    beacon.identifierHashCode());
            text2.setText(secondLine);
        }
    }

}
