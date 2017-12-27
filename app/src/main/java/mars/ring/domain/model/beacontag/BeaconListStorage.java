package mars.ring.domain.model.beacontag;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.AnyThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BeaconListStorage a storage mechanism for a list of BeaconDTOs
 * This stores the instance in a shared preferences file, and provides thread-safe access and mutation.
 *
 * Created by developer on 26/12/17.
 */

public class BeaconListStorage {

    private static final String TAG = BeaconListStorage.class.getSimpleName();
    private static final AtomicReference<WeakReference<BeaconListStorage>> INSTANCE_REF =
            new AtomicReference<>(new WeakReference<>(null));

    private static final String STORE_NAME = "BeaconList0";
    private static final String KEY_STATE = "beacon_list";

    private final SharedPreferences mPrefs;
    private final ReentrantLock mPrefsLock;
    private final AtomicReference<List<BeaconDTO>> mCurrentAuthState;

    @AnyThread
    public static BeaconListStorage getInstance(@NonNull Context context) {
        BeaconListStorage storage = INSTANCE_REF.get().get();
        if (storage == null) {
            storage = new BeaconListStorage(context.getApplicationContext());
            INSTANCE_REF.set(new WeakReference<BeaconListStorage>(storage));
        }
        return storage;
    }

    private BeaconListStorage(Context context) {
        mPrefs = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
        mPrefsLock = new ReentrantLock();
        mCurrentAuthState = new AtomicReference<List<BeaconDTO>>();
    }

    @AnyThread
    @NonNull
    public List<BeaconDTO> getCurrent() {
        if (mCurrentAuthState.get() != null) {
            return mCurrentAuthState.get();
        }

        List<BeaconDTO> list = readState();
        if (mCurrentAuthState.compareAndSet(null, list)) {
            return list;
        } else {
            return mCurrentAuthState.get();
        }

    }

    @AnyThread
    @NonNull
    public List<BeaconDTO> replace(@NonNull List<BeaconDTO> list) {
        Log.i(TAG, "Replacing BeaconList with " + list);
        writeState(list);
        mCurrentAuthState.set(list);
        return list;
    }


    @AnyThread
    @NonNull
    private List<BeaconDTO> readState() {
        mPrefsLock.lock();
        try {
            String currentState = mPrefs.getString(KEY_STATE, null);
            if (currentState == null) {
                return new ArrayList<BeaconDTO>();
            }

            try {
                Type listType = new TypeToken<List<BeaconDTO>>(){}.getType();
                return new Gson().fromJson(currentState, listType);
            } catch (JsonSyntaxException ex) {
                Log.w(TAG, "Failed to deserialize stored BeaconList - discarding");
                return new ArrayList<BeaconDTO>();
            }
        } finally {
            mPrefsLock.unlock();
        }
    }

    @AnyThread
    private void writeState(@Nullable List<BeaconDTO> list) {
        mPrefsLock.lock();
        try {
            SharedPreferences.Editor editor = mPrefs.edit();
            if (list == null) {
                editor.remove(KEY_STATE);
            } else {
                editor.putString(KEY_STATE, new Gson().toJson(list));
            }

            if (!editor.commit()) {
                throw new IllegalStateException("Failed to write BeaconList to shared prefs");
            }
        } finally {
            mPrefsLock.unlock();
        }
    }
}
