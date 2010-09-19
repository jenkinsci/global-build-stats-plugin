package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.Messages;
import hudson.util.DataSetBuilder;

public enum YAxisChartType {

	COUNT() {
		@Override
		public void provideDataInDataSet(DataSetBuilder<String, DateRange> dsb,
				DateRange range, int nbSuccess, int nbFailures, int nbUnstables, 
				int nbAborted, int nbNotBuild) {

			dsb.add(nbSuccess, Messages.Build_Results_Statuses_SUCCESS(), range);
			dsb.add(nbFailures, Messages.Build_Results_Statuses_FAILURES(), range);
			dsb.add(nbUnstables, Messages.Build_Results_Statuses_UNSTABLES(), range);
			dsb.add(nbAborted, Messages.Build_Results_Statuses_ABORTED(), range);
			dsb.add(nbNotBuild, Messages.Build_Results_Statuses_NOT_BUILD(), range);
		}
		@Override
		public String getLabel() {
			return Messages.YAxis_Chart_Types_Count();
		}
	},
	PERCENTAGE(){
		@Override
		public void provideDataInDataSet(DataSetBuilder<String, DateRange> dsb,
				DateRange range, int nbSuccess, int nbFailures, int nbUnstables, 
				int nbAborted, int nbNotBuild) {
			
			double total = nbSuccess + nbFailures + nbUnstables + nbAborted + nbNotBuild;
			if(total == 0){
				dsb.add(Double.valueOf(0.0), Messages.Build_Results_Statuses_SUCCESS(), range);
				dsb.add(Double.valueOf(0.0), Messages.Build_Results_Statuses_FAILURES(), range);
				dsb.add(Double.valueOf(0.0), Messages.Build_Results_Statuses_UNSTABLES(), range);
				dsb.add(Double.valueOf(0.0), Messages.Build_Results_Statuses_ABORTED(), range);
				dsb.add(Double.valueOf(0.0), Messages.Build_Results_Statuses_NOT_BUILD(), range);
			} else {
				dsb.add(Double.valueOf(nbSuccess*100.0/total), Messages.Build_Results_Statuses_SUCCESS(), range);
				dsb.add(Double.valueOf(nbFailures*100.0/total), Messages.Build_Results_Statuses_FAILURES(), range);
				dsb.add(Double.valueOf(nbUnstables*100.0/total), Messages.Build_Results_Statuses_UNSTABLES(), range);
				dsb.add(Double.valueOf(nbAborted*100.0/total), Messages.Build_Results_Statuses_ABORTED(), range);
				dsb.add(Double.valueOf(nbNotBuild*100.0/total), Messages.Build_Results_Statuses_NOT_BUILD(), range);
			}
		}
		@Override
		public String getLabel() {
			return Messages.YAxis_Chart_Types_Percentage();
		}
	};

	private YAxisChartType(){
	}

	public abstract String getLabel();
	public abstract void provideDataInDataSet(DataSetBuilder<String, DateRange> dsb, DateRange range, int nbSuccess, 
			int nbFailures, int nbUnstables, int nbAborted, int nbNotBuild);
}
