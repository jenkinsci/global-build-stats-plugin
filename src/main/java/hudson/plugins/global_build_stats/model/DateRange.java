package hudson.plugins.global_build_stats.model;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import hudson.plugins.global_build_stats.FromRequestObjectFactory;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility=3)
public class DateRange implements Comparable<DateRange> {
	private static final Logger LOGGER = Logger.getLogger(DateRange.class.getName());

	private Calendar start, end;
	private DateFormat dateFormatter;
	private TimeZone timeZone;
	
	public DateRange(Calendar _start, Calendar _end, DateFormat _dateFormatter, TimeZone _timeZone){
		this.start = (Calendar)_start.clone();
		this.end = (Calendar)_end.clone();
		this.dateFormatter = _dateFormatter;
		LOGGER.log(Level.FINEST, "timeZone is "+ _timeZone);
		this.timeZone = _timeZone;
	}
	
	public int compareTo(DateRange o) {
		return this.start.compareTo(o.start);
	}
	
	@Override
	public String toString() {
		String startTimeTxt = dateFormatter.format(start.getTime());
		String endTimeTxt = dateFormatter.format(end.getTime());
		if(this.timeZone != null){
			Calendar convertedStart = new GregorianCalendar(this.timeZone);
			convertedStart.setTimeInMillis(start.getTimeInMillis());
			dateFormatter.setTimeZone(this.timeZone);
			startTimeTxt = dateFormatter.format(convertedStart.getTime());
			LOGGER.log(Level.FINEST, "startTimeTxt is "+ startTimeTxt);

			Calendar convertedEnd = new GregorianCalendar(this.timeZone);
			convertedEnd.setTimeInMillis(end.getTimeInMillis());
			endTimeTxt = dateFormatter.format(convertedEnd.getTime());
			LOGGER.log(Level.FINEST, "endTimeTxt is "+ endTimeTxt);
		}
		return new StringBuilder().append(startTimeTxt).append(" --> ").append(endTimeTxt).toString();
	}

	@Exported
	public Calendar getStart() {
		return start;
	}

	@Exported
	public Calendar getEnd() {
		return end;
	}
}
