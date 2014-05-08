package com.philipp_mandler.hexapod.server;

public class SingleServo {
	
	private ServoController m_controller;
	private int m_servoID;
	private int m_servoResolution;
	private double m_offset;
	private double m_deadZone;
	private boolean m_connected;
	private int m_goalPosition;
	private boolean m_sync = false;
	private double m_goalPositionRad;
	
	public SingleServo(ServoController controller, int id, boolean sync) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = 4096;
		m_offset = 0;
		m_sync = sync;
		m_connected = ping();
	}
	
	public SingleServo(ServoController controller, int id, boolean sync, int servoResolution) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = servoResolution;
		m_offset = 0;
		m_sync = sync;
		m_connected = ping();
	}
	
	public SingleServo(ServoController controller, int id, boolean sync, int servoResolution, double offset) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = servoResolution;
		m_offset = offset;
		m_sync = sync;
		m_connected = ping();
	}

	public SingleServo(ServoController controller, int id, boolean sync, int servoResolution, double offset, double deadZone) {
		m_controller = controller;
		m_servoID = id;
		m_servoResolution = servoResolution;
		m_offset = offset;
		m_deadZone = deadZone;
		m_sync = sync;
		m_connected = ping();
	}
	
	public void setGoalPosition(double rad) {
		m_goalPositionRad = rad;
		m_goalPosition = (int)Math.round((((rad / (Math.PI * 2)) * (2 * Math.PI) - (m_deadZone / 2 * Math.PI)) + m_offset) / (2.0 * Math.PI) * (m_servoResolution - 1.0)) % m_servoResolution;

		if(!m_sync)
			m_controller.setGoalPosition(m_servoID, m_goalPosition);
	}
	
	public int getPosValue()  {
		return m_goalPosition;
	}

	public void setOffset(double offset) {
		m_offset = offset;
	}

	public double getOffset() {
		return m_offset;
	}

	public void setDeadZone(double deadZone) {
		m_deadZone = deadZone;
	}

	public double getDeadZone() {
		return m_deadZone;
	}
	
	public double getGoalPosition() {
		if(m_connected)
			// TODO: Fix it (offset)!!
			return (m_controller.goalPosition(m_servoID) * (2.0 * Math.PI) / (m_servoResolution - 1.0));
		return -1;
	}

	public double getLastGoalPosition() {
		return m_goalPositionRad;
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
		if(m_connected) {
			int load = m_controller.presentLoad(m_servoID);

			if(load > 1023)
				return 1024 - load;

			return load;
		}
		return -1;
	}
	
	public double getCurrentVoltage() {
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
			//TODO: fix
			//return ((double)m_controller.presentPosition(m_servoID) / (m_servoResolution - 1)) * (2 * Math.PI);
			return ((2 * Math.PI * (m_controller.presentPosition(m_servoID) + 0.25 * (m_deadZone - 0.63661977236758 * m_offset) * (m_servoResolution - 1.0))) / (m_servoResolution - 1.0));
		return -1;
	}
	
	public boolean setMovingSpeed(int speed) {
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
		if(m_controller.initialized())
			return m_connected = m_controller.ping(m_servoID);
		else
			return false;
	}
	
	public boolean isConnected() {
		return m_connected;
	}
}
