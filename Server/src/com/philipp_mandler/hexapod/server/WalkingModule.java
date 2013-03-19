package com.philipp_mandler.hexapod.server;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import SimpleDynamixel.Servo;

import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.WalkingScriptPackage;

public class WalkingModule implements Module {
	
	private boolean m_running = true;
	
	private ScriptEngine m_scriptEngine;
	
	private Vec2 m_speed = new Vec2();
	private Leg m_legs[];
	private Servo m_servoController;
	
	private Controller m_gamepad;
	private Component m_gamepad_x;
	private Component m_gamepad_y;
	
	private int m_sensitivity = 30;
	
	private double m_range = 200;
	private double m_bodyHeight = 100;
	
	private boolean m_scriptLoaded = false;
	
	private Object m_jsModule;
	
	private LegUpdater m_legUpdater;
	
	
	public WalkingModule(Servo servoController) {
		m_servoController = servoController;
		
		ScriptEngineManager factory = new ScriptEngineManager();
		m_scriptEngine = factory.getEngineByName("JavaScript");
		
		m_legUpdater = new LegUpdater();
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
		
		m_legs[0] = new Leg(0, Data.upperLeg, Data.lowerLeg, new Vec2(-100, 210), 0, new SingleServo(m_servoController, 7, 4096, 0), new SingleServo(m_servoController, 8, 4096, 0.35), new SingleServo(m_servoController, 9, 4096, 0.6));
		m_legs[1] = new Leg(1, Data.upperLeg, Data.lowerLeg, new Vec2(100, 210), 0, new SingleServo(m_servoController, 10, 4096, 0), new SingleServo(m_servoController, 11, 4096, 0), new SingleServo(m_servoController, 13, 4096, 0));
		m_legs[2] = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-190, 0), 0, new SingleServo(m_servoController, 4, 4096, 0), new SingleServo(m_servoController, 5, 4096, 0), new SingleServo(m_servoController, 6, 4096, 0));
		m_legs[3] = new Leg(3, Data.upperLeg, Data.lowerLeg, new Vec2(190, 0), 0, new SingleServo(m_servoController, 13, 4096, 0), new SingleServo(m_servoController, 14, 4096, 0), new SingleServo(m_servoController, 15, 4096, 0));
		m_legs[4] = new Leg(4, Data.upperLeg, Data.lowerLeg, new Vec2(-100, -210), 0, new SingleServo(m_servoController, 1, 4096, 0), new SingleServo(m_servoController, 2, 4096, 0), new SingleServo(m_servoController, 3, 4096, 0));
		m_legs[5] = new Leg(5, Data.upperLeg, Data.lowerLeg, new Vec2(100, -210), 0, new SingleServo(m_servoController, 16, 4096, 0), new SingleServo(m_servoController, 17, 4096, 0), new SingleServo(m_servoController, 18, 4096, 0));
		
		for(Leg leg : m_legs) {		
			m_legUpdater.addLeg(leg);
		}
		
		m_legUpdater.start();
		
		Robot robot = new Robot(m_legs);
		
		m_scriptEngine.put("robot", robot);
		
		Invocable inv = (Invocable)m_scriptEngine;
		
		double time = System.currentTimeMillis() * 1000000 + System.nanoTime();
		
		while(m_running) {
			if(m_scriptLoaded) {
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
				try {
					inv.invokeMethod(m_jsModule, "walk", elapsedTime / 1000000, m_speed);
				} catch (Exception e) {
					e.printStackTrace();
					DebugHelper.log("Walking script: Running function walk failed.");
					DebugHelper.log(e.toString());
					m_scriptLoaded = false;
				}
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			else {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void stop() {
		m_running = false;
	}

	@Override
	public void onDataReceived(Client client, NetPackage pack) {
		if(pack instanceof WalkingScriptPackage) {
			try {
				m_scriptEngine.eval(((WalkingScriptPackage) pack).getScript());
				m_jsModule = m_scriptEngine.get("module");
				DebugHelper.log("New walking script loaded.");
				m_scriptLoaded = true;
			} catch (ScriptException e1) {
				DebugHelper.log("Walking script not valid.", Log.WARNING);
				DebugHelper.log(e1.toString(), Log.WARNING);
			}
		}
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
