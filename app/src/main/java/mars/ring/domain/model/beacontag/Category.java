package mars.ring.domain.model.beacontag;

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
}
