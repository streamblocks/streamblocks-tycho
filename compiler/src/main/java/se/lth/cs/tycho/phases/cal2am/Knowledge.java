package se.lth.cs.tycho.phases.cal2am;

public enum Knowledge {
	TRUE {
		public Knowledge and(Knowledge knowledge) {
			return knowledge;
		}

		public Knowledge or(Knowledge knowledge) {
			return TRUE;
		}
	},
	FALSE {
		public Knowledge and(Knowledge knowledge) {
			return FALSE;
		}

		public Knowledge or(Knowledge knowledge) {
			return knowledge;
		}
	},
	UNKNOWN {
		public Knowledge and(Knowledge knowledge) {
			if (knowledge == FALSE) {
				return FALSE;
			} else {
				return UNKNOWN;
			}
		}

		public Knowledge or(Knowledge knowledge) {
			if (knowledge == TRUE) {
				return TRUE;
			} else {
				return UNKNOWN;
			}
		}
	};

	public static Knowledge ofNullable(Boolean value) {
		if (value == null) {
			return UNKNOWN;
		} else if (value) {
			return TRUE;
		} else {
			return FALSE;
		}
	}

	public static Knowledge of(boolean value) {
		return value ? TRUE : FALSE;
	}

	public abstract Knowledge and(Knowledge knowledge);
	public abstract Knowledge or(Knowledge knowledge);
}
