package hudson.plugins.global_build_stats.rententionstrategies;

import java.util.Map;

/**
 * @author fcamblor
 */
public class DiscardResultsOlderThanDays extends RetentionStragegy<DiscardResultsOlderThanDays> {

    private int days = 365;

    @Override
    public String getConfigPage() {
        return "discardResultsOlderThanDays.jelly";
    }

    @Override
    public void updateState(Map<String, String[]> parameters) {
        this.days = Integer.valueOf(parameters.get("discardResultsOlderThanDays")[0]);
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    @Override
    public void from(DiscardResultsOlderThanDays otherStrategyToCopy) {
        super.from(otherStrategyToCopy);
        this.days = otherStrategyToCopy.days;
    }
}
