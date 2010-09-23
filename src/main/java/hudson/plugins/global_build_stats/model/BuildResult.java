package hudson.plugins.global_build_stats.model;

import hudson.plugins.global_build_stats.Messages;

public enum BuildResult {

	SUCCESS((short)1){
		@Override
		public String getLabel() {
			return Messages.Build_Results_Statuses_SUCCESS();
		}
	},
	FAILURE((short)2){
		@Override
		public String getLabel() {
			return Messages.Build_Results_Statuses_FAILURES();
		}
	},
	UNSTABLE((short)4){
		@Override
		public String getLabel() {
			return Messages.Build_Results_Statuses_UNSTABLES();
		}
	},
	ABORTED((short)8){
		@Override
		public String getLabel() {
			return Messages.Build_Results_Statuses_ABORTED();
		}
	},
	NOT_BUILD((short)16){
		@Override
		public String getLabel() {
			return Messages.Build_Results_Statuses_NOT_BUILD();
		}
	};

	public transient short code;
	
	private BuildResult(short _code){
		this.code = _code;
	}
	
	public abstract String getLabel();
	
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
