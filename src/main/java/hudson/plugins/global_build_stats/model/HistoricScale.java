package hudson.plugins.global_build_stats.model;

import java.util.Calendar;

public enum HistoricScale {
	YEARLY(Calendar.YEAR),
	MONTHLY(Calendar.MONTH),
	WEEKLY(Calendar.WEEK_OF_YEAR),
	DAILY(Calendar.DAY_OF_YEAR, new Integer[]{ Calendar.HOUR, Calendar.MINUTE }),
	HOURLY(Calendar.HOUR, new Integer[]{ Calendar.MINUTE });
	
	private int calendarField;
	private Integer[] fieldsToReset;
	
	
	private HistoricScale(int _calendarField){
		this(_calendarField, new Integer[0]);
	}
	
	private HistoricScale(int _calendarField, Integer[] _fieldsToReset){
		this.calendarField = _calendarField;
		this.fieldsToReset = _fieldsToReset;
	}
	
	public Calendar getPreviousStep(Calendar currentStep){
		Calendar previousStep = (Calendar)currentStep.clone();
		
		// Rounding date
		boolean fieldToResetAlreadyReseted = true;
		int i=0;
		while(fieldToResetAlreadyReseted && i<fieldsToReset.length){
			fieldToResetAlreadyReseted = currentStep.get(fieldsToReset[i])==0;
			i++;
		}
		
		if(fieldToResetAlreadyReseted){
			previousStep.add(calendarField, -1);
		} else {
			for(i=0; i<fieldsToReset.length; i++){
				previousStep.set(fieldsToReset[i], 0);
			}
		}
		
		return previousStep;
	}
}
