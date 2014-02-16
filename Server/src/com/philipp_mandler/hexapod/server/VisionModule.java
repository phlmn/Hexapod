package com.philipp_mandler.hexapod.server;


import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class VisionModule extends Module {

	private SingleServo m_servoRotate = Main.getActuatorManager().getKinectServo(0);
	private SingleServo m_servoTilt = Main.getActuatorManager().getKinectServo(1);

	public VisionModule() {
		super.setName("vision");
	}

	@Override
	protected void onStart() {
		m_servoRotate.setGoalPosition(Math.PI + 0.2);
		m_servoTilt.setGoalPosition(Math.PI);
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

	}

	@Override
	public void onClientDisconnected(ClientWorker client) {

	}

	@Override
	public void onClientConnected(ClientWorker client) {

	}
}
