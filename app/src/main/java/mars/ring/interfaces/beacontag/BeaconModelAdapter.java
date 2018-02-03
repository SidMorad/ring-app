package mars.ring.interfaces.beacontag;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mars.ring.R;
import mars.ring.domain.model.beacontag.BeaconDTO;

/**
 * Created by developer on 12/12/17.
 */
public class BeaconModelAdapter extends BaseAdapter {

    List<BeaconDTO> mBeacons = new ArrayList<BeaconDTO>();
    BeaconTagActivity activityInstance;

    BeaconModelAdapter(BeaconTagActivity beaconTagActivity) {
        this.activityInstance = beaconTagActivity;
    }

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
            convertView = View.inflate(parent.getContext(), R.layout.beacon_tag_list_item, null);
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
        public ImageButton icon1;
        public ImageButton edit;
        public ImageButton meter;
        public ImageButton missing;
        public ViewHolder(final View target) {
            text1 = (TextView) target.findViewById(android.R.id.text1);
            icon1 = (ImageButton) target.findViewById(R.id.icon1);
            edit = (ImageButton) target.findViewById(R.id.edit_button);
            meter = (ImageButton) target.findViewById(R.id.meter_button);
            missing = (ImageButton) target.findViewById(R.id.missing_button);

            edit.setImageResource(R.drawable.ic_pencil_balck_24dp);
            edit.setBackgroundResource(R.drawable.border_default);
            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Edit button clicked!" + view.getTag());
                    activityInstance.openEditBeaconDialog((BeaconDTO) view.getTag());
                }
            });
            meter.setImageResource(R.drawable.ic_altimeter_balck_24dp);
            meter.setBackgroundResource(R.drawable.border_primary);
            meter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Meter button clicked!" + view.getTag());
                    activityInstance.goToShowOneActivity((BeaconDTO) view.getTag());
                }
            });
            missing.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "Missing button clicked!" + view.getTag());
                    activityInstance.openReportLostBeaconDialog((BeaconDTO) view.getTag());
                }
            });
        }

        public void updateAccordingToBeacon(final BeaconDTO beacon) {

            text1.setText(beacon.getTagName());
            edit.setTag(beacon);
            meter.setTag(beacon);
            missing.setTag(beacon);
            if (beacon.isMissing()) {
                missing.setImageResource(R.drawable.ic_minus_black_24dp);
                missing.setBackgroundResource(R.drawable.border_danger);
            } else {
                missing.setImageResource(R.drawable.ic_check_black_24dp);
                missing.setBackgroundResource(R.drawable.border_secondary);
            }
            if (beacon.getCategory() != null) {
                switch(beacon.getCategory()) {
                    case KEYS:
                        icon1.setBackgroundResource(R.drawable.ic_key_balck_24dp);
                        return;
                    case WALLET:
                        icon1.setBackgroundResource(R.drawable.ic_wallet_balck_24dp);
                        return;
                    case BAG:
                        icon1.setBackgroundResource(R.drawable.ic_bag_balck_24dp);
                        return;
                    case ACCESSORIES:
                        icon1.setBackgroundResource(R.drawable.ic_remote_balck_24dp);
                        return;
                    default:
                        icon1.setBackgroundResource(R.drawable.ic_key_balck_24dp);
                        return;
                }
            }
        }
    }

    private final static String TAG = BeaconModelAdapter.class.getSimpleName() + "1";

}
