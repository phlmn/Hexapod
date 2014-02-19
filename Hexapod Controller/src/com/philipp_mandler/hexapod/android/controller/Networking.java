package com.philipp_mandler.hexapod.android.controller;

import com.philipp_mandler.hexapod.hexapod.net.ExitPackage;
import com.philipp_mandler.hexapod.hexapod.net.NetPackage;

import java.util.ArrayList;

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
			if(listener.getHandler() != null)
				listener.getHandler().post(new Runnable() {					
					public void run() {
						listener.onDataReceived(pack);
					}
				});
			else
				listener.onDataReceived(pack);
		}
	}
	
	public void onConnected() {
		m_connected = true;
		for(final NetworkingEventListener listener : m_listeners) {
			if(listener.getHandler() != null)
				listener.getHandler().post(new Runnable() {					
					public void run() {
						listener.onConnected();
					}
				});
			else
				listener.onConnected();
		}
	}
	
	public void onConnectionError() {
		for(final NetworkingEventListener listener : m_listeners) {
			if(listener.getHandler() != null)
				listener.getHandler().post(new Runnable() {					
					public void run() {
						listener.onConnectionError();
					}
				});
			else
				listener.onConnectionError();
		}
	}
	
	public void onDisconnected() {
		for(final NetworkingEventListener listener : m_listeners) {
			if(listener.getHandler() != null)
				listener.getHandler().post(new Runnable() {					
					public void run() {
						listener.onDisconnected();
					}
				});
			else
				listener.onDisconnected();
		}
	}
	
	public void send(NetPackage pack) {
		if(m_connected)
			m_client.send(pack);
	}
	
	public void disconnect() {
		send(new ExitPackage());
		m_connected = false;
		onDisconnected();
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
