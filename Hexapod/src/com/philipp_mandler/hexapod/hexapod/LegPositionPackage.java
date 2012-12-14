package com.philipp_mandler.hexapod.hexapod;

import java.io.Serializable;

public class LegPositionPackage implements NetPackage, Serializable {

	private static final long serialVersionUID = 4667006753603340204L;
	
	private int legIndex;
	private Vec3 goal;
	
	public LegPositionPackage(int legIndex, Vec3 goal) {
		this.legIndex = legIndex;
		this.goal = goal;
	}
	
	public int getLegIndex() {
		return legIndex;
	}
	
	public void setLegIndex(int index) {
		legIndex = index;
	}
	
	public Vec3 getGoalPosition() {
		return goal;
	}
	
	public void setGoalPosition(Vec3 goal) {
		this.goal = goal;
	}
}
