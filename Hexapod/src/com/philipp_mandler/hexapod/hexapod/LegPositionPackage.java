package com.philipp_mandler.hexapod.hexapod;


public class LegPositionPackage implements NetPackage {

	private static final long serialVersionUID = 4667006753603340204L;
	
	private int m_legIndex;
	private Vec3 m_goal;
	
	public LegPositionPackage(int legIndex, Vec3 goal) {
		this.m_legIndex = legIndex;
		this.m_goal = goal;
	}
	
	public int getLegIndex() {
		return m_legIndex;
	}
	
	public void setLegIndex(int index) {
		m_legIndex = index;
	}
	
	public Vec3 getGoalPosition() {
		return m_goal;
	}
	
	public void setGoalPosition(Vec3 goal) {
		this.m_goal = goal;
	}
}
