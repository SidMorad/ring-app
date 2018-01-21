package mars.ring.application.util;

import android.util.Log;

/**
 * Created by developer on 20/01/18.
 */

public class GenericCallbackImpl implements GenericCallback {
    @Override
    public void onSuccess() {
        Log.d(TAG, "onSuccess called!");
    }

    @Override
    public void onFailure(Exception e) {
        Log.d(TAG, "onFailure called!", e);
    }

    private final String TAG = GenericCallbackImpl.class.getSimpleName() + "1";

}
