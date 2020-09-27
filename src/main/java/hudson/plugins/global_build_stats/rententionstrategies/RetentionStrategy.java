package hudson.plugins.global_build_stats.rententionstrategies;

import hudson.model.Run;
import hudson.plugins.global_build_stats.business.GlobalBuildStatsPluginSaver;
import hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours.BuildCompletedListener;
import hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours.BuildDeletedListener;
import hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours.ParameterizedStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author fcamblor
 */
public abstract class RetentionStrategy<T extends RetentionStrategy> {

    protected static final List<RetentionStrategy> RETENTION_STRATEGIES_IMPLEMENTATIONS = new ArrayList<RetentionStrategy>(){{
        add(new DiscardResultsOlderThanDays());
        add(new DoNotKeepBuildResultWhenDiscarded());
        add(new KeepExistingJobResultsOnly());
    }};

    protected RetentionStrategy(){
    }

    public String getId(){
        return getClass().getName();
    }

    public abstract String getConfigPage();

    public static RetentionStrategy valueOf(String strategyId){
        for(RetentionStrategy strategy : RETENTION_STRATEGIES_IMPLEMENTATIONS){
            if(strategyId.equals(strategy.getId())){
                return strategy;
            }
        }
        return null;
    }

    // Overridable if retention strategy is parameterized
    public void updateState(Map<String, String[]> parameters){
        if(this instanceof ParameterizedStrategy){
            ((ParameterizedStrategy)this).updateStrategyParameters(parameters);
        }
    }
    public void from(T strategyToCopy) {
        if(this instanceof ParameterizedStrategy){
            ((ParameterizedStrategy<T>)this).updateStrategyParameters(strategyToCopy);
        }
    }

    // Overridable if retention strategy is a build deleted listener
    public void onBuildDeleted(Run build, GlobalBuildStatsPluginSaver pluginSaver) {
        if(this instanceof BuildDeletedListener){
            ((BuildDeletedListener)this).buildDeleted(build, pluginSaver);
        }
    }

    // Overridable if retention strategy is a build completed listener
    public void onBuildCompleted(Run build, GlobalBuildStatsPluginSaver pluginSaver) {
        if(this instanceof BuildCompletedListener){
            ((BuildCompletedListener)this).buildCompleted(build, pluginSaver);
        }
    }

    public static List<RetentionStrategy> values(){
        return RETENTION_STRATEGIES_IMPLEMENTATIONS;
    }

    public abstract void strategyActivated(GlobalBuildStatsPluginSaver pluginSaver);
}
