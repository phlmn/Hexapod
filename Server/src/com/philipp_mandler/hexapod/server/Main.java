package com.philipp_mandler.hexapod.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import motej.Mote;
import net.java.games.input.Component;
import net.java.games.input.Component.Identifier;
import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;
import net.java.games.input.Event;
import net.java.games.input.EventQueue;
import SimpleDynamixel.Servo;

import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.LegPositionPackage;
import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.StatusPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec3;

/* 
 *   
 *    [Top View]       y
 *                     ^
 *                     |                            [Front View]
 *           [0]       |       [1]                    
 *               \     |     /                                     z               
 *                \    |    /                                      ^                  
 *                 o   |   o                                       |                   
 *                  \__|__/                                    o   |    o             
 *                  |  |  |                                   / \  |   / \             
 *  --  [2] ----o---|--|--|---o---- [3]   ---> x       ------/----____/---\----> x       
 *                  |__|__|                                 /      |       \           
 *                  /  |   \                                       |                  
 *                 o   |    o                                      |                  
 *                /    |     \                                                        
 *               /     |      \
 *           [4]       |        [5]
 *                     |
 *                     
 */             

public class Main implements NetworkingEventListener {

	/**
	 * @param args
	 */
	static boolean m_testmode = false;
	
	final double m_upperLeg = 140;
	final double m_lowerLeg = 210;
	
	int m_sensitivity = 8;

	Servo servo;
	Leg legs[];
	
	Controller m_gamePad = null;
	
	Mote m_wiimote;
	
	static Networking m_networking;
	
	public static void main(String[] args) {
		boolean testmode = false;
		
		String argLog = "Started with args:\n";
		for(String arg : args) {
			argLog += arg + "\n";
			if(arg.equals("testmode")) testmode = true;
		}
		DebugHelper.log(argLog);
		Main main = new Main(testmode);
		main.run();
	}
	
	public Main(boolean testmode) {		
	
		m_testmode = testmode;
		
		servo = new Servo();
		servo.init("COM5", 57142);
		
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		
		for(Controller controller : controllers) {
			if(controller.getType() == Type.GAMEPAD) {
				m_gamePad = controller;
				DebugHelper.log("Gamepad connected.");
			}
		}
		
		DebugHelper.log("Searching servos...");
		int[] servos = servo.pingRange(0, 32);
		
		DebugHelper.log(servos.length + " servos found.");
		
		if(!testmode && servos.length == 0) {
			DebugHelper.log("No Servos found, aborting now.", Log.CRITICAL);
			System.exit(0);
		}
		
	}
	
	public void run() {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			m_networking = new Networking(8888);
			m_networking.addEventListener(this);
		} catch (IOException e) {
			e.printStackTrace();
			DebugHelper.log("Networking couldn't be initialized aborting now.", Log.CRITICAL);
			System.exit(0);
		}
		
		legs = new Leg[6];
		
		//legs[2] = new Leg(2, m_upperLeg, m_lowerLeg, new Vec2(-100, 0), new SingleServo(servo, 1, 4096, 0), new SingleServo(servo, 2, 4096, 0.35), new SingleServo(servo, 3, 4096, 0.6));
		
		legs[0] = new Leg(0, m_upperLeg, m_lowerLeg, new Vec2(-100, 210), new SingleServo(servo, 1, 4096, 0), new SingleServo(servo, 2, 4096, 0.35), new SingleServo(servo, 3, 4096, 0.6));
		legs[1] = new Leg(1, m_upperLeg, m_lowerLeg, new Vec2(100, 210), new SingleServo(servo, 4, 4096, 0), new SingleServo(servo, 5, 4096, 0), new SingleServo(servo, 6, 4096, 0));
		legs[2] = new Leg(2, m_upperLeg, m_lowerLeg, new Vec2(-190, 0), new SingleServo(servo, 7, 4096, 0), new SingleServo(servo, 8, 4096, 0), new SingleServo(servo, 9, 4096, 0));
		legs[3] = new Leg(3, m_upperLeg, m_lowerLeg, new Vec2(190, 0), new SingleServo(servo, 10, 4096, 0), new SingleServo(servo, 11, 4096, 0), new SingleServo(servo, 12, 4096, 0));
		legs[4] = new Leg(4, m_upperLeg, m_lowerLeg, new Vec2(-100, -210), new SingleServo(servo, 13, 4096, 0), new SingleServo(servo, 14, 4096, 0), new SingleServo(servo, 15, 4096, 0));
		legs[5] = new Leg(5, m_upperLeg, m_lowerLeg, new Vec2(100, -210), new SingleServo(servo, 16, 4096, 0), new SingleServo(servo, 17, 4096, 0), new SingleServo(servo, 18, 4096, 0));
		
		LegGroup triangle1 = new LegGroup(new Leg[]{legs[0], legs[3], legs[4]}, new Vec2[]{new Vec2(-250, 300), new Vec2(350, 0), new Vec2(-250, -300)});
		LegGroup triangle2 = new LegGroup(new Leg[]{legs[1], legs[2], legs[5]}, new Vec2[]{new Vec2(250, 300), new Vec2(-350, 0), new Vec2(250, -300)});
		
		
		Component controller_x = null;		
		Component controller_y = null;
		
		if(m_gamePad != null) {
			controller_x = m_gamePad.getComponent(Identifier.Axis.X);
			controller_y = m_gamePad.getComponent(Identifier.Axis.Y);
		}
		
		double range = 150;
		
		Vec2 speed = new Vec2(0, 4);
		
		double raw_walk = range;
		double walk;
		
		for(int i = 0; i < 100000; i++) {
						
			if(m_gamePad != null) {
				if(m_gamePad.poll()) {				
					EventQueue events = m_gamePad.getEventQueue();
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
					
					speed.setX(controller_x.getPollData() * m_sensitivity);
					speed.setY(controller_y.getPollData() * m_sensitivity);
				}
				else {
					m_gamePad = null;
				}
			}
			
			
			if(raw_walk > range)
				raw_walk -= range * 2;
			
			walk = raw_walk;
			
			raw_walk += speed.getY();	
			
			
			double height1 = 0, height2 = 0;
			
			if(walk < 0)
				height1 = -40;
			else
				height2 = -40;
			
			walk = Math.abs(walk);
			
			walk -= range / 2;		
			
			triangle1.setTranslation(new Vec3(0 , walk, -150 + height1));
			triangle2.setTranslation(new Vec3(0 , -walk, -150 + height2));
			triangle1.moveLegs();
			triangle2.moveLegs();
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		while(true) {
			try {
				String input = reader.readLine();
				switch(input) {
				case "send": m_networking.broadcast(new StatusPackage(42)); break;
				case "exit": System.exit(0); break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void dataReceived(NetPackage pack) {
		if(pack instanceof LegPositionPackage) {
			LegPositionPackage posPack = (LegPositionPackage)pack;
			DebugHelper.log("Position received\nLeg: " + posPack.getLegIndex() + "\nPosition: ( " + posPack.getGoalPosition().getX() + " | " + posPack.getGoalPosition().getY() + " | " + posPack.getGoalPosition().getZ() + " )");
			if(posPack.getLegIndex() < legs.length && posPack.getLegIndex() > 0) {
				legs[posPack.getLegIndex()].setGoalPosition(posPack.getGoalPosition());
			}
			Main.getNetworking().broadcast(pack, DeviceType.InfoScreen);
		}
	}
	
	public static Networking getNetworking() {
		return m_networking;
	}
	
	public static boolean isTestmode() {
		return m_testmode;
	}


}
