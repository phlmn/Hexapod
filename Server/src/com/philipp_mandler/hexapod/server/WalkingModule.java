package com.philipp_mandler.hexapod.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.WalkingScriptPackage;

public class WalkingModule extends Module implements NetworkingEventListener {
	
	private ScriptEngine m_scriptEngine;
	
	private Vec2 m_speed = new Vec2();
	private Leg m_legs[];
	
	private boolean m_scriptLoaded = false;
	
	private Object m_jsModule;
	
	private LegUpdater m_legUpdater;
	
	
	public WalkingModule(ServoController servoController) {

		ScriptEngineManager factory = new ScriptEngineManager();
		m_scriptEngine = factory.getEngineByName("JavaScript");

		m_legUpdater = new LegUpdater(servoController);


		m_legs = new Leg[6];

		m_legs[1] = new Leg(1, Data.upperLeg, Data.lowerLeg, new Vec2(90, 210), -1.0122f + Math.PI, new SingleServo(servoController, 10, 4096, 0), new SingleServo(servoController, 11, 4096, -0.35), new SingleServo(servoController, 12, 4096, -0.6), true);
		m_legs[3] = new Leg(3, Data.upperLeg, Data.lowerLeg, new Vec2(130, 0), Math.PI, new SingleServo(servoController, 13, 4096, 0), new SingleServo(servoController, 14, 4096, -0.35), new SingleServo(servoController, 15, 4096, -0.6), true);
		m_legs[5] = new Leg(5, Data.upperLeg, Data.lowerLeg, new Vec2(90, -210), 1.0122f + Math.PI, new SingleServo(servoController, 16, 4096, 0), new SingleServo(servoController, 17, 4096, -0.35), new SingleServo(servoController, 18, 4096, -0.6), true);

		m_legs[0] = new Leg(0, Data.upperLeg, Data.lowerLeg, new Vec2(-90, 210), -1.0122f, new SingleServo(servoController, 7, 4096, 0), new SingleServo(servoController, 8, 4096, 0.35), new SingleServo(servoController, 9, 4096, 0.6), false);
		m_legs[2] = new Leg(2, Data.upperLeg, Data.lowerLeg, new Vec2(-130, 0), 0, new SingleServo(servoController, 4, 4096, 0), new SingleServo(servoController, 5, 4096, 0.35), new SingleServo(servoController, 6, 4096, 0.6), false);
		m_legs[4] = new Leg(4, Data.upperLeg, Data.lowerLeg, new Vec2(-90, -210), 1.0122f, new SingleServo(servoController, 1, 4096, 0), new SingleServo(servoController, 2, 4096, 0.35), new SingleServo(servoController, 3, 4096, 0.6), false);

		for(Leg leg : m_legs) {
			m_legUpdater.addLeg(leg);
		}

	}

	@Override
	public void onStart() {

		m_legUpdater.start();

		Robot robot = new Robot(m_legs);
		m_scriptEngine.put("robot", robot);

		File gaitScript = new File(this.getClass().getResource("gait.js").getPath());

		if(gaitScript.exists() && gaitScript.isFile()) {
			try {
				m_scriptEngine.eval(new FileReader(gaitScript));
				m_jsModule = m_scriptEngine.get("module");
				m_scriptLoaded = true;
				DebugHelper.log("Default walking script loaded.");
			} catch (FileNotFoundException e1) {
				DebugHelper.log("Default walking script not found.", Log.WARNING);
				DebugHelper.log(e1.toString());
			} catch (ScriptException e1) {
				DebugHelper.log("Error in default walking script.", Log.WARNING);
				DebugHelper.log(e1.toString());
			}
		}
		else {
			DebugHelper.log("Default walking script not found.", Log.WARNING);
		}

		Main.getNetworking().addEventListener(this);
	}

	@Override
	public void onStop() {
		Main.getNetworking().removeEventListener(this);
		m_legUpdater.stop();
	}

	@Override
	public void tick(Time elapsedTime) {

		if(m_scriptLoaded) {
			Invocable inv = (Invocable)m_scriptEngine;

			try {
				inv.invokeMethod(m_jsModule, "walk", elapsedTime.getMilliseconds(), m_speed);
			} catch (Exception e) {
				e.printStackTrace();
				DebugHelper.log("Walking script: Running function walk failed.");
				DebugHelper.log(e.toString());
				m_scriptLoaded = false;
			}
		}
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {
		if(pack instanceof WalkingScriptPackage) {
			try {
				m_scriptEngine.eval(((WalkingScriptPackage) pack).getScript());
				m_jsModule = m_scriptEngine.get("module");
				m_scriptLoaded = true;
				DebugHelper.log("New walking script loaded.");
			} catch (ScriptException e1) {
				DebugHelper.log("Walking script not valid.", Log.WARNING);
				DebugHelper.log(e1.toString(), Log.WARNING);
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
							m_speed.setY( - Double.valueOf(cmd[2]));
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

	@Override
	public String getName() {
		return "walking";
	}
}
