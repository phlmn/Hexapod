package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.ButtonGroupPackage;
import com.philipp_mandler.hexapod.hexapod.ConsolePackage;
import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.NetPackage;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;


public class NetworkManager {
	
	private ServerSocket m_serverSocket;
	private ArrayList<ClientWorker> m_clients;
	private ArrayList<NetworkingEventListener> m_listeners;
	private ArrayList<ButtonGroup> m_buttonGroups;

	public NetworkManager(int port) throws IOException {
		m_serverSocket = new ServerSocket(port);
		m_clients = new ArrayList<>();
		m_listeners = new ArrayList<>();
		m_buttonGroups = new ArrayList<>();
		
		new ClientWorker(this, m_serverSocket).start();
	}

	public void addButtonGroup(ButtonGroup buttonGroup) {
		// add ButtonGroup
		m_buttonGroups.add(buttonGroup);
		broadcast(buttonGroup.toPackage());
		for(Button button : buttonGroup.getButtons()) {
			broadcast(button.toPackage());
		}
	}

	public void removeButtonGroup(ButtonGroup buttonGroup) {
		// remove ButtonGroup
		ButtonGroupPackage pack = buttonGroup.toPackage();
		pack.setDelete(true);
		broadcast(pack);
		m_buttonGroups.remove(buttonGroup);
	}
	
	public void broadcast(NetPackage pack) {
		// broadcast NetPackage
		for(ClientWorker client : m_clients) {
			client.send(pack);
		}
	}
	
	public void broadcast(NetPackage pack, DeviceType devices) {
		// broadcast NetPackage to specified device type
		for(ClientWorker client : m_clients) {
			if(client.getDeviceType() == devices)
				client.send(pack);
		}
	}
	
	public ServerSocket getServerSocket() {
		return m_serverSocket;
	}
	
	public void dataReceived(ClientWorker client, NetPackage pack) {
		// receive data from ClientWorker
		if(pack instanceof ConsolePackage) {
			// handle commands
			ConsolePackage consolePackage = (ConsolePackage)pack;
			String[] cmd = consolePackage.getText().split(" ");
			for(NetworkingEventListener listener : new ArrayList<>(m_listeners)) {
				listener.onCmdReceived(client, cmd);
			}			
		}
		else {
			for(NetworkingEventListener listener : new ArrayList<>(m_listeners)) {
				listener.onDataReceived(client, pack);
			}	
		}
	}
	
	public void clientConnected(ClientWorker client) {
		// notify clients about client connection
		for(NetworkingEventListener listener : m_listeners) {
			listener.onClientConnected(client);
		}

		// send Buttons to new connected client
		for(ButtonGroup buttonGroup : m_buttonGroups) {
			client.send(buttonGroup.toPackage());
			for(Button button : buttonGroup.getButtons()) {
				client.send(button.toPackage());
			}
		}
	}
	
	public void clientDisconnected(ClientWorker client) {
		// notify clients about client disconnection
		for(NetworkingEventListener listener : m_listeners) {
			listener.onClientDisconnected(client);
		}
	}
	
	public void addEventListener(NetworkingEventListener listener) {
		m_listeners.add(listener);
	}
	
	public void removeEventListener(NetworkingEventListener listener) {
		m_listeners.remove(listener);
	}
	
	public void registerClient(ClientWorker client) {
		m_clients.add(client);
	}
	
	public void removeClient(ClientWorker client) {
		m_clients.remove(client);
	}
	
	public void internalCmd(String cmd) {
		// receive an internal command
		String[] splitCmd = cmd.split(" ");
		for(NetworkingEventListener listener : new ArrayList<>(m_listeners)) {
			listener.onCmdReceived(null, splitCmd);
		}
	}

	public void shutdown() {
		// disconnect all clients
		for(ClientWorker clientWorker : m_clients) {
			clientWorker.disconnect();
		}

		// clear client list
		m_clients.clear();

		// clear listener list
		m_listeners.clear();

		// close socket
		try {
			m_serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
