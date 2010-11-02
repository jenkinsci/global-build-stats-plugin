package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.Messages;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public enum HistoricScale {
	HOURLY(Calendar.HOUR_OF_DAY, new Integer[]{ Calendar.MINUTE }, "EEE HH:mm"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_hours(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Hourly(); }
	}, HOURLY_FROM_NOW(Calendar.HOUR_OF_DAY, "EEE HH:mm"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_hours(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Hourly_From_Now(); }
	}, DAILY(Calendar.DAY_OF_YEAR, new Integer[]{ Calendar.HOUR_OF_DAY, Calendar.MINUTE }, "EEE dd HH'h'"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_days(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Daily(); }
	}, DAILY_FROM_NOW(Calendar.DAY_OF_YEAR, "EEE dd HH'h'"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_days(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Daily_From_Now(); }
	}, WEEKLY(Calendar.WEEK_OF_YEAR, new Integer[]{ Calendar.DAY_OF_WEEK, Calendar.HOUR_OF_DAY, Calendar.MINUTE}, "%W%w, EEE"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_weeks(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Weekly(); }
		@Override public Map<String,String> getDateRangeFormatterLocalizedConversionMap(){ return new HashMap<String, String>(){{ put("%W%", Messages.Historic_Scales_Unit_Labels_weekChar()); }}; }
	}, WEEKLY_FROM_NOW(Calendar.WEEK_OF_YEAR, "%W%w, EEE"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_weeks(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Weekly_From_Now(); }
		@Override public Map<String,String> getDateRangeFormatterLocalizedConversionMap(){ return new HashMap<String, String>(){{ put("%W%", Messages.Historic_Scales_Unit_Labels_weekChar()); }}; }
	}, MONTHLY(Calendar.MONTH, new Integer[]{ Calendar.DAY_OF_MONTH, Calendar.HOUR_OF_DAY, Calendar.MINUTE}, "dd MMM ''yy"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_monthes(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Monthly(); }
	}, MONTHLY_FROM_NOW(Calendar.MONTH, "dd MMM ''yy"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_monthes(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Monthly_From_Now(); }
	}, YEARLY(Calendar.YEAR, new Integer[]{ Calendar.MONTH, Calendar.DAY_OF_YEAR, Calendar.HOUR_OF_DAY, Calendar.MINUTE}, "''yy %d%D"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_years(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Yearly(); }
		@Override public Map<String,String> getDateRangeFormatterLocalizedConversionMap(){ return new HashMap<String, String>(){{ put("%d%", Messages.Historic_Scales_Unit_Labels_dayChar()); }}; }
	}, YEARLY_FROM_NOW(Calendar.YEAR, "''yy %d%D"){
		@Override public String getUnitLabel() { return Messages.Historic_Scales_Unit_Labels_years(); }
		@Override public String getLabel() { return Messages.Historic_Scales_Labels_Yearly_From_Now(); }
		@Override public Map<String,String> getDateRangeFormatterLocalizedConversionMap(){ return new HashMap<String, String>(){{ put("%d%", Messages.Historic_Scales_Unit_Labels_dayChar()); }}; }
	};
	
	private int calendarField;
	private Integer[] fieldsToReset;
	private String dateRangeFormatterPattern;
	
	
	private HistoricScale(int _calendarField, String dateRangeFormatterPattern){
		this(_calendarField, new Integer[0], dateRangeFormatterPattern);
	}
	
	private HistoricScale(int _calendarField, Integer[] _fieldsToReset, String _dateRangeFormatterPattern){
		this.calendarField = _calendarField;
		this.fieldsToReset = _fieldsToReset;
		this.dateRangeFormatterPattern = _dateRangeFormatterPattern;
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
		String localizedPattern = this.dateRangeFormatterPattern;
		for(Entry<String, String> entry : getDateRangeFormatterLocalizedConversionMap().entrySet()){
			localizedPattern = localizedPattern.replaceAll(entry.getKey(), "'"+entry.getValue()+"'");
		}
		return new SimpleDateFormat(localizedPattern);
	}

	public Map<String,String> getDateRangeFormatterLocalizedConversionMap(){
		// By default, don't localize anything here !
		return new HashMap<String, String>();
	}
	
	public abstract String getLabel();
	public abstract String getUnitLabel();
}
