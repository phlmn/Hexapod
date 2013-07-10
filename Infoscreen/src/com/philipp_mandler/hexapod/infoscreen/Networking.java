package com.philipp_mandler.hexapod.infoscreen;

import java.util.ArrayList;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class Networking {
	
	Client m_client;
	ArrayList<NetworkingEventListener> m_listeners;
	boolean m_connected = false;

	public Networking() {		
		m_listeners = new ArrayList<>();
	}
	
	public void connect(String address, int port) {
		m_client = new Client(this, address, port);
		m_client.start();
	}
	
	public void onDataReceived(final NetPackage pack) {
		for(final NetworkingEventListener listener : m_listeners) {
			listener.onDataReceived(pack);
		}
	}
	
	public void onConnected() {
		m_connected = true;
		for(final NetworkingEventListener listener : m_listeners) {
			listener.onConnected();
		}
	}
	
	public void onConnectionError() {
		for(final NetworkingEventListener listener : m_listeners) {
			listener.onConnectionError();
		}
	}
	
	public void onDisconnected() {
		m_connected = false;
		for(final NetworkingEventListener listener : m_listeners) {
			listener.onDisconnected();
		}
	}
	
	public void send(NetPackage pack) {
		if(m_connected)
			m_client.send(pack);
	}
	
	public void disconnect() {
		if(m_connected)
			m_client.disconnect();
	}
	
	public void addEventListener(NetworkingEventListener listener) {
		m_listeners.add(listener);
	}
	
	public void removeEventListener(NetworkingEventListener listener) {
		m_listeners.remove(listener);
	}
	
	public boolean isConnected() {
		return m_connected;
	}
}
