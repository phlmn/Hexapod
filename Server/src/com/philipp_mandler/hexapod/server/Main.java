package com.philipp_mandler.hexapod.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

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

	private static ServerNetworking m_networking;
	private static ModuleManager m_moduleManager = new ModuleManager();
	private String m_serialPort;
	private boolean m_running = true;

	
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

		ServoController m_servo = new ServoController();
		try {
			m_servo.init(m_serialPort, 100000);
		} catch (PortInUseException | UnsupportedCommOperationException | NoSuchPortException | IOException e) {
			e.printStackTrace();
		}

		WalkingModule walkingModule = new WalkingModule(m_servo);
		m_moduleManager.registerModule(walkingModule);
		m_moduleManager.registerModule(new TestingModule());

		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		
		while(m_running) {
			try {
				String input = reader.readLine();
				if(input != null)
					m_networking.internalCmd(input);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.exit(0);
	}

	private void onExit() {
		DebugHelper.log("Shutting down...");
		Main.getModuleManager().stop();
		Main.getNetworking().shutdown();
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
		m_moduleManager.onCmdReceived(client, cmd);
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
				case "modules":
					String out = "";
					out += Main.getModuleManager().getModules().size() + " modules available";
					for(Module module : Main.getModuleManager().getModules()) {

						String statusText;
						if(module.isRunning()) {
							statusText = "running";
						}
						else {
							statusText = "stopped";
						}

						out += "\n" + module.getName() + "\t\t " + statusText;
					}
					DebugHelper.log(out);
					break;
				case "exit":
					m_running = false;
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
