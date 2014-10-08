package net.opendf.transform.caltoam.util;

public enum TestResult {
	True, False, Unknown;
	public static TestResult from(Boolean b) {
		if (b == null) {
			return Unknown;
		}
		return b.booleanValue() ? True : False;
	}
	
	public static TestResult from(boolean b) {
		return b ? True : False;
	}
	
	public TestResult and(TestResult that) {
		if (this == False || that == False) {
			return False;
		}
		if (this == True && that == True) {
			return True;
		}
		return Unknown;
	}
	
	public TestResult or(TestResult that) {
		if (this == True || that == True) {
			return True;
		}
		if (this == False && that == False) {
			return False;
		}
		return Unknown;
	}
	
	public TestResult not() {
		switch(this) {
		case True: return False;
		case False: return True;
		default: return Unknown;
		}
	}
}
