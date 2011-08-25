package hudson.plugins.global_build_stats.rententionstrategies.strategybehaviours;

import hudson.plugins.global_build_stats.rententionstrategies.RetentionStragegy;

import java.util.Map;

/**
 * @author fcamblor
 */
public interface ParameterizedStrategy<T extends RetentionStragegy> {
    public void updateStrategyParameters(Map<String, String[]> parameters);
    public void updateStrategyParameters(T strategyToCopy);
}
