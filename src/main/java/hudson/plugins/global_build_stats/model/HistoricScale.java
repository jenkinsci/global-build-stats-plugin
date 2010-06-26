package hudson.plugins.global_build_stats.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public enum HistoricScale {
	HOURLY(Calendar.HOUR_OF_DAY, new Integer[]{ Calendar.MINUTE }, "hours", "EEE HH:mm"),
	HOURLY_FROM_NOW(Calendar.HOUR_OF_DAY, "hours", "EEE HH:mm"),
	DAILY(Calendar.DAY_OF_YEAR, new Integer[]{ Calendar.HOUR_OF_DAY, Calendar.MINUTE }, "days", "EEE dd HH'h'"),
	DAILY_FROM_NOW(Calendar.DAY_OF_YEAR, "days", "EEE dd HH'h'"),
	WEEKLY(Calendar.WEEK_OF_YEAR, new Integer[]{ Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE}, "weeks", "'W'w, EEE"),
	WEEKLY_FROM_NOW(Calendar.WEEK_OF_YEAR, "weeks", "'W'w, EEE"),
	MONTHLY(Calendar.MONTH, new Integer[]{ Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE}, "monthes", "dd MMM ''yy"),
	MONTHLY_FROM_NOW(Calendar.MONTH, "monthes", "dd MMM ''yy"),
	YEARLY(Calendar.YEAR, new Integer[]{ Calendar.MONTH, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY, Calendar.MINUTE}, "years", "''yy 'd'D"),
	YEARLY_FROM_NOW(Calendar.YEAR, "years", "''yy 'd'D");
	
	private int calendarField;
	private Integer[] fieldsToReset;
	private String tickLabel;
	private DateFormat dateRangeFormatter;
	
	
	private HistoricScale(int _calendarField, String tickLabel, String dateRangeFormatterPattern){
		this(_calendarField, new Integer[0], tickLabel, dateRangeFormatterPattern);
	}
	
	private HistoricScale(int _calendarField, Integer[] _fieldsToReset, String tickLabel, String dateRangeFormatterPattern){
		this.calendarField = _calendarField;
		this.fieldsToReset = _fieldsToReset;
		this.tickLabel = tickLabel;
		// TODO: internationalize this !
		this.dateRangeFormatter = new SimpleDateFormat(dateRangeFormatterPattern);
	}
	
	public String getLabel(){
		return this.tickLabel;
	}
	
	public Calendar getPreviousStep(Calendar currentStep){
		Calendar previousStep = (Calendar)currentStep.clone();
		
		// Rounding date
		boolean atLeastOneFieldToResetIsNotReseted = false;
		int i=0;
		while(!atLeastOneFieldToResetIsNotReseted && i<fieldsToReset.length){
			atLeastOneFieldToResetIsNotReseted = !isFieldReseted(fieldsToReset[i], currentStep);
			i++;
		}
		
		if(atLeastOneFieldToResetIsNotReseted){
			for(i=0; i<fieldsToReset.length; i++){
				previousStep.set(fieldsToReset[i], getResetValueForCalendarField(fieldsToReset[i], previousStep));
			}
		} else {
			previousStep.add(calendarField, -1);
		}
		
		return previousStep;
	}
	
	private boolean isFieldReseted(int calendarField, Calendar c){
		return c.get(calendarField)==getResetValueForCalendarField(calendarField, c);
	}
	
	private int getResetValueForCalendarField(int calendarField, Calendar c){
		int resetValue = -1;
		if(calendarField==Calendar.DAY_OF_YEAR || calendarField==Calendar.DAY_OF_MONTH){
			resetValue = 1;
		} else if(calendarField==Calendar.DAY_OF_WEEK){
			resetValue=c.getFirstDayOfWeek();
		} else {
			resetValue = 0;
		}
		return resetValue;
	}

	public DateFormat getDateRangeFormatter() {
		return dateRangeFormatter;
	}
}
