package com.philipp_mandler.hexapod.server;

import SimpleDynamixel.Servo;

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
	private boolean m_rightSide;
	
	public Leg(int legID, double upperLegLength, double lowerLegLength, Vec2 position, double angle, SingleServo servo1, SingleServo servo2, SingleServo servo3, Servo servoController, boolean rightSide) {
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
		m_rightSide = rightSide;
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
	
	public void moveLegToPosition(Vec3 goal) {
		moveLegToRelativePosition(new Vec3(goal.getX() - m_position.getX(), goal.getY() - m_position.getY(), goal.getZ()));
	}
	
	public void moveLegToRelativePosition(Vec3 goal) {
		Vec2 tempRotatedGoal = new Vec2(Math.sqrt(Math.pow(goal.getX(), 2) + Math.pow(goal.getY(), 2)), goal.getZ());
		double rotDistance = tempRotatedGoal.getLength();
		
		
		// calculate s0
		
		double s0;
		
		if(m_rightSide)
			s0 = Math.asin(goal.getY() / rotDistance) + m_angle;
		else
			s0 = Math.PI - Math.asin(goal.getY() / rotDistance) - m_angle;
		
				
		// calculate S1
		
		double s1;
		if(m_rightSide) {
			/*if(goal.getY() < 0)
				s1 = 2 * Math.PI - (Math.PI  - ( Math.acos( (Math.pow(rotDistance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * rotDistance * m_upperLeg) ) - Math.asin( Math.abs(goal.getY()) / rotDistance )) );
			else */
			s1 = 2 * Math.PI - (Math.PI  - ( Math.acos( (Math.pow(rotDistance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * rotDistance * m_upperLeg) ) + Math.asin( tempRotatedGoal.getY() / rotDistance ) ));
		}
		else {
			 /*if(goal.getY() < 0)
				s1 = Math.PI  - ( Math.acos( (Math.pow(rotDistance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * rotDistance * m_upperLeg) ) - Math.asin( Math.abs(goal.getY()) / rotDistance ) );
			else */
				s1 = Math.PI  - ( Math.acos( (Math.pow(rotDistance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * rotDistance * m_upperLeg) ) + Math.asin( tempRotatedGoal.getY() / rotDistance ) );
		}
		
		
		// calculate S2
		
		double s2;
		if(m_rightSide)
			s2 = Math.PI * 2 - Math.acos( (Math.pow(m_upperLeg, 2) + Math.pow(m_lowerLeg, 2) - Math.pow(rotDistance, 2)) / (2 * m_upperLeg * m_lowerLeg) );
		else
			s2 = Math.acos( (Math.pow(m_upperLeg, 2) + Math.pow(m_lowerLeg, 2) - Math.pow(rotDistance, 2)) / (2 * m_upperLeg * m_lowerLeg) );
		
		
		if(!(Double.isNaN(s0) || Double.isNaN(s1) || Double.isNaN(s2) || Double.isInfinite(s0) || Double.isInfinite(s1) || Double.isInfinite(s2))) {
			m_servos[0].setGoalPosition(s0);
			m_servos[1].setGoalPosition(s1);
			m_servos[2].setGoalPosition(s2);
			
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
	
	public SingleServo[] getServos() {
		return m_servos;
	}
}
