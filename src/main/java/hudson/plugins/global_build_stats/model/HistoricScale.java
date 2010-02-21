package hudson.plugins.global_build_stats.model;

import java.util.Calendar;

public enum HistoricScale {
	YEARLY(Calendar.YEAR),
	MONTHLY(Calendar.MONTH),
	WEEKLY(Calendar.WEEK_OF_YEAR),
	DAILY(Calendar.DAY_OF_YEAR),
	HOURLY(Calendar.HOUR);
	
	private int calendarField;
	
	private HistoricScale(int _calendarField){
		this.calendarField = _calendarField;
	}
	
	public Calendar getPreviousStep(Calendar currentStep){
		return step(currentStep, -1);
	}
	
	public Calendar getNextStep(Calendar currentStep){
		return step(currentStep, 1);
	}
	
	public Calendar step(Calendar currentDate, int step){
		Calendar previousStep = (Calendar)currentDate.clone();
		previousStep.add(calendarField, step);
		return previousStep;
	}
}
