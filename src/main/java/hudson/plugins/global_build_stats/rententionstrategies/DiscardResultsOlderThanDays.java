package hudson.plugins.global_build_stats.rententionstrategies;

/**
 * @author fcamblor
 */
public class DiscardResultsOlderThanDays extends RetentionStragegy {

    private int days = 100;

    @Override
    public String getConfigPage() {
        return "discardResultsOlderThanDays.jelly";
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
