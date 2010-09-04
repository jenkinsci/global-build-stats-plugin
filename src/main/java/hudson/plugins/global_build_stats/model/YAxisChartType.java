package hudson.plugins.global_build_stats.model;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.TickUnitSource;

import hudson.plugins.global_build_stats.BuildResultStatusesConstants;
import hudson.util.DataSetBuilder;

public enum YAxisChartType {

	COUNT("Count") {
		@Override
		public void provideDataInDataSet(DataSetBuilder<String, DateRange> dsb,
				DateRange range, int nbSuccess, int nbFailures, int nbUnstables, 
				int nbAborted, int nbNotBuild) {

			dsb.add(nbSuccess, BuildResultStatusesConstants.SUCCESS, range);
			dsb.add(nbFailures, BuildResultStatusesConstants.FAILURES, range);
			dsb.add(nbUnstables, BuildResultStatusesConstants.UNSTABLES, range);
			dsb.add(nbAborted, BuildResultStatusesConstants.ABORTED, range);
			dsb.add(nbNotBuild, BuildResultStatusesConstants.NOT_BUILD, range);
		}
	},
	PERCENTAGE("Percentage"){
		@Override
		public void provideDataInDataSet(DataSetBuilder<String, DateRange> dsb,
				DateRange range, int nbSuccess, int nbFailures, int nbUnstables, 
				int nbAborted, int nbNotBuild) {
			
			double total = nbSuccess + nbFailures + nbUnstables + nbAborted + nbNotBuild;
			if(total == 0){
				dsb.add(Double.valueOf(0.0), BuildResultStatusesConstants.SUCCESS, range);
				dsb.add(Double.valueOf(0.0), BuildResultStatusesConstants.FAILURES, range);
				dsb.add(Double.valueOf(0.0), BuildResultStatusesConstants.UNSTABLES, range);
				dsb.add(Double.valueOf(0.0), BuildResultStatusesConstants.ABORTED, range);
				dsb.add(Double.valueOf(0.0), BuildResultStatusesConstants.NOT_BUILD, range);
			} else {
				dsb.add(Double.valueOf(nbSuccess*100.0/total), BuildResultStatusesConstants.SUCCESS, range);
				dsb.add(Double.valueOf(nbFailures*100.0/total), BuildResultStatusesConstants.FAILURES, range);
				dsb.add(Double.valueOf(nbUnstables*100.0/total), BuildResultStatusesConstants.UNSTABLES, range);
				dsb.add(Double.valueOf(nbAborted*100.0/total), BuildResultStatusesConstants.ABORTED, range);
				dsb.add(Double.valueOf(nbNotBuild*100.0/total), BuildResultStatusesConstants.NOT_BUILD, range);
			}
		}
	};

	private String label;
	
	private YAxisChartType(String label){
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
	
	public abstract void provideDataInDataSet(DataSetBuilder<String, DateRange> dsb, DateRange range, int nbSuccess, 
			int nbFailures, int nbUnstables, int nbAborted, int nbNotBuild);
}
