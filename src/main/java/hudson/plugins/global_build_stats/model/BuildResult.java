package hudson.plugins.global_build_stats.model;

public enum BuildResult {

	SUCCESS((short)1),
	FAILURE((short)2),
	UNSTABLE((short)4),
	ABORTED((short)8),
	NOT_BUILD((short)16);

	public transient short code;
	
	private BuildResult(short _code){
		this.code = _code;
	}
	
	public int getSuccessCount() {
		return (code >> 0)&1;
	}

	public int getFailureCount() {
		return (code >> 1)&1;
	}

	public int getUnstableCount() {
		return (code >> 2)&1;
	}

	public int getAbortedCount() {
		return (code >> 3)&1;
	}

	public int getNotBuildCount() {
		return (code >> 4)&1;
	}
}
