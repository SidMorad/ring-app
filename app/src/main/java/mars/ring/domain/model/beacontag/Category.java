package mars.ring.domain.model.beacontag;

import mars.ring.R;

/**
 * Created by developer on 09/01/18.
 */

public enum Category {
  KEYS, WALLET, BAG, ACCESSORIES;

  public static Category fromIndex(Integer index) {
    switch (index) {
      case 0:
        return KEYS;
      case 1:
        return WALLET;
      case 2:
        return BAG;
      case 3:
        return ACCESSORIES;
      default:
        return null;
    }
  }

  public int index() {
    switch (this) {
      case KEYS:
        return 0;
      case WALLET:
        return 1;
      case BAG:
        return 2;
      case ACCESSORIES:
        return 3;
      default:
        return 0;
    }
  }

  public static int res(Category cat) {
    switch (cat) {
      case KEYS:
        return R.drawable.ic_key_balck_24dp;
      case WALLET:
        return R.drawable.ic_wallet_balck_24dp;
      case BAG:
        return R.drawable.ic_bag_balck_24dp;
      case ACCESSORIES:
        return R.drawable.ic_remote_balck_24dp;
      default:
        return R.drawable.ic_key_balck_24dp;
    }
  }


  public static int resWhite(Category cat) {
    switch (cat) {
      case KEYS:
        return R.drawable.ic_key_white_24dp;
      case WALLET:
        return R.drawable.ic_wallet_white_24dp;
      case BAG:
        return R.drawable.ic_bag_white_24dp;
      case ACCESSORIES:
        return R.drawable.ic_remote_white_24dp;
      default:
        return R.drawable.ic_key_white_24dp;
    }
  }

}
