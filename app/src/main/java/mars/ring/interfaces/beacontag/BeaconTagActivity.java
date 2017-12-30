package mars.ring.interfaces.beacontag;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import mars.ring.R;
import mars.ring.application.RingApp;
import mars.ring.domain.model.beacontag.BeaconDTO;
import mars.ring.domain.model.beacontag.BeaconListStorage;
import mars.ring.interfaces.beacontag.discovery.ShowOneActivity;

/**
 * BeaconTagActivity that displays list of registered beacons.
 *
 * Created by a developer on 21/11/17.
 */

public class BeaconTagActivity extends AppCompatActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener {

    private final BeaconModelAdapter beaconsAdapter = new BeaconModelAdapter();
    public static final int EXPECTED_RESULT_CODE = 2;
    private static final String TAG = BeaconTagActivity.class.getSimpleName();
    private RingApp app;
    private BeaconListStorage beaconListStorage;
    private Button retryButton;
    private Menu mMenu;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.beacon_tag_activity);

        app = (RingApp) getApplication();
        beaconListStorage = BeaconListStorage.getInstance(this);
        beaconsAdapter.setAll(beaconListStorage.getCurrent());

        ListView lv = (ListView) findViewById(R.id.list_view);
        lv.setAdapter(beaconsAdapter);
        TextView tv = (TextView) findViewById(R.id.empty);
        lv.setEmptyView(tv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(BeaconTagActivity.this, ShowOneActivity.class);
                intent.putExtra(BeaconDTO.IDENTIFIER, beaconsAdapter.getItem(i).getIdentifier());
                intent.putExtra(BeaconDTO.MAJOR, beaconsAdapter.getItem(i).getMajor());
                intent.putExtra(BeaconDTO.MINOR, beaconsAdapter.getItem(i).getMinor());
                intent.putExtra(BeaconDTO.TAG_NAME, beaconsAdapter.getItem(i).getTagName());
                startActivity(intent);
            }
        });
        retryButton = (Button) findViewById(R.id.retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getBeacons();
            }
        });
        hideRetryButton();
        if (RingApp.isOnline()) {
            getBeacons();
        }

        BottomNavigationView tabView = (BottomNavigationView) findViewById(R.id.navigation);
        tabView.setOnNavigationItemSelectedListener(this);
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
            } else {
                Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getBeacons() {
        hideRetryButton();
        app.getBeaconsRepo().getBeacons((beacons, ex) -> {
            if (ex == null) {
                runOnUiThread(() -> {
                    beaconsAdapter.setAll(beacons);
                    beaconsAdapter.notifyDataSetChanged();
                });
                beaconListStorage.replace((ArrayList<BeaconDTO>) beacons);
            } else {
                Toast.makeText(this, "Error on getting tag list", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error on getting beacon list", ex);
                showRetryButton();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        this.mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.add_new_item == item.getItemId()) {
            if (app.isNetworkAvailable()) {
                startActivityForResult(new Intent(this, mars.ring.interfaces.beacontag.discovery.BeaconListActivity.class), EXPECTED_RESULT_CODE);
            } else {
                Toast.makeText(this, getString(R.string.no_internet_access), Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.navigation_beacon_list:
                showList();
                return true;
            case R.id.navigation_beacon_map:
                showMap();
                return true;
            case R.id.navigation_notifications:
                showNotifications();
                return true;
        }
        return false;
    }

    public void showList() {
        setTitle(getString(R.string.my_tags));
        getMenuInflater().inflate(R.menu.menu, mMenu);
    }

    public void showMap() {
        setTitle(getString(R.string.tag_locations));
        mMenu.clear();
    }

    public void showNotifications() {
        setTitle(getString(R.string.tag_notifications));
        mMenu.clear();
    }

    private void hideRetryButton() {
        retryButton.setVisibility(View.INVISIBLE);
    }

    private void showRetryButton() {
        retryButton.setVisibility(View.VISIBLE);
    }

}
