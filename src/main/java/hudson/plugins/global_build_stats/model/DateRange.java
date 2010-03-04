package hudson.plugins.global_build_stats.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateRange implements Comparable<DateRange> {

	// TODO: internationalize this !
	private static final SimpleDateFormat SDF = new SimpleDateFormat("dd MMM yy HH:mm");
	
	private Calendar d1, d2;
	
	public DateRange(Calendar _d1, Calendar _d2){
		this.d1 = (Calendar)_d1.clone();
		this.d2 = (Calendar)_d2.clone();
	}
	
	public int compareTo(DateRange o) {
		return this.d1.compareTo(o.d1);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(SDF.format(d1.getTime())).append(" --> ").append(SDF.format(d2.getTime())).toString();
	}
}
