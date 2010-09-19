package hudson.plugins.global_build_stats;

import hudson.plugins.global_build_stats.model.AbstractBuildStatChartDimension;
import hudson.plugins.global_build_stats.model.BuildStatChartData;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.util.DataSetBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Test;

public class BuildStatChartDataCreationTest {
	
	@Test
	public void testBuildStatChartDataInstanciation(){
		
		BuildStatConfiguration config = new BuildStatConfiguration();
		DataSetBuilder<String, DateRange> dsb = new DataSetBuilder<String, DateRange>();
		List<AbstractBuildStatChartDimension> dimensions = new ArrayList<AbstractBuildStatChartDimension>();
		dimensions.add(new AbstractBuildStatChartDimension.BuildCounterChartDimension(config, dsb));
		
		Calendar firstDay = new GregorianCalendar(2010, 0, 1);
		Calendar secondDay = new GregorianCalendar(2010, 0, 2);
		DateRange dr = new DateRange(firstDay, secondDay, new SimpleDateFormat("dd/MM/yyyy"));
		dsb.add(1, Messages.Build_Results_Statuses_SUCCESS(), dr);
		dsb.add(2, Messages.Build_Results_Statuses_FAILURES(), dr);
		
		// Calling constructor
		new BuildStatChartData(dimensions);
	}
}
