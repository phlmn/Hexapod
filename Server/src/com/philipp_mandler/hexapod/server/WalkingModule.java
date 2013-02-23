package com.philipp_mandler.hexapod.server;

import net.java.games.input.Component;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import net.java.games.input.Component.Identifier;

import SimpleDynamixel.Servo;

import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec3;

public class WalkingModule implements Module {
	
	private boolean m_running = true;
	
	private Vec2 m_speed = new Vec2();
	private Leg m_legs[];
	private Servo m_servoController;
	
	private Controller m_gamepad;
	private Component m_gamepad_x;
	private Component m_gamepad_y;
	
	private int m_sensitivity = 30;
	
	private double m_range = 200;
	private double m_bodyHeight = 100;
	
	
	public WalkingModule(Servo servoController) {
		m_servoController = servoController;
	}
	
	public void setGamepad(Controller gamepad) {
		m_gamepad = gamepad;
		if(m_gamepad != null) {
			m_gamepad_x = m_gamepad.getComponent(Identifier.Axis.X);
			m_gamepad_y = m_gamepad.getComponent(Identifier.Axis.Y);
		}
		else {
			m_gamepad_x = null;
			m_gamepad_y = null;
		}
	}
	
	public Controller getGamepad() {
		return m_gamepad;
	}

	@Override
	public void run() {
		
		m_legs = new Leg[6];
		
		m_legs[0] = new Leg(0, Data.upperLeg, Data.lowerLeg, new Vec2(-100, 210), new SingleServo(m_servoController, 1, 4096, 0), new SingleServo(m_servoController, 2, 4096, 0.35), new SingleServo(m_servoController, 3, 4096, 0.6));
		m_legs[1] = new Leg(1, Data.upperLeg, Data.lowerLeg, new Vec2(100, 210), new SingleServo(m_servoController, 4, 4096, 0), new SingleServo(m_servoController, 5, 4096, 0), new SingleServo(m_servoController, 6, 4096, 0));
		m_legs[2] = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-190, 0), new SingleServo(m_servoController, 7, 4096, 0), new SingleServo(m_servoController, 8, 4096, 0), new SingleServo(m_servoController, 9, 4096, 0));
		m_legs[3] = new Leg(3, Data.upperLeg, Data.lowerLeg, new Vec2(190, 0), new SingleServo(m_servoController, 10, 4096, 0), new SingleServo(m_servoController, 11, 4096, 0), new SingleServo(m_servoController, 12, 4096, 0));
		m_legs[4] = new Leg(4, Data.upperLeg, Data.lowerLeg, new Vec2(-100, -210), new SingleServo(m_servoController, 13, 4096, 0), new SingleServo(m_servoController, 14, 4096, 0), new SingleServo(m_servoController, 15, 4096, 0));
		m_legs[5] = new Leg(5, Data.upperLeg, Data.lowerLeg, new Vec2(100, -210), new SingleServo(m_servoController, 16, 4096, 0), new SingleServo(m_servoController, 17, 4096, 0), new SingleServo(m_servoController, 18, 4096, 0));
		
		LegGroup triangle1 = new LegGroup(new Leg[]{m_legs[0], m_legs[3], m_legs[4]}, new Vec2[]{new Vec2(-250, 300), new Vec2(350, 0), new Vec2(-250, -300)});
		LegGroup triangle2 = new LegGroup(new Leg[]{m_legs[1], m_legs[2], m_legs[5]}, new Vec2[]{new Vec2(250, 300), new Vec2(-350, 0), new Vec2(250, -300)});
		
		
		double time = System.currentTimeMillis() * 1000000 + System.nanoTime();
				
		boolean direction1 = true;
		boolean direction2 = false;
		boolean stepping = false;
		
		Vec2 triangle1Pos = new Vec2(0, 40);
		Vec2 triangle2Pos = new Vec2(0, 0);
		
		double z1 = 0, z2 = 0;
		
		while(m_running) {
					
			double newtime = System.currentTimeMillis() * 1000000 + System.nanoTime();		
			double elapsedTime = newtime - time;
			
			time = newtime;
			
			if(m_gamepad != null) {
				if(m_gamepad.poll()) {				
					EventQueue events = m_gamepad.getEventQueue();
					Event event = new Event();
					while(events.getNextEvent(event)) {
						if(!event.getComponent().isAnalog()) {
							if(event.getComponent().getIdentifier() == Identifier.Button._1) {
								if(event.getValue() == 1.0) {
									DebugHelper.log("Gamepad Sesitivity: " + ++m_sensitivity);
								}
							}
							else if(event.getComponent().getIdentifier() == Identifier.Button._0) {
								if(event.getValue() == 1.0) {
									if(m_sensitivity > 1)
										DebugHelper.log("Gamepad Sesitivity: " + --m_sensitivity);
								}
							}
						}
					}
					
					m_speed.setX(m_gamepad_x.getPollData() * m_sensitivity);
					m_speed.setY(m_gamepad_y.getPollData() * m_sensitivity);
				}
				else {
					m_gamepad = null;
				}
			}
			
			if(triangle1Pos.getLength() >= m_range/2) {
				direction1 = !direction1;
				DebugHelper.log("Direction 1: " + direction1);
				
				triangle1Pos.normalize();
				triangle1Pos.multiply(m_range/2);
			}
			
			if(triangle2Pos.getLength() >= m_range/2) {
				direction2 = !direction2;
				DebugHelper.log("Direction 2: " + direction2);
				
				triangle2Pos.normalize();
				triangle2Pos.multiply(m_range/2);
			}
			
			//direction = true -> triangle1 unten
			//direction = false -> triangle2 unten
			
			
			int directionSign1 = -1;
			if(direction1) {
				directionSign1 = 1;
			}
			
			int directionSign2 = -1;
			if(direction2) {
				directionSign2 = 1;
			}
			
			if(triangle1Pos.getLength() > m_range/2 - 20) {
				if(direction1)
					z1 = (m_range/2 - triangle1Pos.getLength()) * 2;
			}
			else {
				z1 = 0;
			}
			
			if(triangle2Pos.getLength() > m_range/2 - 20) {
				if(direction2)
					z2 = (m_range/2 - triangle2Pos.getLength()) * 2;
			}
			else {
				z2 = 0;
			}
			
			triangle1Pos.setY(triangle1Pos.getY() + directionSign1 *  m_speed.getY() * (elapsedTime / 100000000));
			triangle2Pos.setY(triangle2Pos.getY() + directionSign2 *  m_speed.getY() * (elapsedTime / 100000000));
			
			triangle1.getTranslation().set(new Vec3(triangle1Pos, z1));
			triangle2.getTranslation().set(new Vec3(triangle2Pos, z2));
			
			triangle1.moveLegs();
			triangle2.moveLegs();
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		m_running = false;
	}

	@Override
	public void onDataReceived(Client client, NetPackage pack) {
		
	}
	
	@Override
	public void onCmdReceived(Client client, String[] cmd) {
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
				else if(cmd[1].toLowerCase().equals("range")) {
					if(cmd.length > 2) {
						try {
							m_range = Double.valueOf(cmd[2]);
							DebugHelper.log("Walking range set to " + m_range + ".");
						}
						catch(NumberFormatException e) {
							DebugHelper.log("The last parameter is no valid number.");
						}
					}
				}
				else if(cmd[1].toLowerCase().equals("height")) {
					if(cmd.length > 2) {
						try {
							m_bodyHeight = Double.valueOf(cmd[2]);
							DebugHelper.log("Body height set to " + m_bodyHeight + ".");
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
	public void onClientDisconnected(Client client) {
		
	}

	@Override
	public void onClientConnected(Client client) {
		
	}

	@Override
	public String getName() {
		return "walking";
	}
}
