package hudson.plugins.global_build_stats.model;

import java.util.regex.Pattern;

import hudson.plugins.global_build_stats.JobFilter;

public class RegexOnNameJobFilter implements JobFilter {

	private Pattern namePattern;
	
	public RegexOnNameJobFilter(String projectNamePattern){
		namePattern = Pattern.compile(projectNamePattern);
	}
	
	public boolean isJobApplicable(String projectName) {
		return namePattern.matcher(projectName).matches();
	}

}
