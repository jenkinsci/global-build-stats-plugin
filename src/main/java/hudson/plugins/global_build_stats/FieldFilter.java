package hudson.plugins.global_build_stats;


public interface FieldFilter {

	boolean isFieldValueValid(String fieldValue);
	
	
	public static final FieldFilter ALL = new FieldFilter() {
		public boolean isFieldValueValid(String fieldValue) {
			return true;
		}
	};
}
