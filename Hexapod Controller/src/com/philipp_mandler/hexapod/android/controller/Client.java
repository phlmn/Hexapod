package com.philipp_mandler.hexapod.android.controller;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.WelcomePackage;

public class Client extends Thread {
	
	Socket m_socket;
	ObjectOutputStream m_objOutputStream;
	ObjectInputStream m_objInputStream;
	List<NetPackage> m_outQueue;
	Networking m_parent;
	String m_address;
	int m_port;

	public Client(Networking parent, String address, int port) {
		m_parent = parent;
		m_address = address;
		m_port = port;
	}
	
	@Override
	public void run() {
		try {
			m_socket = new Socket(m_address, m_port);
			m_socket.setKeepAlive(true);
			m_objOutputStream = new ObjectOutputStream(m_socket.getOutputStream());
			m_objOutputStream.writeObject(new WelcomePackage(DeviceType.Controller));
			m_objInputStream = new ObjectInputStream(m_socket.getInputStream());
			m_outQueue = new ArrayList<NetPackage>();
		} catch (IOException e) {
			e.printStackTrace();
			m_parent.onConnectionError();
			return;
		}
		
		m_parent.onConnected();
		
		while(m_socket.isConnected()) {
			try {
				while(!m_outQueue.isEmpty()) {
					m_objOutputStream.writeObject(m_outQueue.get(0));
					m_outQueue.remove(0);					
				}
				while(m_socket.getInputStream().available() > 0) {
					Object input = m_objInputStream.readObject();
					if(input instanceof NetPackage)			
						m_parent.onDataReceived((NetPackage)input);
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			try {
				sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		super.run();
	}
	
	public void send(NetPackage pack) {
		m_outQueue.add(pack);
	}
}
