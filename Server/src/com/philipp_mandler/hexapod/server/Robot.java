package com.philipp_mandler.hexapod.server;

public class Robot {
	private Leg[] m_legs;
	
	public Robot(Leg[] legs) {
		m_legs = legs;
	}
	
	public Leg[] getLegs() {
		return m_legs;
	}
	
	public void setLegs(Leg[] legs) {
		m_legs = legs;
	}
}
