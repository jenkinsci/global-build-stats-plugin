package hudson.plugins.global_build_stats.model;

import java.text.DateFormat;
import java.util.Calendar;

public class DateRange implements Comparable<DateRange> {

	private Calendar d1, d2;
	private DateFormat dateFormatter;
	
	public DateRange(Calendar _d1, Calendar _d2, DateFormat _dateFormatter){
		this.d1 = (Calendar)_d1.clone();
		this.d2 = (Calendar)_d2.clone();
		this.dateFormatter = _dateFormatter;
	}
	
	public int compareTo(DateRange o) {
		return this.d1.compareTo(o.d1);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(dateFormatter.format(d1.getTime())).append(" --> ").append(dateFormatter.format(d2.getTime())).toString();
	}
}
