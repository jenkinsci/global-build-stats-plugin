package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.Messages;
import hudson.util.DataSetBuilder;
import hudson.util.StackedAreaRenderer2;

import java.awt.Color;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.data.category.CategoryDataset;

import javax.swing.*;

public abstract class AbstractBuildStatChartDimension {
	
	protected BuildStatConfiguration config;
	protected DataSetBuilder<String, DateRange> datasetBuilder;

    private static final LegendItemData[] BUILD_STATUSES_LEGENDITEMS = new LegendItemData[]{
            new LegendItemData(Messages.Build_Results_Item_Legend_Statuses_NOT_BUILD(), new Color(85, 85, 85)),
            new LegendItemData(Messages.Build_Results_Item_Legend_Statuses_FAILURES(), new Color(255, 85, 85)),
            new LegendItemData(Messages.Build_Results_Item_Legend_Statuses_ABORTED(), new Color(255, 85, 255)),
            new LegendItemData(Messages.Build_Results_Item_Legend_Statuses_UNSTABLES(), new Color(255, 255, 85)),
            new LegendItemData(Messages.Build_Results_Item_Legend_Statuses_SUCCESS(), new Color(85, 85, 255))
    };
    private static final LegendItemData TOTAL_BUILD_TIME_LEGENDITEM =
            new LegendItemData(Messages.Build_Results_Total_Build_Time(), new Color(0, 0, 0));
    private static final LegendItemData AVERAGE_BUILD_TIME_LEGENDITEM =
            new LegendItemData(Messages.Build_Results_Average_Build_Time(), new Color(128, 255, 255));

	protected AbstractBuildStatChartDimension(BuildStatConfiguration config, DataSetBuilder<String, DateRange> datasetBuilder){
		this.config = config;
		this.datasetBuilder = datasetBuilder;
	}
	
	public DataSetBuilder<String, DateRange> getDatasetBuilder(){
		return this.datasetBuilder;
	}
	    
	public static class LegendItemData{
		public String label;
		public Color color;
		public LegendItemData(String label, Color color){
			this.label = label;
			this.color = color;
		}
	}
	
	// Useless... for the moment...
	public static List<LegendItemData> getSortedLegendItemsLabels(){
		List<LegendItemData> sortedLegendItemsLabels = new ArrayList<LegendItemData>();
		
		// Build statuses
        for(int i=0; i<BUILD_STATUSES_LEGENDITEMS.length; i++){
            sortedLegendItemsLabels.add(BUILD_STATUSES_LEGENDITEMS[i]);
        }

		// Build durations
		sortedLegendItemsLabels.add(TOTAL_BUILD_TIME_LEGENDITEM);
		sortedLegendItemsLabels.add(AVERAGE_BUILD_TIME_LEGENDITEM);

		return sortedLegendItemsLabels;
	}
	
	public abstract void provideDataInDataSet(DateRange range);
	public abstract void saveDataForBuild(JobBuildResult currentBuild);
	public abstract CategoryItemRenderer getRenderer();
	public abstract ValueAxis getRangeAxis();
	
	public static class BuildCounterChartDimension extends AbstractBuildStatChartDimension{
		private int nbSuccess=0, nbFailures=0, nbUnstables=0, nbAborted=0, nbNotBuild=0;
		public BuildCounterChartDimension(BuildStatConfiguration config, DataSetBuilder<String, DateRange> datasetBuilder){
			super(config, datasetBuilder);
		}
		public void provideDataInDataSet(DateRange range){
			config.getyAxisChartType().provideDataInDataSet(datasetBuilder, range, nbSuccess, nbFailures, nbUnstables, nbAborted, nbNotBuild);
			
			nbSuccess=0; nbFailures=0; nbUnstables=0; nbAborted=0; nbNotBuild=0;
		}
		
		public void saveDataForBuild(JobBuildResult currentBuild){
			nbSuccess += currentBuild.getResult().getSuccessCount();
			nbFailures += currentBuild.getResult().getFailureCount();
			nbUnstables += currentBuild.getResult().getUnstableCount();
			nbAborted += currentBuild.getResult().getAbortedCount();
			nbNotBuild += currentBuild.getResult().getNotBuildCount();
		}
		
		
		public CategoryItemRenderer getRenderer() {
	        // This renderer allows to map area for clicks
	        // + it fixes some rendering bug (0 is displayed on "demi" tick instead of "plain" tick)
	        final StackedAreaRenderer2 renderer = new StackedAreaRenderer2(){
	            @Override
	            public String generateURL(CategoryDataset dataset, int row, int column) {
	                DateRange range = (DateRange) dataset.getColumnKey(column);
	                String status = (String) dataset.getRowKey(row);
	                
	                boolean successShown=Messages.Build_Results_Item_Legend_Statuses_SUCCESS().equals(status);
	                boolean failuresShown=Messages.Build_Results_Item_Legend_Statuses_FAILURES().equals(status);
	                boolean unstablesShown=Messages.Build_Results_Item_Legend_Statuses_UNSTABLES().equals(status);
	                boolean abortedShown=Messages.Build_Results_Item_Legend_Statuses_ABORTED().equals(status);
	                boolean notBuildShown=Messages.Build_Results_Item_Legend_Statuses_NOT_BUILD().equals(status);
	                
	                StringBuilder sb = new StringBuilder()
	                	.append("buildHistory?jobFilter=").append(URLEncoder.encode(config.getBuildFilters().getJobFilter()))
	                	.append("&start=").append(range.getStart().getTimeInMillis())
	                	.append("&end=").append(range.getEnd().getTimeInMillis())
	                	.append("&successShown=").append(successShown)
	                	.append("&failuresShown=").append(failuresShown)
	                	.append("&unstablesShown=").append(unstablesShown)
	                	.append("&abortedShown=").append(abortedShown)
	                	.append("&notBuildShown=").append(notBuildShown);
	                if(config.getBuildFilters().getNodeFilter() != null){
	                	sb.append("&nodeFilter=").append(URLEncoder.encode(config.getBuildFilters().getNodeFilter()));
	                }
	                if(config.getBuildFilters().getLauncherFilter() != null){
	                	sb.append("&launcherFilter=").append(URLEncoder.encode(config.getBuildFilters().getLauncherFilter()));
	                }
	                return sb.toString();
	            }

	/*          TODO: add tooltip  
				@Override
	            public String generateToolTip(CategoryDataset dataset, int row, int column) {
	                NumberOnlyBuildLabel label = (NumberOnlyBuildLabel) dataset.getColumnKey(column);
	                AbstractTestResultAction a = label.build.getAction(AbstractTestResultAction.class);
	                switch (row) {
	                    case 0:
	                        return String.valueOf(Messages.AbstractTestResultAction_fail(a.getFailCount()));
	                    case 1:
	                        return String.valueOf(Messages.AbstractTestResultAction_skip(a.getSkipCount()));
	                    default:
	                        return String.valueOf(Messages.AbstractTestResultAction_test(a.getTotalCount()));
	                }
	            }*/
	        };

            for(int i=0; i<BUILD_STATUSES_LEGENDITEMS.length; i++){
                renderer.setSeriesPaint(i, BUILD_STATUSES_LEGENDITEMS[i].color);
            }

	        return renderer;
		}
		
		public ValueAxis getRangeAxis(){
			return new NumberAxis(config.getyAxisChartType().getLabel());
			
		}
	}

	public static class BuildTotalDurationChartDimension extends AbstractBuildStatChartDimension{
		private long totalBuildDuration=0;
		public BuildTotalDurationChartDimension(BuildStatConfiguration config, DataSetBuilder<String, DateRange> datasetBuilder){
			super(config, datasetBuilder);
		}
		
		public void saveDataForBuild(JobBuildResult currentBuild) {
			if(!currentBuild.isDurationEmpty()){
				totalBuildDuration += currentBuild.getDuration();
			}
		}
		
		public void provideDataInDataSet(DateRange range) {
			datasetBuilder.add(Long.valueOf(totalBuildDuration).doubleValue()/1000.0, Messages.Build_Results_Total_Build_Time(), range);
			totalBuildDuration=0;
		}
		
		public CategoryItemRenderer getRenderer() {
			LineAndShapeRenderer renderer = new LineAndShapeRenderer();
			renderer.setSeriesPaint(0, TOTAL_BUILD_TIME_LEGENDITEM.color);
			return renderer;
		}
		
		public ValueAxis getRangeAxis(){
			return new NumberAxis(Messages.YAxis_Chart_Types_Total_Duration_Time());
		}
	}
	

	public static class BuildAverageDurationChartDimension extends AbstractBuildStatChartDimension{
		private int buildCounter=0;
		private long totalBuildDuration=0;
		public BuildAverageDurationChartDimension(BuildStatConfiguration config, DataSetBuilder<String, DateRange> datasetBuilder){
			super(config, datasetBuilder);
		}
		
		public void saveDataForBuild(JobBuildResult currentBuild) {
			if(!currentBuild.isDurationEmpty()){
				buildCounter++;
				totalBuildDuration += currentBuild.getDuration();
			}
		}
		
		public void provideDataInDataSet(DateRange range) {
			
			datasetBuilder.add(buildCounter==0?0:Long.valueOf(totalBuildDuration).doubleValue()/(buildCounter*1000.0), Messages.Build_Results_Average_Build_Time(), range);
			
			buildCounter=0;
			totalBuildDuration=0;
		}
		
		public CategoryItemRenderer getRenderer() {
			LineAndShapeRenderer renderer = new LineAndShapeRenderer();
			renderer.setSeriesPaint(0, AVERAGE_BUILD_TIME_LEGENDITEM.color);
			return renderer;
		}
		
		public ValueAxis getRangeAxis(){
			return new NumberAxis(Messages.YAxis_Chart_Types_Average_Duration_Time());
			
		}
	}
}
