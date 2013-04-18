package com.philipp_mandler.hexapod.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import net.java.games.input.Controller;
import net.java.games.input.Controller.Type;
import net.java.games.input.ControllerEnvironment;
import SimpleDynamixel.Servo;

import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.LegPositionPackage;
import com.philipp_mandler.hexapod.hexapod.NetPackage;

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
	
	int m_sensitivity = 8;

	Servo servo;
	Leg legs[];
	
	Controller m_gamePad = null;
	
	ArrayList<Module> m_modules;
	
	static ServerNetworking m_networking;
	
	public static void main(String[] args) {
		Main main = new Main();
		main.run();
	}
	
	public Main() {		
		m_modules = new ArrayList<Module>();
		
		servo = new Servo();
		servo.init("COM5", 100000);
		
		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
		
		for(Controller controller : controllers) {
			if(controller.getType() == Type.GAMEPAD) {
				m_gamePad = controller;
				DebugHelper.log("Gamepad connected.");
			}
		}
	}
	
	public void run() {
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			m_networking = new ServerNetworking(8888);
			m_networking.addEventListener(this);
		} catch (IOException e) {
			e.printStackTrace();
			DebugHelper.log("Networking couldn't be initialized aborting now.", Log.WARNING);
			System.exit(0);
		}
		
		while(true) {
			try {
				String input = reader.readLine();
				String[] cmd = input.split(" ");
				onCmdReceived(null, cmd);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDataReceived(Client client, NetPackage pack) {
		if(pack instanceof LegPositionPackage) {
			LegPositionPackage posPack = (LegPositionPackage)pack;
			DebugHelper.log("Position received\nLeg: " + posPack.getLegIndex() + "\nPosition: ( " + posPack.getGoalPosition().getX() + " | " + posPack.getGoalPosition().getY() + " | " + posPack.getGoalPosition().getZ() + " )");
			if(posPack.getLegIndex() < legs.length && posPack.getLegIndex() > 0) {
				legs[posPack.getLegIndex()].setGoalPosition(posPack.getGoalPosition());
			}
			Main.getNetworking().broadcast(pack, DeviceType.InfoScreen);
		}
	}
	
	@Override
	public void onCmdReceived(Client client, String[] cmd) {
		if(cmd.length > 0) {
			String mainCmd = cmd[0].toLowerCase();
			switch(mainCmd) {
				case "module": 
					if(cmd.length > 2) {
						if(cmd[2].toLowerCase().equals("start")) {
							switch(cmd[1]) {
							case "walking": WalkingModule mod = new WalkingModule(servo); startModule(mod); if(mod != null) mod.setGamepad(m_gamePad); break;
							default: DebugHelper.log("No such module found.");
							}
						}
						else if(cmd[2].toLowerCase().equals("stop")) {
							stopModule(cmd[1]);
						}
						else if(cmd[2].toLowerCase().equals("status")) {
							if(isModuleRunning(cmd[1].toLowerCase())) {
								DebugHelper.log("The module \"" + cmd[1] + "\" is running.");
							}
							else {
								DebugHelper.log("The module \"" + cmd[1] + "\" is not running.");
							}
						}
						else {
							showSyntax("module");
						}
					}
					else {
						showSyntax("module");
					}
					break;
			}
		}
	}
	
	public boolean isModuleRunning(String moduleName) {
		for(Module mod: m_modules) {
			if(mod.getName().equals(moduleName));
				return true;
		}
		return false;
	}
	
	private void showSyntax(String cmd) {
		String answer;
		switch(cmd) {
		case "module": answer = "Syntax: module <module> <start|stop|status>"; break;
		default: answer = "No such command found.";
		}
		DebugHelper.log(answer);
	}
	
	public void startModule(Module module) {
		for(Module mod: m_modules) {
			if(mod.getName().equals(module.getName())) {
				DebugHelper.log("This module is already running.");
				return;
			}
		}
		new ModuleThread(this, module).start();
		m_networking.addEventListener(module);
		m_modules.add(module);
	}
	
	public void stopModule(String module) {
		for(Module mod: m_modules) {
			if(mod.getName().equals(module)) {
				mod.stop();
				return;
			}
		}
	}
	
	public void unregsiterModule(Module module) {
		m_networking.removeEventListener(module);
		m_modules.remove(module);
	}
	
	@Override
	public void onClientDisconnected(Client client) {
		DebugHelper.log("Client Disconnected (" + client.getDeviceType() + ") | IP: " + client.getSocket().getInetAddress() + ":" + client.getSocket().getPort());
	}

	@Override
	public void onClientConnected(Client client) {
		DebugHelper.log("Client Connected (" + client.getDeviceType() + ") | IP : " + client.getSocket().getInetAddress() + ":" + client.getSocket().getPort());
	}
	
	public static ServerNetworking getNetworking() {
		return m_networking;
	}
}
