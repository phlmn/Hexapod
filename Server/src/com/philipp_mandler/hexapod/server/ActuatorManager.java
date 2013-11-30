package com.philipp_mandler.hexapod.server;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.util.ArrayList;

public class ActuatorManager {

	private ServoController m_servoController = new ServoController();

	private SingleServo[] m_legServos = new SingleServo[18];

	public ActuatorManager(String serialPort, int baudRate) {
		try {
			DebugHelper.log("Connect to serial Port " + serialPort + " with baud rate " + baudRate);
			m_servoController.init(serialPort, baudRate);
		} catch (PortInUseException | UnsupportedCommOperationException | NoSuchPortException | IOException e) {
			e.printStackTrace();
		}

		for(int i = 0; i < 18; i++) {
			m_legServos[i] = new SingleServo(m_servoController, i + 1, 4096, Main.getConfig().getServoOffset(i + 1));
		}
	}

	public int getServoID(int legID, int servoPos) {
		return (legID * 3) + servoPos + 1;
	}

	public SingleServo getLegServo(int legID, int servoPos) {
		if(-1 <= legID && legID <= 5) {
			if(0 <= servoPos && servoPos <= 2) {
				return m_legServos[getServoID(legID, servoPos) - 1];
			}
		}
		return null;
	}

	public void syncUpdateLegServos() {

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
