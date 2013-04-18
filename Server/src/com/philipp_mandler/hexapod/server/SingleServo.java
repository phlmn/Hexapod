package com.philipp_mandler.hexapod.server;

import SimpleDynamixel.Servo;

public class SingleServo {
	
	private Servo m_controller;
	private int m_servoID;
	private int m_servoResolution;
	private double m_offset;
	private boolean m_connected;
	private int m_goalPosition;
	
	public SingleServo(Servo controller, int id) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = 4096;
		m_offset = 0;
		m_connected = ping();
	}
	
	public SingleServo(Servo controller, int id, int servoResolution) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = servoResolution;
		m_offset = 0;
		m_connected = ping();
	}
	
	public SingleServo(Servo controller, int id, int servoResolution, double offset) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = servoResolution;
		m_offset = offset;
		m_connected = ping();
	}
	
	public void setGoalPosition(double rad) {
		m_goalPosition = (int)((Math.round((rad + m_offset) / (2.0 * Math.PI) * (m_servoResolution - 1.0))) % m_servoResolution);
	}
	
	public int getPosValue()  {
		return m_goalPosition;
	}
	
	public double getGoalPosition() {
		if(m_connected)
			return m_controller.goalPosition(m_servoID) * (2.0 * Math.PI) / (m_servoResolution - 1.0);
		return -1;
	}
	
	public boolean setMaxTorque(int torque) {
		if(m_connected)
			return m_controller.setMaxTorque(m_servoID, torque);
		return false;
	}
	
	public int getMaxTorque() {
		if(m_connected)
			return m_controller.maxTorque(m_servoID);
		return -1;
	}
	
	public int getCurrentTemp() {
		if(m_connected)
			return m_controller.presentTemp(m_servoID);
		return -1;
	}
	
	public int getCurrentLoad() {
		if(m_connected)
			return m_controller.presentLoad(m_servoID);
		return -1;
	}
	
	public int getCurrentVoltage() {
		if(m_connected)
			return m_controller.presentVolt(m_servoID);
		return -1;
	}
	
	public int getCurrentSpeed() {
		if(m_connected)
			return m_controller.presentSpeed(m_servoID);
		return -1;
	}
	
	public double getCurrentPosition() {
		if(m_connected)
			return (m_controller.presentPosition(m_servoID) / (m_servoResolution - 1)) * (2 * Math.PI);
		return -1;
	}
	
	public boolean setMoovingSpeed(int speed) {
		if(m_connected)
			return m_controller.setMovingSpeed(m_servoID, speed);
		return false;
	}
	
	public int getMovingSpeed() {
		if(m_connected)
			return m_controller.movingSpeed(m_servoID);
		return -1;
	}
	
	public boolean isMoving() {
		if(m_connected)
			return m_controller.moving(m_servoID);
		return false;
	}
	
	public boolean setTorqueEnabled(boolean enable) {
		if(m_connected)
			return m_controller.setTorqueEnable(m_servoID, enable);
		return false;
	}
	
	public boolean getTorqueEnabled() {
		if(m_connected)
			return m_controller.torqueEnable(m_servoID);
		return false;
	}
	
	public void setServoResolution(int res) {
		m_servoResolution = res;
	}
	
	public int getServoResolution() {
		return m_servoResolution;
	}

	public int getBaudrate() {
		if(m_connected)
			return m_controller.baudrate(m_servoID);
		return -1;
	}
	
	public boolean setTempLimit(int temp) {
		if(m_connected)
			return m_controller.setHighLimitTemp(m_servoID, temp);
		return false;
	}
	
	public int getTempLimit() {
		if(m_connected)
			return m_controller.highLimitTemp(m_servoID);
		return -1;
	}
	
	public int getVoltageLimitLow() {
		if(m_connected)
			return m_controller.lowLimitVolt(m_servoID);
		return -1;
	}
	
	public int getVoltageLimitHigh() {
		if(m_connected)
			return m_controller.highLimitVolt(m_servoID);
		return -1;
	}
	
	public boolean setVoltageLimitLow(int voltage) {
		if(m_connected)
			return m_controller.setLowLimitVolt(m_servoID, voltage);
		return false;
	}
	
	public boolean setVoltageLimitHigh(int voltage) {
		if(m_connected)
			return m_controller.setHightLimitVolt(m_servoID, voltage);
		return false;
	}

	public boolean setLed(boolean enable) {
		if(m_connected)
			return m_controller.setLed(m_servoID, enable);
		return false;
	}
	
	public boolean getLed() {
		if(m_connected)
			return m_controller.led(m_servoID);
		return false;
	}
	
	public void setID(int servoID) {
		m_servoID = servoID;
		m_connected = ping();
	}
		
	public int getID() {
		return m_servoID;
	}

	public boolean setHardwareID(int hardwareID) {
		if(m_controller.setId(m_servoID, hardwareID)) {
			m_servoID = hardwareID;
			return true;
		}
		return false;		
	}
	
	public boolean ping() {
		return m_connected = m_controller.ping(m_servoID);		
	}
	
	public boolean isConnected() {
		return m_connected;
	}
}
