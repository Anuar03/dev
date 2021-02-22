package kz.report.dev.utils.collectionutils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionUtils {

    public static <T, E> Map<T, List<E>> groupData(Collection<E> collection, T keyField, T valueField) {
        return new HashMap<>();
    }

}
