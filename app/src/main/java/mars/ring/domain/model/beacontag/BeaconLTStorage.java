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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BeaconLTStorage a storage mechanism for a set of BeaconLTs
 * This stores the instance in a shared preferences file, and provides thread-safe access and mutation.
 *
 * Created by developer on 15/01/18.
 */

public class BeaconLTStorage {

    private static final String TAG = BeaconLTStorage.class.getSimpleName();
    private static final AtomicReference<WeakReference<BeaconLTStorage>> INSTANCE_REF =
            new AtomicReference<>(new WeakReference<>(null));

    private static final String STORE_NAME = "BeaconLT0";
    private static final String KEY_STATE = "beacon_lt_set";

    private final SharedPreferences mPrefs;
    private final ReentrantLock mPrefsLock;
    private final AtomicReference<Set<BeaconLTCommand>> mCurrentAuthState;

    @AnyThread
    public static BeaconLTStorage getInstance(@NonNull Context context) {
        BeaconLTStorage storage = INSTANCE_REF.get().get();
        if (storage == null) {
            storage = new BeaconLTStorage(context.getApplicationContext());
            INSTANCE_REF.set(new WeakReference<BeaconLTStorage>(storage));
        }
        return storage;
    }

    private BeaconLTStorage(Context context) {
        mPrefs = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE);
        mPrefsLock = new ReentrantLock();
        mCurrentAuthState = new AtomicReference<Set<BeaconLTCommand>>();
    }

    @AnyThread
    @NonNull
    public Set<BeaconLTCommand> getCurrent() {
        if (mCurrentAuthState.get() != null) {
            return mCurrentAuthState.get();
        }

        Set<BeaconLTCommand> set = readState();
        if (mCurrentAuthState.compareAndSet(null, set)) {
            return set;
        } else {
            return mCurrentAuthState.get();
        }

    }

    @AnyThread
    @NonNull
    public Set<BeaconLTCommand> replace(@NonNull Set<BeaconLTCommand> set) {
        Log.i(TAG, "Replacing BeaconLTCommand set with " + set);
        writeState(set);
        mCurrentAuthState.set(set);
        return set;
    }


    @AnyThread
    @NonNull
    private Set<BeaconLTCommand> readState() {
        mPrefsLock.lock();
        try {
            String currentState = mPrefs.getString(KEY_STATE, null);
            if (currentState == null) {
                return new HashSet<BeaconLTCommand>();
            }

            try {
                Type type = new TypeToken<Set<BeaconLTCommand>>(){}.getType();
                return new Gson().fromJson(currentState, type);
            } catch (JsonSyntaxException ex) {
                Log.w(TAG, "Failed to deserialize stored BeaconLTSet - discarding");
                return new HashSet<BeaconLTCommand>();
            }
        } finally {
            mPrefsLock.unlock();
        }
    }

    @AnyThread
    private void writeState(@Nullable Set<BeaconLTCommand> set) {
        mPrefsLock.lock();
        try {
            SharedPreferences.Editor editor = mPrefs.edit();
            if (set == null) {
                editor.remove(KEY_STATE);
            } else {
                editor.putString(KEY_STATE, new Gson().toJson(set));
            }

            if (!editor.commit()) {
                throw new IllegalStateException("Failed to write BeaconLTSet to shared prefs");
            }
        } finally {
            mPrefsLock.unlock();
        }
    }

}
