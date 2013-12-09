package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.JoystickPackage;
import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.WalkingScriptPackage;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class OldWalkingModule extends Module implements NetworkingEventListener {

	private ScriptEngine m_scriptEngine;

	private Vec2 m_speed = new Vec2();
	private Leg m_legs[];

	private boolean m_scriptLoaded = false;

	private Object m_jsModule;

	private LegUpdater m_legUpdater;


	public OldWalkingModule() {

		setName("walking");

		ScriptEngineManager factory = new ScriptEngineManager();
		m_scriptEngine = factory.getEngineByName("JavaScript");

		m_legUpdater = new LegUpdater();


		m_legs = new Leg[6];

		ActuatorManager acs = Main.getActuatorManager();

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
		else if(pack instanceof JoystickPackage) {
			JoystickPackage joyPack = (JoystickPackage)pack;
			m_speed.set(joyPack.getData().getX() * 10, joyPack.getData().getY() * 10);
			DebugHelper.log("Walking speed set to: " + m_speed.getX() + "  " + m_speed.getY());
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

}
