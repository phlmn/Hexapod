package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.LegPositionPackage;
import com.philipp_mandler.hexapod.hexapod.LegServoPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec3;

public class Leg {
	
	private SingleServo[] m_servos;
	private volatile Vec3 m_goalPosition;
	private Vec2 m_position;
	private double m_upperLeg;
	private double m_lowerLeg;
	private int m_legID;
	private double m_angle;
	
	public Leg(int legID, double upperLegLength, double lowerLegLength, Vec2 position, double angle, SingleServo servo1, SingleServo servo2, SingleServo servo3) {
		m_legID = legID;
		
		m_servos = new SingleServo[3];
		
		m_upperLeg = upperLegLength;
		m_lowerLeg = lowerLegLength;
		
		m_servos[0] = servo1;
		m_servos[1] = servo2;
		m_servos[2] = servo3;
		
		for(SingleServo servo : m_servos) {
			if(!servo.ping())
				DebugHelper.log("Servo (ID: " + servo.getID() + ") from Leg (ID: " + legID + ") couldn't be found.", Log.WARNING);
		}
		
		m_goalPosition = null;
		
		m_position = position;
		m_angle = angle;
	}
	
	public void setGoalPosition(Vec3 pos) {
		m_goalPosition = pos;
	}
	
	public Vec3 getGoalPosition() {
		return m_goalPosition;
	}
	
	public void setToruqeEnabled(boolean enable) {
		m_servos[0].setTorqueEnabled(enable);
		m_servos[1].setTorqueEnabled(enable);
		m_servos[2].setTorqueEnabled(enable);
	}
	
	public double calculateS1Value(Vec2 goal, double distance) {
		if(goal.getX() < 0) {
			if(goal.getY() < 0)
				return 2 * Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) + Math.asin( Math.abs(goal.getY()) / distance ) );
			
			return 2 * Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) - Math.asin( Math.abs(goal.getY()) / distance ) );
		}
		else {
			if(goal.getY() < 0)
				return Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) - Math.asin( Math.abs(goal.getY()) / distance ) );
			
			return Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) + Math.asin( Math.abs(goal.getY()) / distance ) );
		}
	}
	
	public double calculateS2Value(Vec2 goal, double distance) {
		return Math.acos( (Math.pow(m_upperLeg, 2) + Math.pow(m_lowerLeg, 2) - Math.pow(distance, 2)) / (2 * m_upperLeg * m_lowerLeg) );
	}
	
	public double calculateStartGoalDistance(Vec2 goal) {
		return Math.sqrt( Math.pow(goal.getX(), 2) + Math.pow(goal.getY(), 2) );
	}
	
	public void moveLegToPosition(Vec3 goal) {
		Vec3 tmpGoal = new Vec3(goal.getX() - m_position.getX(), goal.getY() - m_position.getY(), goal.getZ());
		Vec2 tempRotatedGoal = new Vec2(Math.sqrt(Math.pow(tmpGoal.getX(), 2) + Math.pow(tmpGoal.getY(), 2)) - 55, tmpGoal.getZ());
		double rotDistance = calculateStartGoalDistance(tempRotatedGoal);
		double s1 = calculateS1Value(tempRotatedGoal, rotDistance);
		double s2 = calculateS2Value(tempRotatedGoal, rotDistance);
		double s0 = Math.PI - Math.asin(tmpGoal.getY() / rotDistance) - m_angle;
		if(!(Double.isNaN(s0) || Double.isNaN(s1) || Double.isNaN(s2) || Double.isInfinite(s0) || Double.isInfinite(s1) || Double.isInfinite(s2))) {
			if(!Main.isTestmode()) {
				m_servos[0].setGoalPosition(s0);				
				m_servos[1].setGoalPosition(s1);
				m_servos[2].setGoalPosition(s2);
			}
			Main.getNetworking().broadcast(new LegServoPackage(m_legID, s0, s1, s2), DeviceType.InfoScreen);
		}
		Main.getNetworking().broadcast(new LegPositionPackage(m_legID, new Vec3(goal)), DeviceType.InfoScreen);
	}
	
	public void moveLegToRelativePosition(Vec3 goal) {
		Vec2 tempRotatedGoal = new Vec2(Math.sqrt(Math.pow(goal.getX(), 2) + Math.pow(goal.getY(), 2)) - 55, goal.getZ());
		double rotDistance = calculateStartGoalDistance(tempRotatedGoal);
		double s1 = calculateS1Value(tempRotatedGoal, rotDistance);
		double s2 = calculateS2Value(tempRotatedGoal, rotDistance);
		double s0 = Math.PI - Math.asin(goal.getY() / rotDistance) - m_angle;
		if(!(Double.isNaN(s0) || Double.isNaN(s1) || Double.isNaN(s2) || Double.isInfinite(s0) || Double.isInfinite(s1) || Double.isInfinite(s2))) {
			if(!Main.isTestmode()) {
				m_servos[0].setGoalPosition(s0);				
				m_servos[1].setGoalPosition(s1);
				m_servos[2].setGoalPosition(s2);
			}
			Main.getNetworking().broadcast(new LegServoPackage(m_legID, s0, s1, s2), DeviceType.InfoScreen);
		}
		Main.getNetworking().broadcast(new LegPositionPackage(m_legID, new Vec3(goal.getX() + m_position.getX(), goal.getY() + m_position.getY(), goal.getZ())), DeviceType.InfoScreen);
	}
	
	public void setLegID(int id) {
		m_legID = id;
	}
	
	public int getLegID() {
		return m_legID;
	}
	
	public void updateServos() {
		if(m_goalPosition != null)
			moveLegToPosition(m_goalPosition);	
	}
}
