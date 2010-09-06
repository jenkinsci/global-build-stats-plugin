package hudson.plugins.global_build_stats;

import hudson.plugins.global_build_stats.model.BuildStatChartData;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.util.DataSetBuilder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.junit.Test;

public class BuildStatChartDataCreationTest {
	
	@Test
	public void testBuildStatChartDataInstanciation(){
		DataSetBuilder<String, DateRange> dsb = new DataSetBuilder<String, DateRange>();
		
		Calendar firstDay = new GregorianCalendar(2010, 0, 1);
		Calendar secondDay = new GregorianCalendar(2010, 0, 2);
		DateRange dr = new DateRange(firstDay, secondDay, new SimpleDateFormat("dd/MM/yyyy"));
		dsb.add(1, Messages.Build_Results_Statuses_5_SUCCESS(), dr);
		dsb.add(2, Messages.Build_Results_Statuses_4_FAILURES(), dr);
		
		// Calling constructor
		new BuildStatChartData(dsb);
	}
}
