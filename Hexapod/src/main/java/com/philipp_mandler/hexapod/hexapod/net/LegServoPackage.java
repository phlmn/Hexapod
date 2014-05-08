package com.philipp_mandler.hexapod.hexapod.net;

public class LegServoPackage implements NetPackage {

	private static final long serialVersionUID = -8879734348968174386L;
	
	private int m_legID;
	private double m_servoPos1;
	private double m_servoPos2;
	private double m_servoPos3;
	
	public LegServoPackage(int legID, double servoPos1, double servoPos2, double servoPos3) {
		m_legID = legID;
		m_servoPos1 = servoPos1;
		m_servoPos2 = servoPos2;
		m_servoPos3 = servoPos3;
	}
	
	public void setLegID(int legID) {
		m_legID = legID;
	}
	
	public int getLegID() {
		return m_legID;
	}
	
	public void setServoPos1(double pos) {
		m_servoPos1 = pos;
	}
	
	public double getServoPos1() {
		return m_servoPos1;
	}
	
	public void setServoPos2(double pos) {
		m_servoPos2 = pos;
	}
	
	public double getServoPos2() {
		return m_servoPos2;
	}
	
	public void setServoPos3(double pos) {
		m_servoPos3 = pos;
	}
	
	public double getServoPos3() {
		return m_servoPos3;
	}

}
