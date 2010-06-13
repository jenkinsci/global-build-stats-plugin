package hudson.plugins.global_build_stats.utils;

public class UIUtils {

	public static String escapeAntiSlashes(String value){
		if(value != null){
			return value.replaceAll("\\\\", "\\\\\\\\");
		} else {
			return null;
		}
	}
}
