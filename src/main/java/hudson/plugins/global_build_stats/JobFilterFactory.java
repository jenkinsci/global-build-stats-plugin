package hudson.plugins.global_build_stats;

import hudson.plugins.global_build_stats.model.RegexOnNameJobFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobFilterFactory {

	public static final String ALL_JOBS_FILTER_PATTERN = "ALL";
	
	private static final Pattern REGEX_ON_NAME_JOB_FILTER_PATTERN = Pattern.compile("jobNameRegex\\((.*)\\)");
	
	public static JobFilter createJobFilter(String jobFilter){
		if(ALL_JOBS_FILTER_PATTERN.equals(jobFilter)){
			return JobFilter.ALL;
		} else {
			Matcher regexOnNameJobFilterMatcher = REGEX_ON_NAME_JOB_FILTER_PATTERN.matcher(jobFilter);
			if(regexOnNameJobFilterMatcher.matches()){
				return new RegexOnNameJobFilter(regexOnNameJobFilterMatcher.group(1));
			} else {
				return JobFilter.ALL;
			}
		}
	}
}
