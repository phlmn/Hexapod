package com.philipp_mandler.hexapod.server;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.ArrayList;

public class ActuatorManager {

	private ServoController m_servoController = new ServoController();

	private SingleServo[] m_legServos = new SingleServo[18];

	private SingleServo[] m_kinectServos = new SingleServo[2];

	public ActuatorManager(String serialPort, int baudRate) {

		// connect to serial port
		try {
			DebugHelper.log("Connect to serial Port " + serialPort + " with baud rate " + baudRate);
			m_servoController.init(serialPort, baudRate);
		} catch (PortInUseException | UnsupportedCommOperationException | NoSuchPortException | IOException e) {
			e.printStackTrace();
		}

		// initialize leg servos
		for(int i = 0; i < 18; i++) {
			m_legServos[i] = new SingleServo(m_servoController, i + 1, true, 4096, Data.servoAngleOffsets[i]);
		}

		// initialize Kinect servos
		m_kinectServos[0] = new SingleServo(m_servoController, 19, false, 1024, 0, 0.104);
		m_kinectServos[1] = new SingleServo(m_servoController, 20, false, 1024, 0, 0.104);
	}

	public int getServoID(int legID, int servoPos) {
		// calculate leg servo id from leg and position
		return (legID * 3) + servoPos + 1;
	}

	public SingleServo getLegServo(int legID, int servoPos) {
		// return the servo object from leg id and position
		if(-1 <= legID && legID <= 5) {
			if(0 <= servoPos && servoPos <= 2) {
				return m_legServos[getServoID(legID, servoPos) - 1];
			}
		}
		return null;
	}

	public SingleServo getKinectServo(int pos) {
		// return the Kinect servo: 0 - rotate, 1 - tilt
		if(pos == 0 || pos == 1)
			return m_kinectServos[pos];
		return null;
	}

	public void syncUpdateLegServos() {
		// update all leg servos at once
		ArrayList<Integer> values = new ArrayList<>();
		ArrayList<Integer> ids = new ArrayList<>();

		for(SingleServo servo : m_legServos) {
			if(servo.isConnected()) {
				ids.add(servo.getID());
				values.add(servo.getPosValue());
			}
		}

		int[] valueArray = new int[values.size()];
		int[] idArray = new int[ids.size()];

		for(int i = 0; i < ids.size(); i++) {
			idArray[i] = ids.get(i);
		}

		for(int i = 0; i < values.size(); i++) {
			valueArray[i] = values.get(i);
		}

		m_servoController.syncWriteGoalPosition(idArray, valueArray);
	}

}
