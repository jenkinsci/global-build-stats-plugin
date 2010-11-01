package hudson.plugins.global_build_stats.xstream.migration.v0;

import hudson.plugins.global_build_stats.xstream.migration.AbstractMigrator;

/**
 * V1 Evolutions :
 * - No more empty BuildStatConfig's jobFilter in data configuration
 * - BuildStatConfiguration id added
 * @author fcamblor
 */
public class InitialMigrator extends AbstractMigrator<V0GlobalBuildStatsPOJO, V0GlobalBuildStatsPOJO> {

	@Override
	protected V0GlobalBuildStatsPOJO createMigratedPojo() {
		return new V0GlobalBuildStatsPOJO();
	}
	
	@Override
	protected boolean registerBuildStatConfigId() {
		return false;
	}
	
	@Override
	public V0GlobalBuildStatsPOJO migrate(V0GlobalBuildStatsPOJO pojo) {
		throw new IllegalAccessError("migrate() method should never be called on InitialMigrator !");
	}
	
}
