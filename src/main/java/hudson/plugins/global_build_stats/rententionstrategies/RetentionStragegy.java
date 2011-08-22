package hudson.plugins.global_build_stats.rententionstrategies;

import java.util.ArrayList;
import java.util.List;

/**
 * @author fcamblor
 */
public abstract class RetentionStragegy {

    protected static final List<RetentionStragegy> RETENTION_STRAGEGYS_IMPLEMENTATIONS = new ArrayList<RetentionStragegy>(){{
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
        for(RetentionStragegy strategy : RETENTION_STRAGEGYS_IMPLEMENTATIONS){
            if(strategyId.equals(strategy.getId())){
                return strategy;
            }
        }
        return null;
    }

    public static List<RetentionStragegy> values(){
        return RETENTION_STRAGEGYS_IMPLEMENTATIONS;
    }
}
