package hudson.plugins.global_build_stats.model;

import java.util.Calendar;

public enum HistoricScale {
	HOURLY(Calendar.HOUR, new Integer[]{ Calendar.MINUTE }, "hours"),
	HOURLY_FROM_NOW(Calendar.HOUR, "hours"),
	DAILY(Calendar.DAY_OF_YEAR, new Integer[]{ Calendar.HOUR, Calendar.MINUTE }, "days"),
	DAILY_FROM_NOW(Calendar.DAY_OF_YEAR, "days"),
	WEEKLY(Calendar.WEEK_OF_YEAR, new Integer[]{ Calendar.DAY_OF_WEEK, Calendar.HOUR, Calendar.MINUTE}, "weeks"),
	WEEKLY_FROM_NOW(Calendar.WEEK_OF_YEAR, "weeks"),
	MONTHLY(Calendar.MONTH, new Integer[]{ Calendar.DAY_OF_MONTH, Calendar.HOUR, Calendar.MINUTE}, "monthes"),
	MONTHLY_FROM_NOW(Calendar.MONTH, "monthes"),
	YEARLY(Calendar.YEAR, new Integer[]{ Calendar.MONTH, Calendar.DAY_OF_YEAR, Calendar.HOUR, Calendar.MINUTE}, "years"),
	YEARLY_FROM_NOW(Calendar.YEAR, "years");
	
	private int calendarField;
	private Integer[] fieldsToReset;
	private String tickLabel;
	
	
	private HistoricScale(int _calendarField, String tickLabel){
		this(_calendarField, new Integer[0], tickLabel);
	}
	
	private HistoricScale(int _calendarField, Integer[] _fieldsToReset, String tickLabel){
		this.calendarField = _calendarField;
		this.fieldsToReset = _fieldsToReset;
		this.tickLabel = tickLabel;
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
}
