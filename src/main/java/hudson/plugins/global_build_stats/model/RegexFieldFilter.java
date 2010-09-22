package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.FieldFilter;

import java.util.regex.Pattern;

public class RegexFieldFilter implements FieldFilter {

	private Pattern fieldPattern;
	
	public RegexFieldFilter(String fieldPattern){
		this.fieldPattern = Pattern.compile(fieldPattern);
	}
	
	public boolean isFieldValueValid(String fieldValue) {
		if(fieldPattern == null || "".equals(fieldPattern.pattern())){
			return true;
		}
		
		if(fieldValue == null){
			return false;
		}
		
		return fieldPattern.matcher(fieldValue).matches();
	}

}
