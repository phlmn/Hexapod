package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.net.ModuleStatusPackage;
import com.philipp_mandler.hexapod.hexapod.net.NetPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

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

	private static NetworkManager m_networking;
	private static ModuleManager m_moduleManager = new ModuleManager();
	private static ActuatorManager m_actuatorManager;
	private static SensorManager m_sensorManager;
	private static TimeManager m_timeManager;

	private String m_serialPort;
	private boolean m_running = true;

	
	public static void main(String[] args) {
		String serialPort = "/dev/ttyUSB0";
		if(args.length > 0)
			serialPort = args[0];
		
		Main main = new Main(serialPort);
		main.run();
	}
	
	public Main(String serialPort) {
		m_serialPort = serialPort;
	}
	
	public void run() {

		// initialize NetworkManager
		try {
			m_networking = new NetworkManager(8888);
			m_networking.addEventListener(this);
		} catch (IOException e) {
			e.printStackTrace();
			DebugHelper.log("Networking couldn't be initialized, aborting now.", Log.WARNING);
			System.exit(0);
		}

		m_timeManager = new TimeManager(1000);

		// initialize ActuatorManager
		m_actuatorManager = new ActuatorManager(m_serialPort, 57600); //57600

		m_sensorManager = new SensorManager();

		// register Modules
		m_moduleManager.registerModule(new MobilityModule());
		m_moduleManager.registerModule(new TestingModule());
		m_moduleManager.registerModule(new VisionModule());
		m_moduleManager.registerModule(new BatteryModule());
		m_moduleManager.registerModule(new AutoModule());

		// start modules
		m_moduleManager.startModule("battery");

		// handle console input
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

		onExit();
		System.exit(0);
	}

	private void onExit() {
		// clean up
		DebugHelper.log("Shutting down...");
		Main.getModuleManager().stop();
		Main.getNetworking().shutdown();
		Main.getTimeManager().stop();
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {

	}
	
	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {

		// handle commands
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
				case "modulestatus":
					ModuleStatusPackage pack = new ModuleStatusPackage();
					for(Module module: m_moduleManager.getModules()) {
						pack.addModule(module.getName(), module.isRunning());
					}
					client.send(pack);
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
	
	public static NetworkManager getNetworking() {
		return m_networking;
	}

	public static ModuleManager getModuleManager() {
		return m_moduleManager;
	}

	public static ActuatorManager getActuatorManager() {
		return m_actuatorManager;
	}

	public static SensorManager getSensorManager() {
		return m_sensorManager;
	}

	public static TimeManager getTimeManager() {
		return m_timeManager;
	}
}
