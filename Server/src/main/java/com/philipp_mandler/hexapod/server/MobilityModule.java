package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.*;
import com.philipp_mandler.hexapod.hexapod.net.JoystickPackage;
import com.philipp_mandler.hexapod.hexapod.net.NetPackage;
import com.philipp_mandler.hexapod.hexapod.net.NotificationPackage;
import com.philipp_mandler.hexapod.hexapod.net.RotationPackage;

import java.util.ArrayList;
import java.util.List;

public class MobilityModule extends Module implements NetworkingEventListener {

	private Vec2 m_speed = new Vec2();
	private double m_rotSpeed = 0.0;
	private Leg m_legs[];
	private Vec2[] m_endPositions = new Vec2[6];
	private double m_speedFactor;
	private Vec3 m_rotationGoal = new Vec3();
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
	private int m_caseStepWave[] = {1, 3, 5, 7, 9, 11};

	private WalkingGait m_walkingGait = WalkingGait.Ripple;

	private WalkingGait m_switchGait = null;

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

	private List<Vec3> m_lastGroundRotation = new ArrayList<>();

	private TimeTracker m_timeTracker;


	public MobilityModule() {

		super.setName("mobility");

		// create time trackers
		m_timeTracker = Main.getTimeManager().createTracker("mobility");

		m_buttonGroup = new ButtonGroup(getName(), "Mobility Module");
		m_buttonGroup.addButton(new Button("lift", "Lift", getName() + " lift"));
		m_buttonGroup.addButton(new Button("drop", "Drop", getName() + " drop"));
		m_buttonGroup.addButton(new Button("gait-ripple", "Ripple Gait", getName() + " gait ripple"));
		m_buttonGroup.addButton(new Button("gait-tripod", "Tripod Gait", getName() + " gait tripod"));
		m_buttonGroup.addButton(new Button("gait-wave", "Wave Gait", getName() + " gait wave"));
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

	}

	@Override
	public void onStart() {

		for(int i = 0; i < 6; i++) {
			m_legs[i].setGoalPosition(new Vec3(m_defaultPositions[i], 20));
			m_currentWalkPositions[i] = new Vec3(m_defaultPositions[i], 20);
			m_endPositions[i] = new Vec2(m_defaultPositions[i]);
			m_loadOffsets[i] = 0.0;
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
	public void tick(long tick, Time elapsedTime) {

		m_timeTracker.startTracking(tick);

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

			TimeTrackerAction action;

			if(m_groundAdaption) {
				action = m_timeTracker.trackAction("level");

				Vec3 raw = Main.getSensorManager().getLevel();

				Vec3 gravity = new Vec3(raw.getZ(), raw.getX(), 0);

				m_lastGroundRotation.add(m_groundRotation.sum(new Vec3(-gravity.getX() * 0.4, gravity.getY() * 0.4, 0)));

				while( m_lastGroundRotation.size() > 1000 )
					m_lastGroundRotation.remove(0);

				if(m_lastGroundRotation.size() > 0) {

					Vec3 sum = new Vec3();

					for(Vec3 value : m_lastGroundRotation) {
						sum.add(new Vec3(value.getX(), value.getY(), value.getZ()));
					}

					Vec3 average = sum.divide(m_lastGroundRotation.size());

					m_groundRotation = new Vec3(average.getX(), average.getY(), 0);
				}

				action.stopTracking();
			}

			action = m_timeTracker.trackAction("prepare walking");


			double duration = 800 / 6 / m_speedFactor; // duration per case, 800ms per step

			Vec2 speed = new Vec2(m_speed);
			speed.multiply(150 * m_speedFactor);

			double speedR = m_rotSpeed / 2 * m_speedFactor;

            double preferredHeight = 140;
			double stepHeight = 40;

			if(m_groundAdaption)
				stepHeight = 60;

			if(m_switchGait != null) {
				m_speed = new Vec2();
				speedR = 0;
			}

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

			if(m_switchGait != null && idle) {
				m_walkingGait = m_switchGait;
				m_switchGait = null;
			}

			action.stopTracking();

			if(m_groundAdaption) {

				action = m_timeTracker.trackAction("get leg loads");

				double sumLoad = 0;

				double legsOnGround = 0;

				double loads[] = new double[6];


				for(int i = 0; i < 6; i++) {
					loads[i] = m_loadReader.getLoad(i);
				}

				for(int i = 0; i < 6; i++) {
					if(m_caseStepRipple[i] != 1 && m_caseStepRipple[i] != 2) {
						legsOnGround++;
						double load = loads[i];
						if(m_legs[i].isRightSided()) load = -load;
						Vec2 pos2d = new Vec2(m_legs[i].getRelativeGoalPosition().getX(), m_legs[i].getRelativeGoalPosition().getY());
						load = load / pos2d.getLength();
						sumLoad += load;
						m_loadOffsets[i] = 0;
					}
				}

				for(int i = 0; i < 6; i++) {
					if(m_caseStepRipple[i] != 1 && m_caseStepRipple[i] != 2) {
						double load = loads[i];
						if(m_legs[i].isRightSided()) load = -load;
						Vec2 pos2d = new Vec2(m_legs[i].getRelativeGoalPosition().getX(), m_legs[i].getRelativeGoalPosition().getY());
						load = load / pos2d.getLength();
						double loadError = (sumLoad / legsOnGround) - load;

						m_loadOffsets[i] -= loadError / 2.0;
					}
				}


				action.stopTracking();

				action = m_timeTracker.trackAction("height correction");

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

				action.stopTracking();
			}


			action = m_timeTracker.trackAction("sequencing");

			if(m_walkingGait == WalkingGait.Ripple) {

			}
			else if(m_walkingGait == WalkingGait.Tripod) {
				speed.multiply(3);
				speedR *= 1;
			}
			if(m_walkingGait == WalkingGait.Wave) {
				speed.multiply(0.3);
				speedR *= 0.3;
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

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepTripod[legID] = 4;
								break;

							case 4: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								m_endPositions[legID] = new Vec2(pos.getX(), pos.getY());

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepTripod[legID] = 1;
								break;

						}
					}
					else if(m_walkingGait == WalkingGait.Wave) {

						switch(m_caseStepWave[legID]) {

							case 1: //forward raise

								pos.setX(((m_endPositions[legID].getX() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getX() * m_stepTime.getMilliseconds())) / (duration * 2));
								pos.setY(((m_endPositions[legID].getY() * (duration * 2 - m_stepTime.getMilliseconds())) + (initPos.getY() * m_stepTime.getMilliseconds())) / (duration * 2));

								pos.setZ(Math.sin((m_stepTime.getMilliseconds() / duration) * (Math.PI / 2)) * stepHeight);

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 2;
								break;

							case 2: // forward lower

								pos.setX(((m_endPositions[legID].getX() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getX() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));
								pos.setY(((m_endPositions[legID].getY() * (duration * 2 - (m_stepTime.getMilliseconds() + duration))) + (initPos.getY() * (m_stepTime.getMilliseconds() + duration))) / (duration * 2));

								pos.setZ((Math.cos(((m_stepTime.getMilliseconds() / duration) * Math.PI)) + 1) * (stepHeight / 2));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 3;
								break;

							case 3: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 4;
								break;

							case 4: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 5;
								break;

							case 5: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 6;
								break;

							case 6: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								m_endPositions[legID] = new Vec2(pos.getX(), pos.getY());

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 7;
								break;

							case 7: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 8;
								break;

							case 8: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 9;
								break;

							case 9: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 10;
								break;

							case 10: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								m_endPositions[legID] = new Vec2(pos.getX(), pos.getY());

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 11;
								break;
							case 11: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 12;
								break;

							case 12: // pull back slowly

								pos.add(new Vec3(-speed.getX() * elapsedTime.getSeconds(), -speed.getY() * elapsedTime.getSeconds(), 0));
								pos.setZ(0);
								pos.rotate(new Vec3(0, 0, speedR * elapsedTime.getSeconds()));

								m_endPositions[legID] = new Vec2(pos.getX(), pos.getY());

								if(m_stepTime.getMilliseconds() >= duration) m_caseStepWave[legID] = 1;
								break;

						}
					}

					m_currentWalkPositions[legID] = new Vec3(pos);

				}

				if(idle) pos.setZ(0);

				m_rotation.setX((m_rotationGoal.getX() - m_rotation.getX()) * elapsedTime.getSeconds() + m_rotation.getX());
				m_rotation.setY((m_rotationGoal.getY() - m_rotation.getY()) * elapsedTime.getSeconds() + m_rotation.getY());
				pos.rotate(m_rotation);

				if(m_groundAdaption) pos.rotate(m_groundRotation);
				if(m_groundAdaption) pos.add(new Vec3(0, 0, m_loadOffsets[legID]));
				leg.setGoalPosition(pos.sum(new Vec3(0, 0, -preferredHeight)));

			}

			action.stopTracking();

			action = m_timeTracker.trackAction("step calculation");

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

			action.stopTracking();
		}

		m_timeTracker.stopTracking();

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

			if(m_tilt) {
				m_rotationGoal.setX(rawRot.getX() / 2);
				m_rotationGoal.setY(-rawRot.getY() / 2);
			}
			else {
				m_rotationGoal.setX(0);
				m_rotationGoal.setY(0);
			}
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
					if(!m_groundAdaption) {
						m_tilt = !m_tilt;
						if(m_tilt) Main.getNetworking().broadcast(new NotificationPackage("Tilting activated."));
						else Main.getNetworking().broadcast(new NotificationPackage("Tilting deactivated."));
						if(!m_tilt) m_rotationGoal = new Vec3();
					}
					else Main.getNetworking().broadcast(new NotificationPackage("Error: Ground Adaption enabled."));
				}
				else if(cmd[1].toLowerCase().equals("toggle-groundadaption")) {
					if(Main.getSensorManager().getKinect() != null) {
						m_lastGroundRotation.clear();
						m_tilt = false;
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
					else {
						Main.getNetworking().broadcast(new NotificationPackage("Missing Kinect."));
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
				else if(cmd[1].toLowerCase().equals("gait")) {
					if(cmd.length > 2) {
						if(cmd[2].toLowerCase().equals("ripple")) {
							if(m_walkingGait != WalkingGait.Ripple) m_switchGait = WalkingGait.Ripple;
							Main.getNetworking().broadcast(new NotificationPackage("Switching to ripple gait..."));
						}
						else if(cmd[2].toLowerCase().equals("tripod")) {
							if(m_walkingGait != WalkingGait.Tripod) m_switchGait = WalkingGait.Tripod;
							Main.getNetworking().broadcast(new NotificationPackage("Switching to tripod gait..."));
						}
						else if(cmd[2].toLowerCase().equals("wave")) {
							if(m_walkingGait != WalkingGait.Wave) m_switchGait = WalkingGait.Wave;
							Main.getNetworking().broadcast(new NotificationPackage("Switching to wave gait..."));
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

	public void setWalkingSpeed(Vec2 speed) {
		m_speed = speed;
	}

	public Vec2 getWalkingSpeed() {
		return m_speed;
	}

	public void setRotationSpeed(double speed) {
		if(speed > 1) m_rotSpeed = 1;
		else if(speed < -1) m_rotSpeed = -1;
		else m_rotSpeed = speed;
	}

	public double getRotationSpeed() {
		return m_rotSpeed;
	}

	public boolean lifted() {
		return (m_mode == 2);
	}

	public void lift() {
		if(m_mode != 2) m_mode = 0;
	}

	public void drop() {
		if(m_mode != 3) m_mode = 1;
	}

}
