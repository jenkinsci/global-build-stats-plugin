package hudson.plugins.global_build_stats;

import hudson.plugins.global_build_stats.model.AbstractBuildStatChartDimension;
import hudson.plugins.global_build_stats.model.BuildStatChartData;
import hudson.plugins.global_build_stats.model.BuildStatConfiguration;
import hudson.plugins.global_build_stats.model.DateRange;
import hudson.util.DataSetBuilder;
import org.junit.jupiter.api.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BuildStatChartDataCreationTest {

	@Test
	void testBuildStatChartDataInstantiation() {
		BuildStatConfiguration config = new BuildStatConfiguration();
		DataSetBuilder<String, DateRange> dsb = new DataSetBuilder<>();
		List<AbstractBuildStatChartDimension> dimensions = new ArrayList<>();
		dimensions.add(new AbstractBuildStatChartDimension.BuildCounterChartDimension(config, dsb));

		Calendar firstDay = new GregorianCalendar(2010, Calendar.JANUARY, 1);
		Calendar secondDay = new GregorianCalendar(2010, Calendar.JANUARY, 2);
		DateRange dr = new DateRange(firstDay, secondDay, new SimpleDateFormat("dd/MM/yyyy"));
		dsb.add(1, Messages.Build_Results_Item_Legend_Statuses_SUCCESS(), dr);
		dsb.add(2, Messages.Build_Results_Item_Legend_Statuses_FAILURES(), dr);

		// Calling constructor
		assertDoesNotThrow(() -> new BuildStatChartData(dimensions));
	}
}
