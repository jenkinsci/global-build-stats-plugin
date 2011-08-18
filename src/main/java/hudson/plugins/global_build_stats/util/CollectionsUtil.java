package hudson.plugins.global_build_stats.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author fcamblor
 */
public class CollectionsUtil {

    public static <T> List<T> minus(List<T> initialList, List<T> elementsToRemove){
        List<T> minusedList = new ArrayList<T>(initialList);
        minusedList.removeAll(elementsToRemove);
        return minusedList;
    }
}
