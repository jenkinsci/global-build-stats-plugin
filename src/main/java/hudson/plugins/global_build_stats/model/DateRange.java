package hudson.plugins.global_build_stats.model;

import java.text.DateFormat;
import java.util.Calendar;

public class DateRange implements Comparable<DateRange> {

	private Calendar start, end;
	private DateFormat dateFormatter;
	
	public DateRange(Calendar _start, Calendar _end, DateFormat _dateFormatter){
		this.start = (Calendar)_start.clone();
		this.end = (Calendar)_end.clone();
		this.dateFormatter = _dateFormatter;
	}
	
	public int compareTo(DateRange o) {
		return this.start.compareTo(o.start);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append(dateFormatter.format(start.getTime())).append(" --> ").append(dateFormatter.format(end.getTime())).toString();
	}

	public Calendar getStart() {
		return start;
	}

	public Calendar getEnd() {
		return end;
	}
}
