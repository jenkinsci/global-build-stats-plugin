package hudson.plugins.global_build_stats.rententionstrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fcamblor
 */
public abstract class RetentionStragegy<T extends RetentionStragegy> {

    protected static final List<RetentionStragegy> RETENTION_STRATEGIES_IMPLEMENTATIONS = new ArrayList<RetentionStragegy>(){{
        add(new DiscardResultsOlderThanDays());
        add(new DoNotKeepBuildResultWhenDiscarded());
    }};

    protected RetentionStragegy(){
    }

    public String getId(){
        return getClass().getName();
    }

    public abstract String getConfigPage();

    public static RetentionStragegy valueOf(String strategyId){
        for(RetentionStragegy strategy : RETENTION_STRATEGIES_IMPLEMENTATIONS){
            if(strategyId.equals(strategy.getId())){
                return strategy;
            }
        }
        return null;
    }

    // Overridable if retention strategy is parameterized
    public void updateState(Map<String, String[]> parameters){}

    public static List<RetentionStragegy> values(){
        return RETENTION_STRATEGIES_IMPLEMENTATIONS;
    }

    public void from(T strategyToCopy) {}
}
