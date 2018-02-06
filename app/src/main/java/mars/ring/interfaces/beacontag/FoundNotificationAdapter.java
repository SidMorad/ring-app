package mars.ring.interfaces.beacontag;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import mars.ring.R;
import mars.ring.domain.model.beacontag.FoundNotificationDTO;

/**
 * Created by developer on 05/02/18.
 */

public class FoundNotificationAdapter extends BaseAdapter {

    List<FoundNotificationDTO> notifications = new ArrayList<>();
    BeaconTagActivity activityInstance;

    FoundNotificationAdapter(BeaconTagActivity activity) {
        this.activityInstance = activity;
    }

    @Override
    public int getCount() {
        return notifications.size();
    }

    @Override
    public FoundNotificationDTO getItem(int i) {
        return notifications.get(i);
    }

    public void clear() {
        notifications.clear();
    }

    public void setAll(List<FoundNotificationDTO> notes) {
        clear();
        notifications = notes;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.found_notification_list_item, null);
        }
        FoundNotificationAdapter.ViewHolder holder = (FoundNotificationAdapter.ViewHolder) convertView.getTag();
        if (holder == null) {
            holder = new FoundNotificationAdapter.ViewHolder(convertView);
            convertView.setTag(holder);
        }
        holder.updateRow(getItem(position));
        return convertView;
    }

    private class ViewHolder {
        ImageButton icon1;
        TextView text1;
        TextView noteText;
        ImageButton showInTheMap;

        public ViewHolder(final View target) {
            icon1 = (ImageButton) target.findViewById(R.id.icon1);
            text1 = (TextView) target.findViewById(R.id.text1);
            noteText = (TextView) target.findViewById(R.id.note_text);
            showInTheMap = (ImageButton) target.findViewById(R.id.show_in_the_map);

            showInTheMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    activityInstance.showNoteOnTheMap((FoundNotificationDTO) view.getTag());
                }
            });
        }

        public void updateRow(final FoundNotificationDTO note) {
            text1.setText(note.getTagName());
            String date = DateTimeFormatter.ofPattern("hh:mm E d MMM yyyy").format(note.getRecordedAt().withZoneSameInstant(ZoneId.systemDefault()));
            noteText.setText(String.format("was seen here at %s", date));
            showInTheMap.setTag(note);

            if (note.getCategory() != null) {
                switch(note.getCategory()) {
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

}