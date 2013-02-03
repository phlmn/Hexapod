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
	
	private double m_range = 220;
	private double m_bodyHeight = 100;
	
	
	public WalkingModule(Servo servoController) {
		m_servoController = servoController;
	}
	
	public void setGamepad(Controller gamepad) {
		m_gamepad = gamepad;
		m_gamepad_x = m_gamepad.getComponent(Identifier.Axis.X);
		m_gamepad_y = m_gamepad.getComponent(Identifier.Axis.Y);
	}
	
	public Controller getGamepad() {
		return m_gamepad;
	}

	@Override
	public void run() {
		
		double raw_walk = m_range;
		double walk;
		
		m_legs = new Leg[6];
		
		m_legs[2] = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-100, 0), new SingleServo(m_servoController, 1, 4096, 0), new SingleServo(m_servoController, 2, 4096, 0.35), new SingleServo(m_servoController, 3, 4096, 0.6));
		
//		m_legs[0] = new Leg(0, Data.upperLeg, Data.lowerLeg, new Vec2(-100, 210), new SingleServo(m_servoController, 1, 4096, 0), new SingleServo(m_servoController, 2, 4096, 0.35), new SingleServo(m_servoController, 3, 4096, 0.6));
//		m_legs[1] = new Leg(1, Data.upperLeg, Data.lowerLeg, new Vec2(100, 210), new SingleServo(m_servoController, 4, 4096, 0), new SingleServo(m_servoController, 5, 4096, 0), new SingleServo(m_servoController, 6, 4096, 0));
//		m_legs[2] = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-190, 0), new SingleServo(m_servoController, 7, 4096, 0), new SingleServo(m_servoController, 8, 4096, 0), new SingleServo(m_servoController, 9, 4096, 0));
//		m_legs[3] = new Leg(3, Data.upperLeg, Data.lowerLeg, new Vec2(190, 0), new SingleServo(m_servoController, 10, 4096, 0), new SingleServo(m_servoController, 11, 4096, 0), new SingleServo(m_servoController, 12, 4096, 0));
//		m_legs[4] = new Leg(4, Data.upperLeg, Data.lowerLeg, new Vec2(-100, -210), new SingleServo(m_servoController, 13, 4096, 0), new SingleServo(m_servoController, 14, 4096, 0), new SingleServo(m_servoController, 15, 4096, 0));
//		m_legs[5] = new Leg(5, Data.upperLeg, Data.lowerLeg, new Vec2(100, -210), new SingleServo(m_servoController, 16, 4096, 0), new SingleServo(m_servoController, 17, 4096, 0), new SingleServo(m_servoController, 18, 4096, 0));
		
		LegGroup triangle1 = new LegGroup(new Leg[]{m_legs[0], m_legs[3], m_legs[4]}, new Vec2[]{new Vec2(-250, 300), new Vec2(350, 0), new Vec2(-250, -300)});
		LegGroup triangle2 = new LegGroup(new Leg[]{m_legs[1], m_legs[2], m_legs[5]}, new Vec2[]{new Vec2(250, 300), new Vec2(-350, 0), new Vec2(250, -300)});
		
		
		double time = System.currentTimeMillis() * 1000000 + System.nanoTime();
				
		
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
			
			
			if(raw_walk > m_range)
				raw_walk -= m_range * 2;
			
			walk = raw_walk;
			
			raw_walk += m_speed.getY() * (elapsedTime / 100000000);	
			
			double height1 = 0, height2 = 0;
			
			if(walk < 0)
				height1 = -40;
			else
				height2 = -40;
			
			walk = Math.abs(walk);
			
			walk -= m_range / 2;		
			
			triangle1.setTranslation(new Vec3(0 , walk, -m_bodyHeight + height1));
			triangle2.setTranslation(new Vec3(0 , -walk, -m_bodyHeight + height2));
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
