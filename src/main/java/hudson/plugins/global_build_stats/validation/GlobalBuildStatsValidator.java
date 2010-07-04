package hudson.plugins.global_build_stats.validation;

import hudson.plugins.global_build_stats.JobFilterFactory;
import hudson.plugins.global_build_stats.model.HistoricScale;
import hudson.util.FormValidation;

public class GlobalBuildStatsValidator {

    public FormValidation checkJobFilter(String value){
    	try{ JobFilterFactory.createJobFilter(value); return FormValidation.ok(); }
    	catch(Throwable t){ return FormValidation.error("JobFilter is invalid"); }
    }
    
    public FormValidation checkFailuresShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error("FailuresShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkUnstablesShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error("UnstablesShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkAbortedShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error("AbortedShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkNotBuildsShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error("NotBuildsShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
    public FormValidation checkSuccessShown(String value){
    	if(!ValidationHelper.isBool(value)){ return FormValidation.error("SuccessShown must be a boolean"); }
    	else { return FormValidation.ok(); }
    }
    
	public FormValidation checkHistoricScale(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error("Historic scale is mandatory"); }
    	else {
    		try{ HistoricScale.valueOf(value); return FormValidation.ok(); }
    		catch(Throwable t){ return FormValidation.error("HistoricScale is invalid"); }
    	}
    }

	public FormValidation checkHistoricLength(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error("Historic length is mandatory"); }
    	else if(!ValidationHelper.isInt(value)){ return FormValidation.error("Historic length should be an integer"); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation checkBuildStatHeight(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error("Build stats height is mandatory"); }
    	else if(!ValidationHelper.isInt(value)){ return FormValidation.error("Build stats height should be an integer"); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation checkBuildStatWidth(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error("Build stats width is mandatory"); }
    	else if(!ValidationHelper.isInt(value)){ return FormValidation.error("Build stats width should be an integer"); }
    	else { return FormValidation.ok(); }
    }

    public FormValidation checkTitle(String value){
    	if(!ValidationHelper.isMandatory(value)){ return FormValidation.error("Title is mandatory"); }
    	else { return FormValidation.ok(); }
    }
}
