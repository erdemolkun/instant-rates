package dynoapps.exchange_rates.util;

import java.util.List;

/**
 * Created by erdemmac on 01/12/2016.
 */

public class CollectionUtils {
    public static <E> E getInstance(List<E> list, Class clazz) {
        for (E e : list) {
            if (clazz.isInstance(e)) {
                return e;
            }
        }
        return null;
    }
}
