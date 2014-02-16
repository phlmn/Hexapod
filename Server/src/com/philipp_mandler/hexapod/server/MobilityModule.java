package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.*;

import java.util.ArrayList;

public class MobilityModule extends Module implements NetworkingEventListener {

	private Vec2 m_speed = new Vec2();
	private double m_rotSpeed = 0.0;
	private Leg m_legs[];
	private Vec2[] m_endPositions = new Vec2[6];
	private double m_speedFactor;
	private Vec3 m_rotation = new Vec3();
	private Vec3 m_groundRotation = new Vec3();
	private boolean m_tilt = false;
	private ButtonGroup m_buttonGroup;

	private boolean m_groundAdaption = false;

	private ServoLoadReader m_loadReader;

	private double m_loadOffsets[] = new double[6];

	private LegUpdater m_legUpdater;

	private int m_caseStepRipple[] = {5, 2, 3, 6, 1, 4};
	private int m_caseStepTripod[] = {1, 3, 3, 1, 1, 3};

	private WalkingGait m_walkingGait = WalkingGait.Ripple;
	private int m_mode = 3; // 0: lifting, 1: dropping, 2: lifted, 3: dropped

	private Vec2 m_defaultPositions[] = new Vec2[] {
			new Vec2(-200, 310),
			new Vec2(200, 310),
			new Vec2(-300, 0),
			new Vec2(300, 0),
			new Vec2(-200, -310),
			new Vec2(200, -310)
	};

	private Vec3 m_currentWalkPositions[] = new Vec3[6];

	private Time m_stepTime = new Time();

	private ArrayList<Vec3> m_lastGroundRotation = new ArrayList<>();


	public MobilityModule() {

		super.setName("mobility");

		m_buttonGroup = new ButtonGroup(getName(), "Mobility Module");
		m_buttonGroup.addButton(new Button("lift", "Lift", getName() + " lift"));
		m_buttonGroup.addButton(new Button("drop", "Drop", getName() + " drop"));
		m_buttonGroup.addButton(new Button("toggle-tilt", "Tilt", getName() + " toggle-tilt"));
		m_buttonGroup.addButton(new Button("toggle-groundadaption", "Ground Adaption", getName() + " toggle-groundadaption"));

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

		m_loadReader = new ServoLoadReader();

		for(int i = 0; i < 6; i++) {
			m_loadReader.registerServo(acs.getLegServo(i, 1));
		}

	}

	@Override
	public void onStart() {

		for(int i = 0; i < 6; i++) {
			m_legs[i].setGoalPosition(new Vec3(m_defaultPositions[i], 20));
			m_currentWalkPositions[i] = new Vec3(m_defaultPositions[i], 20);
			m_endPositions[i] = new Vec2(m_defaultPositions[i]);
			m_loadOffsets[i] =  0.0;
		}

		m_speedFactor = 0.2;

		m_legUpdater.start();

		m_lastGroundRotation.clear();
		for(int i = 0; i < 400; i++) {
			m_lastGroundRotation.add(new Vec3());
		}

		Main.getNetworking().addEventListener(this);
		Main.getNetworking().addButtonGroup(m_buttonGroup);

		m_loadReader.start();
	}

	@Override
	public void onStop() {
		Main.getNetworking().removeButtonGroup(m_buttonGroup);
		Main.getNetworking().removeEventListener(this);
		m_legUpdater.stop();

		m_loadReader.stop();
	}

	@Override
	public void tick(Time elapsedTime) {

		if(m_mode == 0) { // lifting
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
				m_mode = 2;
		}
		else if(m_mode == 1) { // dropping
			int readyLegs = 0;
			for(Leg leg : m_legs) {
				double m_preferredHeight = -10;
				double moveDist = ((leg.getGoalPosition().getZ() + m_preferredHeight) / 2 - 10) * elapsedTime.getSeconds();
				if(leg.getGoalPosition().getZ() + moveDist < -m_preferredHeight)
					leg.transform(new Vec3(0, 0, -moveDist));
				else {
					leg.getGoalPosition().setZ(-m_preferredHeight);
					readyLegs++;
				}
			}
			if(readyLegs > 5)
				m_mode = 4;
		}
		else if(m_mode == 2) { // lifted
			if(m_groundAdaption) {
				Vec3 gravity = new Vec3(Main.getSensorManager().getLevel().getZ(), Main.getSensorManager().getLevel().getX(), 0);

				m_lastGroundRotation.add(m_groundRotation.sum(new Vec3(-gravity.getX() * 0.4, gravity.getY() * 0.4, 0)));

				while( m_lastGroundRotation.size() > 300 )
					m_lastGroundRotation.remove(0);

				if(m_lastGroundRotation.size() > 0) {

					Vec3 sum = new Vec3();

					for(Vec3 value : m_lastGroundRotation) {
						sum.add(new Vec3(value.getX(), value.getY(), value.getZ()));
					}

					Vec3 average = sum.divide(m_lastGroundRotation.size());

					m_groundRotation = new Vec3(average.getX(), average.getY(), 0);
				}
			}


			double duration = 800 / 6 / m_speedFactor; // duration per case, 800ms per step

			Vec2 speed = new Vec2(m_speed);
			speed.multiply(150 * m_speedFactor);

			double speedR = m_rotSpeed / 2 * m_speedFactor;

            double preferredHeight = 140;
			double stepHeight = 40;

			if(m_groundAdaption)
				stepHeight = 60;

			boolean idle = false;

			if(speed.getLength() < 0.05 && Math.abs(speedR) < 0.01) {

				idle = true;

				for(int legID = 0; legID < 6; legID++) {
					if(new Vec2(m_currentWalkPositions[legID].getX(), m_currentWalkPositions[legID].getY()).sub(m_defaultPositions[legID]).getLength() > 1) {
						idle = false;
						break;
					}
				}

			}

			if(m_groundAdaption) {

				double sumLoad = 0;

				double legsOnGround = 0;

				for(int i = 0; i < 6; i++) {
					if(m_caseStepRipple[i] != 1 && m_caseStepRipple[i] != 2) {
						legsOnGround++;
						double load = m_loadReader.getLoad(Main.getActuatorManager().getLegServo(i, 1));
						if(m_legs[i].isRightSided()) load = -load;
						Vec2 pos2d = new Vec2(m_legs[i].getRelativeGoalPosition().getX(), m_legs[i].getRelativeGoalPosition().getY());
						load = load / pos2d.getLength();
						sumLoad += load;
					}
				}

				for(int i = 0; i < 6; i++) {
					if(m_caseStepRipple[i] != 1 && m_caseStepRipple[i] != 2) {
						double load = m_loadReader.getLoad(Main.getActuatorManager().getLegServo(i, 1));
						if(m_legs[i].isRightSided()) load = -load;
						Vec2 pos2d = new Vec2(m_legs[i].getRelativeGoalPosition().getX(), m_legs[i].getRelativeGoalPosition().getY());
						load = load / pos2d.getLength();
						double loadError = (sumLoad / legsOnGround) - load;

						m_loadOffsets[i] -= loadError / 2.0;
					}
				}

				double loadOffsetSum = 0;

				for(int i = 0; i < 6; i++) {
					loadOffsetSum += m_loadOffsets[i];
				}


				for(int i = 0; i < 6; i++) {
					m_loadOffsets[i] -= loadOffsetSum / 6;
				}


				double correctionY = ((m_loadOffsets[1] + m_loadOffsets[3] + m_loadOffsets[5]) / 3 - (m_loadOffsets[0] + m_loadOffsets[2] + m_loadOffsets[4]) / 3) / 12;
				m_loadOffsets[0] += correctionY;
				m_loadOffsets[2] += correctionY;
				m_loadOffsets[4] += correctionY;

				m_loadOffsets[1] -= correctionY;
				m_loadOffsets[3] -= correctionY;
				m_loadOffsets[5] -= correctionY;

				double correctionX = ((m_loadOffsets[0] + m_loadOffsets[1] ) / 2 - (m_loadOffsets[4] + m_loadOffsets[5]) / 2) / 12;
				m_loadOffsets[4] += correctionX;
				m_loadOffsets[5] += correctionX;

				m_loadOffsets[0] -= correctionX;
				m_loadOffsets[1] -= correctionX;


				loadOffsetSum = 0;

				for(int i = 0; i < 6; i++) {
					loadOffsetSum += m_loadOffsets[i];
				}


				for(int i = 0; i < 6; i++) {
					m_loadOffsets[i] -= loadOffsetSum / 6;
				}

				for(int i = 0; i < 6; i++) {
					if(m_caseStepRipple[i] == 1 || m_caseStepRipple[i] == 2) {
						m_loadOffsets[i] = 0;
					}
				}
			}





			for(int legID = 0; legID < 6; legID++) {

				Leg leg = m_legs[legID];
				Vec2 initPos = m_defaultPositions[legID];
				Vec3 pos = new Vec3(m_currentWalkPositions[legID]);




				if(!idle) {

					if(m_walkingGait == WalkingGait.Ripple) {

						switch(m_caseStepRipple[legID]) {

							case 1: //forward raise

								pos.setX(((m_endPositions[legID].getX() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getX() * m_stepTime.getMilliseconds())) / (duration * 2));
								pos.setY(((m_endPositions[legID].getY() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getY() * m_stepTime.getMilliseconds())) / (duration * 2));

								pos.setZ(Math.sin((m_stepTime.getMilliseconds() / duration) * (Math.PI / 2)) * stepHeight);

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepRipple[legID] = 2;
								break;

							case 2: // forward lower

								pos.setX(((m_endPositions[legID].getX() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getX() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));
								pos.setY(((m_endPositions[legID].getY() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getY() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));

								pos.setZ((Math.cos(((m_stepTime.getMilliseconds() / duration) * Math.PI)) + 1) * (stepHeight / 2));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepRipple[legID] = 3;
								break;

							case 3: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepRipple[legID] = 4;
								break;

							case 4: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepRipple[legID] = 5;
								break;

							case 5: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepRipple[legID] = 6;
								break;

							case 6: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								m_endPositions[legID] = new Vec2(pos.getX(), pos.getY());

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepRipple[legID] = 1;
								break;

						}
					}
					else if(m_walkingGait == WalkingGait.Tripod) {

						switch(m_caseStepTripod[legID]) {

							case 1: //forward raise

								pos.setX(((m_endPositions[legID].getX() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getX() * m_stepTime.getMilliseconds())) / (duration * 2));
								pos.setY(((m_endPositions[legID].getY() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getY() * m_stepTime.getMilliseconds())) / (duration * 2));

								pos.setZ(Math.sin((m_stepTime.getMilliseconds() / duration) * (Math.PI / 2)) * stepHeight);

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepTripod[legID] = 2;
								break;

							case 2: // forward lower

								pos.setX(((m_endPositions[legID].getX() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getX() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));
								pos.setY(((m_endPositions[legID].getY() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getY() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));

								pos.setZ((Math.cos(((m_stepTime.getMilliseconds() / duration) * Math.PI)) + 1) * (stepHeight / 2));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepTripod[legID] = 3;
								break;

							case 3: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds() * 3, -speed.getY() * elapsedTime.getSeconds() * 3, 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds() * 1));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepTripod[legID] = 4;
								break;

							case 4: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds() * 3, -speed.getY() * elapsedTime.getSeconds() * 3, 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds() * 1));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepTripod[legID] = 1;
								break;

						}
					}

					m_currentWalkPositions[legID] = new Vec3(pos);

				}
				if(idle) pos.setZ(0);
				if(m_tilt) pos.rotate(m_rotation);
				if(m_groundAdaption) pos.rotate(m_groundRotation);
				if(m_groundAdaption) pos.add(new Vec3(0, 0, m_loadOffsets[legID]));
				leg.setGoalPosition(pos.sum(new Vec3(0, 0, -preferredHeight)));

			}

			if(!idle) {
				if (m_stepTime.getMilliseconds() < duration) m_stepTime = Time.fromNanoseconds(m_stepTime.getNanoseconds() + elapsedTime.getNanoseconds());
				else {
					if(m_speed.getLength() < Math.abs(m_rotSpeed)) {
						m_speedFactor = Math.abs(m_rotSpeed) * 0.8 + 0.2;
					}
					else {
						m_speedFactor = m_speed.getLength() * 0.8 + 0.2;
					}
					m_stepTime = Time.fromNanoseconds(0);
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
		else if(pack instanceof RotationPackage) {
			RotationPackage rotPack = (RotationPackage)pack;

			Vec3 rawRot = rotPack.getValue();

			m_rotation.setX(rawRot.getX() / 2);
			m_rotation.setY(-rawRot.getY() / 2);

		}
	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {
		if(cmd.length > 1) {
			if(cmd[0].toLowerCase().equals(getName())) {
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
				else if(cmd[1].toLowerCase().equals("drop")) {
					if(m_mode != 3) m_mode = 1;
					Main.getNetworking().broadcast(new NotificationPackage("Dropping..."));
				}
				else if(cmd[1].toLowerCase().equals("lift")) {
					if(m_mode != 2) m_mode = 0;
					Main.getNetworking().broadcast(new NotificationPackage("Lifting..."));
				}
				else if(cmd[1].toLowerCase().equals("tilt")) {
					if(cmd.length > 2) {
						if(cmd[2].toLowerCase().equals("on")) {
							m_tilt = true;
							Main.getNetworking().broadcast(new NotificationPackage("Tilting activated."));
						}
						else if(cmd[2].toLowerCase().equals("off")) {
							m_tilt = false;
							Main.getNetworking().broadcast(new NotificationPackage("Tilting deactivated."));
						}
					}
				}
				else if(cmd[1].toLowerCase().equals("toggle-tilt")) {
					m_tilt = !m_tilt;
					if(m_tilt) Main.getNetworking().broadcast(new NotificationPackage("Tilting activated."));
					else Main.getNetworking().broadcast(new NotificationPackage("Tilting deactivated."));
				}
				else if(cmd[1].toLowerCase().equals("toggle-groundadaption")) {
					m_lastGroundRotation.clear();
					for(int i = 0; i < 400; i++) {
						m_lastGroundRotation.add(new Vec3());
					}
					for(int i = 0; i < 6; i++) {
						m_loadOffsets[i] =  0.0;
					}

					m_groundAdaption = !m_groundAdaption;
					if(m_groundAdaption) Main.getNetworking().broadcast(new NotificationPackage("Adaption activated."));
					else Main.getNetworking().broadcast(new NotificationPackage("Adaption deactivated."));
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
