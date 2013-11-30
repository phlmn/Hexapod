package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec3;

public class LegTest extends Module {

	private Leg m_leg;

	public LegTest() {

		setName("LegTest");

		ActuatorManager acs = Main.getActuatorManager();

		//m_leg = new Leg(1, Data.upperLeg, Data.lowerLeg, new Vec2(90, 210), -1.0122f + Math.PI, acs.getLegServo(1, 0), acs.getLegServo(1, 1), acs.getLegServo(1, 2), true);

		//m_leg = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-130, 0), 0, acs.getLegServo(2, 0), acs.getLegServo(2, 1), acs.getLegServo(2, 2), false);


		m_leg = new Leg(0, Data.upperLeg, Data.lowerLeg, new Vec2(-90, 210), -1.0122f, acs.getLegServo(0, 0), acs.getLegServo(0, 1), acs.getLegServo(0, 2), false);


		acs.getLegServo(0, 2).setOffset(-0.6);
		acs.getLegServo(0, 1).setOffset(-0.32);

		acs.getLegServo(1, 2).setOffset(0.6);
		acs.getLegServo(1, 1).setOffset(0.32);

		acs.getLegServo(2, 2).setOffset(-0.6);
		acs.getLegServo(2, 1).setOffset(-0.32);

		acs.getLegServo(3, 2).setOffset(0.6);
		acs.getLegServo(3, 1).setOffset(0.32);

		acs.getLegServo(4, 2).setOffset(-0.6);
		acs.getLegServo(4, 1).setOffset(-0.32);

		acs.getLegServo(5, 2).setOffset(0.6);
		acs.getLegServo(5, 1).setOffset(0.32);

//		m_legs[3] = new Leg(3, Data.upperLeg, Data.lowerLeg, new Vec2(130, 0), Math.PI, acs.getLegServo(3, 0), acs.getLegServo(3, 1), acs.getLegServo(3, 2), true);
//		m_legs[5] = new Leg(5, Data.upperLeg, Data.lowerLeg, new Vec2(90, -210), 1.0122f + Math.PI, acs.getLegServo(5, 0), acs.getLegServo(5, 1), acs.getLegServo(5, 2), true);
//
//
//		m_legs[0] = new Leg(0, Data.upperLeg, Data.lowerLeg, new Vec2(-90, 210), -1.0122f, acs.getLegServo(0, 0), acs.getLegServo(0, 1), acs.getLegServo(0, 2), false);
		//m_leg = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-130, 0), 0, acs.getLegServo(2, 0), acs.getLegServo(2, 1), acs.getLegServo(2, 2), false);
//		m_legs[4] = new Leg(4, Data.upperLeg, Data.lowerLeg, new Vec2(-90, -210), 1.0122f, acs.getLegServo(4, 0), acs.getLegServo(4, 1), acs.getLegServo(4, 2), false);

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
		if(cmd[0].equals("leg")) {
			if(cmd.length > 1) {


				if(cmd[1].equals("reset")) {
					m_leg.moveLegToRelativePosition(new Vec3(-120, 0, -90));
					Main.getActuatorManager().syncUpdateLegServos();
					DebugHelper.log("Servo 1: " + Main.getActuatorManager().getLegServo(0, 0).getGoalPosition());
					DebugHelper.log("Servo 2: " + Main.getActuatorManager().getLegServo(0, 1).getGoalPosition());
					DebugHelper.log("Servo 3: " + Main.getActuatorManager().getLegServo(0, 2).getGoalPosition());
				}

				if(cmd[1].equals("set")) {
					m_leg.moveLegToRelativePosition(new Vec3(-140, 0, Integer.parseInt(cmd[2])));
					Main.getActuatorManager().syncUpdateLegServos();
					DebugHelper.log("Servo 1: " + Main.getActuatorManager().getLegServo(0, 0).getGoalPosition());
					DebugHelper.log("Servo 2: " + Main.getActuatorManager().getLegServo(0, 1).getGoalPosition());
					DebugHelper.log("Servo 3: " + Main.getActuatorManager().getLegServo(0, 2).getGoalPosition());
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
