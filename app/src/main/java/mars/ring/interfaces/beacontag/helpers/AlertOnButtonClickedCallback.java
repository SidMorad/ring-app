package mars.ring.interfaces.beacontag.helpers;

import mars.ring.domain.model.beacontag.BeaconDTO;

/**
 * Created by developer on 10/01/18.
 */

public interface AlertOnButtonClickedCallback {

    void onPositiveClick(String tagName, Integer categoryIndex, BeaconDTO dto);

    void onNegativeClick(BeaconDTO dto);
}
