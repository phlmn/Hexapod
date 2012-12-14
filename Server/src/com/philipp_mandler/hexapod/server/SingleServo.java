package com.philipp_mandler.hexapod.server;

import SimpleDynamixel.Servo;

public class SingleServo {
	
	private Servo m_controller;
	private int m_servoID;
	private int m_servoResolution;
	private double m_offset;
	
	public SingleServo(Servo controller, int id) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = 4096;
		m_offset = 0;
	}
	
	public SingleServo(Servo controller, int id, int servoResolution) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = servoResolution;
		m_offset = 0;
	}
	
	public SingleServo(Servo controller, int id, int servoResolution, double offset) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = servoResolution;
		m_offset = offset;
	}
	
	public void setGoalPosition(double rad) {
		m_controller.setGoalPosition(m_servoID, (int)Math.round((rad + m_offset) / (2.0 * Math.PI) * (m_servoResolution - 1.0)));
	}
	
	public void getGoalPosition() {
		m_controller.goalPosition(m_servoID);
	}
	
	public void setMaxTorque(int torque) {
		m_controller.setMaxTorque(m_servoID, torque);
	}
	
	public int getMaxTorque() {
		return m_controller.maxTorque(m_servoID);
	}
	
	public int getCurrentTemp() {
		return m_controller.presentTemp(m_servoID);
	}
	
	public int getCurrentLoad() {
		return m_controller.presentLoad(m_servoID);
	}
	
	public int getCurrentVoltage() {
		return m_controller.presentVolt(m_servoID);
	}
	
	public int getCurrentSpeed() {
		return m_controller.presentSpeed(m_servoID);
	}
	
	public double getCurrentPosition() {
		return (m_controller.presentPosition(m_servoID) / (m_servoResolution - 1)) * (2 * Math.PI);
	}
	
	public void setMoovingSpeed(int speed) {
		m_controller.setMovingSpeed(m_servoID, speed);
	}
	
	public int getMovingSpeed() {
		return m_controller.movingSpeed(m_servoID);
	}
	
	public boolean isMoving() {
		return m_controller.moving(m_servoID);
	}
	
	public void setTorqueEnabled(boolean enable) {
		m_controller.setTorqueEnable(m_servoID, enable);
	}
	
	public boolean getTorqueEnabled() {
		return m_controller.torqueEnable(m_servoID);
	}
	
	public void setServoResolution(int res) {
		m_servoResolution = res;
	}
	
	public int getServoResolution() {
		return m_servoResolution;
	}

	public int getBaudrate() {
		return m_controller.baudrate(m_servoID);
	}
	
	public void setTempLimit(int temp) {
		m_controller.setHighLimitTemp(m_servoID, temp);
	}
	
	public int getTempLimit() {
		return m_controller.highLimitTemp(m_servoID);
	}
	
	public int getVoltageLimitLow() {
		return m_controller.lowLimitVolt(m_servoID);
	}
	
	public int getVoltageLimitHigh() {
		return m_controller.highLimitVolt(m_servoID);
	}
	
	public void setVoltageLimitLow(int voltage) {
		m_controller.setLowLimitVolt(m_servoID, voltage);
	}
	
	public void setVoltageLimitHigh(int voltage) {
		m_controller.setHightLimitVolt(m_servoID, voltage);
	}

	public boolean setLed(boolean enable) {
		return m_controller.setLed(m_servoID, enable);
	}
	
	public boolean getLed() {
		return m_controller.led(m_servoID);
	}
	
	public void setID(int servoID) {
		m_servoID = servoID;
	}
		
	public int getID() {
		return m_servoID;
	}

	public void setHardwareID(int hardwareID) {
		if(m_controller.setId(m_servoID, hardwareID))
			m_servoID = hardwareID;
		
	}
}
