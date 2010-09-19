package hudson.plugins.global_build_stats.model;

import hudson.util.DataSetBuilder;

public enum YAxisChartDimension {
	BUILD_COUNTER{
		@Override
		public AbstractBuildStatChartDimension createBuildStatChartDimension(
				BuildStatConfiguration config,
				DataSetBuilder<String, DateRange> datasetBuilder) {
			return new AbstractBuildStatChartDimension.BuildCounterChartDimension(config, datasetBuilder);
		}
	},
	BUILD_TOTAL_DURATION{
		@Override
		public AbstractBuildStatChartDimension createBuildStatChartDimension(
				BuildStatConfiguration config,
				DataSetBuilder<String, DateRange> datasetBuilder) {
			return new AbstractBuildStatChartDimension.BuildTotalDurationChartDimension(config, datasetBuilder);
		}
	},
	BUILD_AVERAGE_DURATION{
		@Override
		public AbstractBuildStatChartDimension createBuildStatChartDimension(
				BuildStatConfiguration config,
				DataSetBuilder<String, DateRange> datasetBuilder) {
			return new AbstractBuildStatChartDimension.BuildAverageDurationChartDimension(config, datasetBuilder);
		}
	};
	
	public abstract AbstractBuildStatChartDimension createBuildStatChartDimension(BuildStatConfiguration config, DataSetBuilder<String, DateRange> datasetBuilder);
}
