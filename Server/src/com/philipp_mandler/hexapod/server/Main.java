package com.philipp_mandler.hexapod.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
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
 *                  \__|__/                                    o   |   o             
 *                  |  |  |                                   / \  |  / \             
 *  --  [2] ----o---|--|--|---o---- [3]   ---> x      -------/---\___/---\----> x       
 *                  |__|__|                                 /      |      \           
 *                  /  |   \                                       |                  
 *                 o   |    o                                      |                  
 *                /    |     \                                                        
 *               /     |      \
 *           [4]       |        [5]
 *                     |
 *                     
 */             

public class Main implements NetworkingEventListener {
	
	private int m_sensitivity = 8;
	private ServoController m_servo;
	private Controller m_gamePad = null;
	private ArrayList<Module> m_modules;
	private static ServerNetworking m_networking;
	private static ModuleManager m_moduleManager = new ModuleManager();
	private String m_serialPort;

	
	public static void main(String[] args) {
		String serialPort = "COM5";
		if(args.length > 0)
			serialPort = args[0];
		
		Main main = new Main(serialPort);
		main.run();
	}
	
	public Main(String serialPort) {
		m_serialPort = serialPort;
	}
	
	public void run() {

		try {
			m_networking = new ServerNetworking(8888);
			m_networking.addEventListener(this);
		} catch (IOException e) {
			e.printStackTrace();
			DebugHelper.log("Networking couldn't be initialized, aborting now.", Log.WARNING);
			System.exit(0);
		}

		m_modules = new ArrayList<>();

		m_servo = new ServoController();
		try {
			m_servo.init(m_serialPort, 100000);
		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		m_moduleManager.registerModule(new WalkingModule(m_servo));
		m_moduleManager.registerModule(new TestingModule());

		Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();

		for(Controller controller : controllers) {
			if(controller.getType() == Type.GAMEPAD) {
				m_gamePad = controller;
				DebugHelper.log("Gamepad connected.");
			}
		}

		BufferedReader  reader = new BufferedReader(new InputStreamReader(System.in));
		
		while(true) {
			try {
				String input = reader.readLine();
				m_networking.internalCmd(input);				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {
		if(pack instanceof LegPositionPackage) {
			LegPositionPackage posPack = (LegPositionPackage)pack;
			DebugHelper.log("Position received\nLeg: " + posPack.getLegIndex() + "\nPosition: ( " + posPack.getGoalPosition().getX() + " | " + posPack.getGoalPosition().getY() + " | " + posPack.getGoalPosition().getZ() + " )");
			Main.getNetworking().broadcast(pack, DeviceType.InfoScreen);
		}
	}
	
	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {
		if(cmd.length > 0) {
			String mainCmd = cmd[0].toLowerCase();
			switch(mainCmd) {
				case "module": 
					if(cmd.length > 2) {
						if(cmd[2].toLowerCase().equals("start")) {
							if(!Main.getModuleManager().startModule(cmd[1]))
								DebugHelper.log("No such module found.");
						}
						else if(cmd[2].toLowerCase().equals("stop")) {
							if(!Main.getModuleManager().stopModule(cmd[1]))
								DebugHelper.log("No such module found.");
						}
						else if(cmd[2].toLowerCase().equals("status")) {
							Module module = Main.getModuleManager().getModule(cmd[1]);
							if(module != null) {
								if(module.isRunning()) {
									DebugHelper.log("The module \"" + cmd[1] + "\" is running.");
								}
								else {
									DebugHelper.log("The module \"" + cmd[1] + "\" is not running.");
								}
							}
							else {
								DebugHelper.log("No such module found.");
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
	
	private void showSyntax(String cmd) {
		String answer;
		switch(cmd) {
		case "module": answer = "Syntax: module <module> <start|stop|status>"; break;
		default: answer = "No such command found.";
		}
		DebugHelper.log(answer);
	}

	@Override
	public void onClientDisconnected(ClientWorker client) {
		DebugHelper.log("Client Disconnected (" + client.getDeviceType() + ") | IP: " + client.getSocket().getInetAddress() + ":" + client.getSocket().getPort());
	}

	@Override
	public void onClientConnected(ClientWorker client) {
		DebugHelper.log("Client Connected (" + client.getDeviceType() + ") | IP : " + client.getSocket().getInetAddress() + ":" + client.getSocket().getPort());
	}
	
	public static ServerNetworking getNetworking() {
		return m_networking;
	}

	public static ModuleManager getModuleManager() {
		return m_moduleManager;
	}
}
