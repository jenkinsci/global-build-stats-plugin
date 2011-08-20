package hudson.plugins.global_build_stats.util;

import java.util.*;

/**
 * @author fcamblor
 */
public class CollectionsUtil {

    public static <T> List<T> minus(List<T> initialList, List<T> elementsToRemove){
        List<T> minusedList = new ArrayList<T>(initialList);
        minusedList.removeAll(elementsToRemove);
        return minusedList;
    }

    public static <T> Set<T> toSet(List<T> list){
        Set<T> set = new HashSet<T>();
        for(T t : list){
            if(!set.contains(t)){
                set.add(t);
            }
        }
        return set;
    }

    public static <T,U> void mapMergeAdd(Map<T,List<U>> map, Map<T,List<U>> mapToAdd){
        for(Map.Entry<T,List<U>> e : mapToAdd.entrySet()){
            if(!map.containsKey(e.getKey())){
                map.put(e.getKey(), new ArrayList<U>());
            }
            map.get(e.getKey()).addAll(e.getValue());
        }
    }

    public static <T,U> void mapMergeRemove(Map<T,List<U>> map, Map<T,List<U>> mapToRemove){
        for(Map.Entry<T,List<U>> e : mapToRemove.entrySet()){
            if(map.containsKey(e.getKey())){
                map.get(e.getKey()).removeAll(e.getValue());
                if(map.get(e.getKey()).isEmpty()){
                    map.remove(e.getKey());
                }
            }
        }
    }
}
