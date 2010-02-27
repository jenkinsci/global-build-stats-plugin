package hudson.plugins.global_build_stats;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Hudson;
import hudson.model.ManagementLink;
import hudson.model.TaskListener;
import hudson.model.TopLevelItem;
import hudson.model.listeners.RunListener;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.plugins.global_build_stats.model.HistoricScale;
import hudson.plugins.global_build_stats.model.JobBuildResult;
import hudson.util.ChartUtil;
import hudson.util.DataSetBuilder;
import hudson.util.ShiftedCategoryAxis;
import hudson.util.StackedAreaRenderer2;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * Entry point of the global build stats plugin
 * 
 * @author fcamblor
 * @plugin
 */
public class GlobalBuildStatsPlugin extends Plugin{

	private List<JobBuildResult> jobBuildResults = new ArrayList<JobBuildResult>();
	private List<BuildStatConfiguration> buildStatConfigs = new ArrayList<BuildStatConfiguration>();

	@Override
	public void postInitialize() throws Exception {
		super.postInitialize();
		
		// Reload plugin informations
		this.load();
	}
	
    @Extension
    public static class GlobalBuildStatsManagementLink extends ManagementLink {

        public String getIconFileName() {
            return "/plugin/global-build-stats/icons/global-build-stats.png";
        }

        public String getDisplayName() {
            return "Global Builds Stats";
        }

        public String getUrlName() {
            return "plugin/global-build-stats/";
        }

        @Override public String getDescription() {
            return "Displays stats about daily build failures";
        }
    }
    
    @Extension
    public static class GlobalBuildStatsRunListener extends RunListener<AbstractBuild>{
    	public GlobalBuildStatsRunListener() {
    		super(AbstractBuild.class);
		}
    	
    	@Override
    	public void onCompleted(AbstractBuild r, TaskListener listener) {
    		super.onCompleted(r, listener);
    		
    		GlobalBuildStatsPlugin plugin = Hudson.getInstance().getPlugin(GlobalBuildStatsPlugin.class);
    		plugin.addBuild(r);
    		try {
				plugin.save();
			} catch (IOException e) {
			}
    	}
    }
    
    public void doRecordBuildInfos(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
        jobBuildResults.clear();
        
        //TODO fix MatrixProject and use getAllJobs()
        for (TopLevelItem item : Hudson.getInstance().getItems()) {
            if (item instanceof AbstractProject) {
            	addBuildsFrom((AbstractProject) item);
            }
        }

    	save();
    	
        res.forwardToPreviousPage(req);
    }
    
    public void doCreateChart(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	BuildStatConfiguration config = createBuildStatConfig(req);
    	List<JobBuildResult> filteredJobBuildResults = createFilteredAndSortedBuildResults(config);
        DataSetBuilder<String, DateRange> dsb = createDataSetBuilder(filteredJobBuildResults, config);
    	
        ChartUtil.generateGraph(req, res, createChart(req, dsb.build(), config.getBuildStatTitle()), 
        		config.getBuildStatWidth(), config.getBuildStatHeight());
    }
    
    public void doUpdateBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	this.buildStatConfigs.set(Integer.parseInt(req.getParameter("buildStatId")), createBuildStatConfig(req));
    	save();
        res.forwardToPreviousPage(req);
    }
    
    public void doAddBuildStatConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	this.buildStatConfigs.add(createBuildStatConfig(req));
    	save();
        res.forwardToPreviousPage(req);
    }
    
    public void doDeleteConfiguration(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	this.buildStatConfigs.remove(Integer.parseInt(req.getParameter("buildStatId")));
    	save();
        res.forwardToPreviousPage(req);
    }
    
    public void doMoveUpConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	// Swapping build confs
    	int index = Integer.parseInt(req.getParameter("buildStatId"));
    	BuildStatConfiguration b = this.buildStatConfigs.get(index);
    	this.buildStatConfigs.set(index, this.buildStatConfigs.get(index-1));
    	this.buildStatConfigs.set(index-1, b);
    	save();
        res.forwardToPreviousPage(req);
    }
    
    public void doMoveDownConf(StaplerRequest req, StaplerResponse res) throws ServletException, IOException {
    	// Swapping build confs
    	int index = Integer.parseInt(req.getParameter("buildStatId"));
    	BuildStatConfiguration b = this.buildStatConfigs.get(index);
    	this.buildStatConfigs.set(index, this.buildStatConfigs.get(index+1));
    	this.buildStatConfigs.set(index+1, b);
    	save();
        res.forwardToPreviousPage(req);
    }
    
    private BuildStatConfiguration createBuildStatConfig(StaplerRequest req){
    	return new BuildStatConfiguration(
    			req.getParameter("title"), 
    			Integer.parseInt(req.getParameter("buildStatWidth")),
    			Integer.parseInt(req.getParameter("buildStatHeight")),
    			Integer.parseInt(req.getParameter("historicLength")), 
    			HistoricScale.valueOf(req.getParameter("historicScale")),
    			req.getParameter("jobFilter"),
    			Boolean.parseBoolean(req.getParameter("successShown")),
    			Boolean.parseBoolean(req.getParameter("failuresShown")),
    			Boolean.parseBoolean(req.getParameter("unstablesShown")),
    			Boolean.parseBoolean(req.getParameter("abortedShown")),
    			Boolean.parseBoolean(req.getParameter("notBuildsShown")));
    }
    
    private JFreeChart createChart(StaplerRequest req, CategoryDataset dataset, String title) {

        final JFreeChart chart = ChartFactory.createLineChart(
                title, // chart title
                null, // unused
                "Count", // range axis label
                dataset, // data
                PlotOrientation.VERTICAL, // orientation
                true, // include legend
                true, // tooltips
                false // urls
                );

        final LegendTitle legend = chart.getLegend();
        legend.setPosition(RectangleEdge.RIGHT);

        chart.setBackgroundPaint(Color.white);

        final CategoryPlot plot = chart.getCategoryPlot();

        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlinePaint(null);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.black);

        CategoryAxis domainAxis = new ShiftedCategoryAxis(null);
        plot.setDomainAxis(domainAxis);
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setLowerMargin(0.0);
        domainAxis.setUpperMargin(0.0);
        domainAxis.setCategoryMargin(0.0);
        domainAxis.setMaximumCategoryLabelLines(2);
        domainAxis.setMaximumCategoryLabelWidthRatio(2.0F);

        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setLowerBound(0.0);
        rangeAxis.setUpperBound(rangeAxis.getUpperBound()+1.0);

        final StackedAreaRenderer2 renderer = new StackedAreaRenderer2();
        renderer.setSeriesPaint(0, Color.yellow);
        renderer.setSeriesPaint(1, Color.red);
        renderer.setSeriesPaint(2, Color.gray);
        renderer.setSeriesPaint(3, Color.blue);
        renderer.setSeriesPaint(4, Color.pink);

        
        plot.setRenderer(renderer);
        plot.setInsets(new RectangleInsets(5.0, 0, 0, 5.0));

        return chart;
    }
    
    private DataSetBuilder<String, DateRange> createDataSetBuilder(List<JobBuildResult> filteredJobBuildResults, 
    			BuildStatConfiguration config){
        DataSetBuilder<String, DateRange> dsb = new DataSetBuilder<String, DateRange>();
        
        if(filteredJobBuildResults.size() == 0){
        	return dsb;
        }
        
    	Calendar d2 = new GregorianCalendar();
    	Calendar d1 = config.getHistoricScale().getPreviousStep(d2);
    	
    	int nbSuccess=0, nbFailures=0, nbUnstables=0, nbAborted=0, nbNotBuild=0;
    	int nbSteps = 0;
    	Iterator<JobBuildResult> buildsIter = filteredJobBuildResults.iterator();
    	JobBuildResult currentBuild = buildsIter.next();
    	Calendar buildDate = currentBuild.getBuildDate();
    	while(nbSteps != config.getHistoricLength()){
        	// Finding range where the build resides
        	while(nbSteps < config.getHistoricLength() && d1.after(buildDate)){
        		DateRange range = new DateRange(d1, d2);
        		//if((config.getShownBuildResults() & BuildResult.SUCCESS.code) != 0){
        		dsb.add(nbSuccess, "success", range);
        		//}
    			dsb.add(nbFailures, "failures", range);
    			dsb.add(nbUnstables, "unstables", range);
    			dsb.add(nbAborted, "aborted", range);
    			dsb.add(nbNotBuild, "not build", range);
        		
				d2 = (Calendar)d1.clone();
				d1 = config.getHistoricScale().getPreviousStep(d2);
				nbSuccess=0; nbFailures=0; nbUnstables=0; nbAborted=0; nbNotBuild=0;
				nbSteps++;
        	}
        	
        	// If no range found : stop the iteration !
        	if(nbSteps != config.getHistoricLength() && currentBuild != null){
        		nbSuccess += config.isSuccessShown()?currentBuild.getResult().getSuccessCount():0;
        		nbFailures += config.isFailuresShown()?currentBuild.getResult().getFailureCount():0;
        		nbUnstables += config.isUnstablesShown()?currentBuild.getResult().getUnstableCount():0;
        		nbAborted += config.isAbortedShown()?currentBuild.getResult().getAbortedCount():0;
        		nbNotBuild += config.isNotBuildShown()?currentBuild.getResult().getNotBuildCount():0;
        		
        		if(buildsIter.hasNext()){
        			currentBuild = buildsIter.next();
        			buildDate = currentBuild.getBuildDate();
        		} else {
        			currentBuild = null;
        			buildDate = new GregorianCalendar(); buildDate.setTimeInMillis(1);
        		}
        	}
    	}
    	
        return dsb;
    }
    
    private List<JobBuildResult> createFilteredAndSortedBuildResults(BuildStatConfiguration config){
    	List<JobBuildResult> filteredJobBuildResults = new ArrayList<JobBuildResult>();
        for(JobBuildResult r : jobBuildResults){
        	if(JobFilterFactory.createJobFilter(config.getJobFilter()).isJobApplicable(r.getJobName())){
        		filteredJobBuildResults.add(r);
        	}
        }
        
        // Sorting on job results dates
        Collections.sort(filteredJobBuildResults, Collections.reverseOrder(new Comparator<JobBuildResult>() {
        	public int compare(JobBuildResult o1, JobBuildResult o2) {
        		return o1.getBuildDate().compareTo(o2.getBuildDate());
        	}
		}));
        
        return filteredJobBuildResults;
    }
    	
	private void addBuildsFrom(AbstractProject project){
        List<AbstractBuild> builds = project.getBuilds();
        Iterator<AbstractBuild> buildIterator = builds.iterator();

        while (buildIterator.hasNext()) {
        	addBuild(buildIterator.next());
        }
	}
	
	private void addBuild(AbstractBuild build){
    	jobBuildResults.add(JobBuildResultFactory.INSTANCE.createJobBuildResult(build));
	}

	public BuildStatConfiguration[] getBuildStatConfigs() {
		return buildStatConfigs.toArray(new BuildStatConfiguration[]{});
	}
}
