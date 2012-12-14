package com.philipp_mandler.hexapod.hexapod;

public class StatusPackage implements NetPackage {

	private static final long serialVersionUID = 646406666254974429L;
	
	int m_servoCount = 0;
	
	public StatusPackage(int servoCount) {
		m_servoCount = servoCount;
	}	

}
