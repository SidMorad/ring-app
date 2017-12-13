package mars.ring.interfaces.beacon.registered;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import mars.ring.R;
import mars.ring.application.RingApp;
import mars.ring.domain.model.beacontag.BeaconDTO;
import mars.ring.interfaces.beacon.BeaconModelAdapter;

/**
 * BeaconListActivity that displays list of registered beacons.
 *
 * Created by a developer on 21/11/17.
 */

public class BeaconListActivity extends AppCompatActivity {

    private final BeaconModelAdapter beaconsAdapter = new BeaconModelAdapter();
    public static final int EXPECTED_RESULT_CODE = 2;
    private static final String TAG = "R.BeaconListActivity";
    private RingApp app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registered_beacon_list);

        app = (RingApp) getApplication();

        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(beaconsAdapter);
        TextView tv = (TextView) findViewById(R.id.empty);
        lv.setEmptyView(tv);
        getBeacons();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ", " + data);
        super.onActivityResult(requestCode, resultCode, data);
        if (EXPECTED_RESULT_CODE == requestCode) {
            if (Activity.RESULT_OK == resultCode) {
                AlertDialog.Builder aBuilder = new AlertDialog.Builder(this);
                final EditText input = new EditText(this);
                aBuilder.setView(input);
                aBuilder.setTitle(getString(R.string.name_your_tag));
                aBuilder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String tagName = input.getText().toString();
                        Log.d(TAG, "Entered tagName is " + tagName);
                        createBeaconTag(tagName, data);
                    }
                });
                aBuilder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        dialog.cancel();
                    }
                });
                aBuilder.show();
            }
        }
    }

    private void createBeaconTag(String tagName, Intent data) {
        data.putExtra(BeaconDTO.TAG_NAME, tagName);
        BeaconDTO bModel = new BeaconDTO(data);
        app.getBeaconsRepo().createBeacon(bModel, (ex) -> {
            if (ex == null) {
                getBeacons();
            }
        });
    }

    private void getBeacons() {
        app.getBeaconsRepo().getBeacons((beacons, ex) -> {
            if (ex == null) {
                runOnUiThread(() -> {
                    beaconsAdapter.setAll(beacons);
                    beaconsAdapter.notifyDataSetChanged();
                });
            } else {
                Toast.makeText(this, "Error on getting beacon list", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error on getting beacon list", ex);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.add_new_item == item.getItemId()) {
            startActivityForResult(new Intent(this, mars.ring.interfaces.beacon.discovery.BeaconListActivity.class), EXPECTED_RESULT_CODE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
