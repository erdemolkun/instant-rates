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

    public static <E> int size(List<E> list) {
        return list == null ? 0 : list.size();
    }

    public static <E> int size(E[] array) {
        return array == null ? 0 : array.length;
    }

    public static <E> boolean isNullOrEmpty(List<E> list) {
        return list == null || list.size() < 1;
    }

    public static <E> boolean isNullOrEmpty(E[] array) {
        return array == null || array.length < 1;
    }

}
