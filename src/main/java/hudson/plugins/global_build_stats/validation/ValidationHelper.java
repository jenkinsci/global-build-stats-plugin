package hudson.plugins.global_build_stats.validation;

public class ValidationHelper {
	static boolean isInt(String value){
		try{
			Integer.parseInt(value);
			return true;
		}catch(NumberFormatException e){
			return false;
		}
	}
	
	static boolean isMandatory(String value){
		return value != null && !"".equals(value);
	}
	
	static boolean isBool(String value){
		try{
			Boolean.valueOf(value);
			return true;
		}catch(Throwable t){
			return false;
		}
	}
}
