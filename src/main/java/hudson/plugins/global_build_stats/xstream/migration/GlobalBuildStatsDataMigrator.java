package hudson.plugins.global_build_stats.xstream.migration;

/**
 * Migrator from old GlobalBuildStats POJO to later GlobalBuildStats POJO
 * @author fcamblor
 * @param <TFROM>
 * @param <TTO>
 */
public interface GlobalBuildStatsDataMigrator<TFROM extends GlobalBuildStatsPOJO, TTO extends GlobalBuildStatsPOJO> {
	public TTO migrate(TFROM pojo);
}
