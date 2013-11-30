package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class CalibrationModule extends Module {

	private SingleServo m_currentServo;

	public CalibrationModule() {
		setName("calibration");
	}

	@Override
	protected void onStart() {
	}

	@Override
	protected void onStop() {
	}

	@Override
	public void tick(Time elapsedTime) {
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {
	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {
		if(cmd[0].equals("calibrate")) {
			if(cmd.length > 1) {
				if(cmd[1].equals("set")) {
					if(m_currentServo != null) {
						m_currentServo.setOffset(0);

						double value = -m_currentServo.getCurrentPosition();
						Main.getConfig().setServoOffset(m_currentServo.getID(), value);
						Main.getConfig().save();
						m_currentServo.setOffset(value);

						client.sendText("New offset of Servo " + m_currentServo.getID() + " set to " + value);

						m_currentServo.setLed(false);
						m_currentServo = null;
					}
				}
				if(cmd[1].equals("stop")) {
					if(m_currentServo != null) {
						m_currentServo.setLed(false);
						m_currentServo = null;
					}
				}
				else if(cmd.length > 2) {

					if(m_currentServo != null) {
						m_currentServo.setLed(false);
						m_currentServo = null;
					}

					try {
						int legID = Integer.parseInt(cmd[1]);
						int servoPos = Integer.parseInt(cmd[2]);

						m_currentServo = Main.getActuatorManager().getLegServo(legID, servoPos);

						if(m_currentServo != null) {
							m_currentServo.setTorqueEnabled(false);
							m_currentServo.setLed(true);
						}
					}
					catch (NumberFormatException e) {
						client.sendText("First and the second parameter has to be a number.");
					}
				}
			}
		}
	}

	@Override
	public void onClientDisconnected(ClientWorker client) {
	}

	@Override
	public void onClientConnected(ClientWorker client) {
	}
}
