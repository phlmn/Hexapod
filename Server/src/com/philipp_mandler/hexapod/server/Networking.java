package com.philipp_mandler.hexapod.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class Networking {
	
	ServerSocket m_serverSocket;
	Socket m_clientSocket;
	ArrayList<Client> m_clients;
	ArrayList<NetworkingEventListener> m_listeners;

	public Networking(int port) throws IOException {
		m_serverSocket = new ServerSocket(port);
		m_clients = new ArrayList<Client>();
		m_listeners = new ArrayList<NetworkingEventListener>();
		
		new Client(this, m_serverSocket).start();
	}
	
	public void broadcast(NetPackage pack) {
		for(Client client : m_clients) {
			client.send(pack);
		}
	}
	
	public void broadcast(NetPackage pack, DeviceType devices) {
		for(Client client : m_clients) {
			if(client.getDeviceType() == devices)
				client.send(pack);
		}
	}
	
	public ServerSocket getServerSocket() {
		return m_serverSocket;
	}
	
	public synchronized void dataReceived(NetPackage pack) {
		DebugHelper.log("Data received!");
		for(NetworkingEventListener listener : m_listeners) {
			listener.dataReceived(pack);
		}
	}
	
	public void addEventListener(NetworkingEventListener listenter) {
		m_listeners.add(listenter);
	}
	
	public void removeEventListener(NetworkingEventListener listener) {
		m_listeners.remove(listener);
	}
	
	public synchronized void registerClient(Client client) {
		m_clients.add(client);
	}
	
	public synchronized void unregisterClient(Client client) {
		m_clients.remove(client);
	}
	
}
