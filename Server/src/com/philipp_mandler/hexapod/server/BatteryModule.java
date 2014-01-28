package com.philipp_mandler.hexapod.server;


import com.philipp_mandler.hexapod.hexapod.BatteryPackage;
import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class BatteryModule extends Module {

	private SingleServo m_servo = Main.getActuatorManager().getLegServo(0, 0);
	private Time m_time = new Time();

	public BatteryModule() {
		super.setName("battery");
	}

	@Override
	protected void onStart() {
		m_time = Time.fromNanoseconds(0);
	}

	@Override
	protected void onStop() {

	}

	@Override
	public void tick(Time elapsedTime) {
		m_time = Time.fromNanoseconds(m_time.getNanoseconds() + elapsedTime.getNanoseconds());
		if(m_time.getSeconds() > 10) {
			m_time = Time.fromNanoseconds(0);
			double charge = (m_servo.getCurrentVoltage() - 10.5) / 4.0;
			DebugHelper.log("Battery: " + charge);
			Main.getNetworking().broadcast(new BatteryPackage(charge));
		}
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
