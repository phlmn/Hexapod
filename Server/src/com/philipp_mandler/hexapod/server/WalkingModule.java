package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.*;

public class WalkingModule extends Module implements NetworkingEventListener {

	private Vec2 m_speed = new Vec2();
	private double m_rotSpeed = 0.0;
	private Leg m_legs[];
	private Vec3[] m_endPositions = new Vec3[6];
	private double m_speedFactor;

	private LegUpdater m_legUpdater;

	private int m_caseStep[] = {5, 2, 3, 6, 1, 4};

	private Vec2 m_defaultPositions[] = new Vec2[] {
			new Vec2(-200, 310),
			new Vec2(200, 310),
			new Vec2(-300, 0),
			new Vec2(300, 0),
			new Vec2(-200, -310),
			new Vec2(200, -310)
	};

	private boolean m_lifted;

	private Time m_stepTime = new Time();


	public WalkingModule() {

		super.setName("walking");

		m_legUpdater = new LegUpdater();


		ActuatorManager acs = Main.getActuatorManager();

		m_legs = new Leg[6];

		m_legs[1] = new Leg(1, Data.upperLeg, Data.lowerLeg, new Vec2(90, 210), -1.0122f + Math.PI, acs.getLegServo(1, 0), acs.getLegServo(1, 1), acs.getLegServo(1, 2), true);
		m_legs[3] = new Leg(3, Data.upperLeg, Data.lowerLeg, new Vec2(130, 0), Math.PI, acs.getLegServo(3, 0), acs.getLegServo(3, 1), acs.getLegServo(3, 2), true);
		m_legs[5] = new Leg(5, Data.upperLeg, Data.lowerLeg, new Vec2(90, -210), 1.0122f + Math.PI, acs.getLegServo(5, 0), acs.getLegServo(5, 1), acs.getLegServo(5, 2), true);


		m_legs[0] = new Leg(0, Data.upperLeg, Data.lowerLeg, new Vec2(-90, 210), -1.0122f, acs.getLegServo(0, 0), acs.getLegServo(0, 1), acs.getLegServo(0, 2), false);
		m_legs[2] = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-130, 0), 0, acs.getLegServo(2, 0), acs.getLegServo(2, 1), acs.getLegServo(2, 2), false);
		m_legs[4] = new Leg(4, Data.upperLeg, Data.lowerLeg, new Vec2(-90, -210), 1.0122f, acs.getLegServo(4, 0), acs.getLegServo(4, 1), acs.getLegServo(4, 2), false);


		for(Leg leg : m_legs) {
			m_legUpdater.addLeg(leg);
		}

	}

	@Override
	public void onStart() {

		for(int i = 0; i < 6; i++) {
			m_legs[i].setGoalPosition(new Vec3(m_defaultPositions[i], 20));
			m_endPositions[i] = new Vec3(new Vec3(m_defaultPositions[i], 0));
		}

		m_speedFactor = 0.2;

		m_legUpdater.start();

		Main.getNetworking().addEventListener(this);
	}

	@Override
	public void onStop() {
		Main.getNetworking().removeEventListener(this);
		m_legUpdater.stop();
	}

	@Override
	public void tick(Time elapsedTime) {
		if(!m_lifted) {
			int readyLegs = 0;
			for(Leg leg : m_legs) {
				double m_preferredHeight = 140;
				double moveDist = ((leg.getGoalPosition().getZ() + m_preferredHeight) / 2 + 10) * elapsedTime.getSeconds();
				if(leg.getGoalPosition().getZ() - moveDist > -m_preferredHeight)
					leg.transform(new Vec3(0, 0, -moveDist));
				else {
					leg.getGoalPosition().setZ(-m_preferredHeight);
					readyLegs++;
				}
			}
			if(readyLegs > 5)
				m_lifted = true;
		}
		else {


			double duration = 800 / 6 / m_speedFactor; // duration per case, 800ms per step

			Vec2 speed = new Vec2(m_speed);
			speed.multiply(150 * m_speedFactor); // max 100mm/sec

			double speedR = m_rotSpeed / 2 * m_speedFactor;

            double preferredHeight = 140;
			double stepHeight = 40;

			boolean idle = false;

			if(speed.getLength() < 0.05 && Math.abs(speedR) < 0.01) {

				idle = true;

				for(int legID = 0; legID < 6; legID++) {
					if(new Vec2(m_legs[legID].getGoalPosition().getX(), m_legs[legID].getGoalPosition().getY()).sub(m_defaultPositions[legID]).getLength() > 1) {
						idle = false;
						break;
					}
				}

			}

			if(!idle) {

				for(int legID = 0; legID < 6; legID++) {

					Leg leg = m_legs[legID];
					Vec2 initPos = m_defaultPositions[legID];
					Vec3 pos = new Vec3(leg.getGoalPosition().getX(), leg.getGoalPosition().getY(), leg.getGoalPosition().getZ() + preferredHeight);

					switch(m_caseStep[legID]) {

						case 1: //forward raise

							pos.setX(((m_endPositions[legID].getX() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getX() * m_stepTime.getMilliseconds())) / (duration * 2));
							pos.setY(((m_endPositions[legID].getY() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getY() * m_stepTime.getMilliseconds())) / (duration * 2));

							pos.setZ(Math.sin((m_stepTime.getMilliseconds() / duration) * (Math.PI / 2)) * stepHeight);

							if(m_stepTime.getMilliseconds() >= duration) m_caseStep[legID] = 2;
							break;

						case 2: // forward lower

							pos.setX(((m_endPositions[legID].getX() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getX() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));
							pos.setY(((m_endPositions[legID].getY() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getY() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));

							pos.setZ((Math.cos(((m_stepTime.getMilliseconds() / duration) * Math.PI)) + 1) * (stepHeight / 2));

							if(m_stepTime.getMilliseconds() >= duration) m_caseStep[legID] = 3;
							break;

						case 3: // pull back slowly

							pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
							pos.setZ(0);
							pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

							if(m_stepTime.getMilliseconds() >= duration) m_caseStep[legID] = 4;
							break;

						case 4: // pull back slowly

							pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
							pos.setZ(0);
							pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

							if(m_stepTime.getMilliseconds() >= duration) m_caseStep[legID] = 5;
							break;

						case 5: // pull back slowly

							pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
							pos.setZ(0);
							pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

							if(m_stepTime.getMilliseconds() >= duration) m_caseStep[legID] = 6;
							break;

						case 6: // pull back slowly

							pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
							pos.setZ(0);
							pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

							m_endPositions[legID] = new Vec3(pos.getX(), pos.getY(), pos.getZ());

							if(m_stepTime.getMilliseconds() >= duration) m_caseStep[legID] = 1;
							break;

					}

					leg.setGoalPosition(pos.sum(new Vec3(0, 0, -preferredHeight)));
				}

				if (m_stepTime.getMilliseconds() < duration) m_stepTime.setNanoseconds(m_stepTime.getNanoseconds() + elapsedTime.getNanoseconds());
				else {
					if(m_speed.getLength() < Math.abs(m_rotSpeed)) {
						m_speedFactor = Math.abs(m_rotSpeed) * 0.8 + 0.2;
					}
					else {
						m_speedFactor = m_speed.getLength() * 0.8 + 0.2;
					}
					m_stepTime.setNanoseconds(0);
				}
			}
		}

	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {
		if(pack instanceof JoystickPackage) {
			JoystickPackage joyPack = (JoystickPackage)pack;
			if(joyPack.getType() == JoystickType.Direction) {
				m_speed.set(joyPack.getData().getX(), joyPack.getData().getY());
			}
			else if(joyPack.getType() == JoystickType.Rotation) {
				m_rotSpeed = joyPack.getData().getX();
			}
		}
	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {
		if(cmd.length > 1) {
			if(cmd[0].toLowerCase().equals("walking")) {
				if(cmd[1].toLowerCase().equals("speed")) {
					if(cmd.length > 2) {
						try {
							m_speed.setY(Double.valueOf(cmd[2]));
							DebugHelper.log("Walking speed set to " + m_speed.getY() + ".");
						}
						catch(NumberFormatException e) {
							DebugHelper.log("The last parameter is no valid number.");
						}
					}
				}
				else if(cmd[1].toLowerCase().equals("speedx")) {
					if(cmd.length > 2) {
						try {
							m_speed.setX(Double.valueOf(cmd[2]));
							DebugHelper.log("Walking speed x set to " + m_speed.getX() + ".");
						}
						catch(NumberFormatException e) {
							DebugHelper.log("The last parameter is no valid number.");
						}
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
