package hudson.plugins.global_build_stats.validation;

import hudson.plugins.global_build_stats.FieldFilterFactory;
import hudson.plugins.global_build_stats.Messages;
import hudson.plugins.global_build_stats.model.HistoricScale;
import hudson.util.FormValidation;

public class GlobalBuildStatsValidator {

    public FormValidation checkJobFilter(String value){
    	try{ FieldFilterFactory.createJobFilter(value); return FormValidation.ok(); }
    	catch(Throwable t){ return FormValidation.error(Messages.Validation_Messages_JobFilter_Invalid()); }
    }
    
    public FormValidation checkFailuresShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error(Messages.Validation_Messages_FailuresShown_Boolean()); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkUnstablesShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error(Messages.Validation_Messages_UnstablesShown_Boolean()); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkAbortedShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error(Messages.Validation_Messages_AbortedShown_Boolean()); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkNotBuildsShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error(Messages.Validation_Messages_NotBuildsShown_Boolean()); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkSuccessShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error(Messages.Validation_Messages_SuccessShown_Boolean()); }
    	else { return FormValidation.ok(); }
    }
    
	public FormValidation checkHistoricScale(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error(Messages.Validation_Messages_HistoricScale_Mandatory()); }
    	else {
    		try{ HistoricScale.valueOf(value); return FormValidation.ok(); }
    		catch(Throwable t){ return FormValidation.error(Messages.Validation_Messages_HistoricScale_Invalid()); }
    	}
    }

	public FormValidation checkHistoricLength(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error(Messages.Validation_Messages_HistoricLength_Mandatory()); }
    	else if(!ValidationHelper.isInt(value)){ return FormValidation.error(Messages.Validation_Messages_HistoricLength_Integer()); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation checkBuildStatHeight(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error(Messages.Validation_Messages_BuildStatsHeight_Mandatory()); }
    	else if(!ValidationHelper.isInt(value)){ return FormValidation.error(Messages.Validation_Messages_BuildStatsHeight_Integer()); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation checkBuildStatWidth(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error(Messages.Validation_Messages_BuildStatsWidth_Mandatory()); }
    	else if(!ValidationHelper.isInt(value)){ return FormValidation.error(Messages.Validation_Messages_BuildStatsWidth_Integer()); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation checkTitle(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error(Messages.Validation_Messages_Title_Mandatory()); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkYAxisChartType(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error(Messages.Validation_Messages_YAxisChartType_Mandatory()); }
    	else { return FormValidation.ok(); }
    }
}
