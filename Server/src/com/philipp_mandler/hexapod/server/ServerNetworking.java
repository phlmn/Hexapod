package com.philipp_mandler.hexapod.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

import com.philipp_mandler.hexapod.hexapod.ConsolePackage;
import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class ServerNetworking {
	
	private ServerSocket m_serverSocket;
	private ArrayList<ClientWorker> m_clients;
	private ArrayList<NetworkingEventListener> m_listeners;

	public ServerNetworking(int port) throws IOException {
		m_serverSocket = new ServerSocket(port);
		m_clients = new ArrayList<>();
		m_listeners = new ArrayList<>();
		
		new ClientWorker(this, m_serverSocket).start();
	}
	
	public void broadcast(NetPackage pack) {
		for(ClientWorker client : m_clients) {
			client.send(pack);
		}
	}
	
	public void broadcast(NetPackage pack, DeviceType devices) {
		for(ClientWorker client : m_clients) {
			if(client.getDeviceType() == devices)
				client.send(pack);
		}
	}
	
	public ServerSocket getServerSocket() {
		return m_serverSocket;
	}
	
	public void dataReceived(ClientWorker client, NetPackage pack) {
		if(pack instanceof ConsolePackage) {
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
		for(NetworkingEventListener listener : m_listeners) {
			listener.onClientConnected(client);
		}
	}
	
	public void clientDisconnected(ClientWorker client) {
		for(NetworkingEventListener listener : m_listeners) {
			listener.onClientDisconnected(client);
		}
	}
	
	public void addEventListener(NetworkingEventListener listenter) {
		m_listeners.add(listenter);
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
		String[] splitCmd = cmd.split(" ");
		for(NetworkingEventListener listener : new ArrayList<>(m_listeners)) {
			listener.onCmdReceived(null, splitCmd);
		}
	}

	public void shutdown() {
		m_listeners.clear();
		try {
			m_serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
