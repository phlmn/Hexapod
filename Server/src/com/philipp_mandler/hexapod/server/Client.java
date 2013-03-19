package com.philipp_mandler.hexapod.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.philipp_mandler.hexapod.hexapod.DeviceType;
import com.philipp_mandler.hexapod.hexapod.ExitPackage;
import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.WelcomePackage;

public class Client extends Thread {
	
	private ServerSocket m_serverSocket;
	private Socket m_clientSocket;
	private ObjectOutputStream m_objOutputStream;
	private ObjectInputStream m_objInputStream;
	private ServerNetworking m_parent;
	private List<NetPackage> m_outQueue;
	private DeviceType deviceType;
	private boolean m_run;
	
	public Client(ServerNetworking parent, ServerSocket serverSocket) {
		m_parent = parent;
		m_serverSocket = serverSocket;
		m_outQueue = new ArrayList<NetPackage>();
		m_run = true;
		super.setName("Network client worker");
	}
	
	@Override
	public void run() {
		while(m_clientSocket == null) {
			try {				
				m_clientSocket = m_serverSocket.accept();
				m_clientSocket.setKeepAlive(true);
				m_objOutputStream = new ObjectOutputStream(m_clientSocket.getOutputStream());
				m_objOutputStream.writeObject(new WelcomePackage(DeviceType.Server));
				m_objInputStream = new ObjectInputStream(m_clientSocket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				try {
					sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
		
		m_parent.registerClient(this);
		new Client(m_parent, m_serverSocket).start();
		
		while(m_clientSocket.isConnected() && m_run) {
			try {
				while(!m_outQueue.isEmpty()) {
					m_objOutputStream.writeObject(m_outQueue.get(0));
					m_outQueue.remove(0);
				}
				while(m_clientSocket.getInputStream().available() > 0) {
					Object input = m_objInputStream.readObject();
					if(input instanceof NetPackage) {
						if(input instanceof WelcomePackage) {
							if(deviceType == null) {
								deviceType = ((WelcomePackage)input).getDeviceType();
								m_parent.clientConnected(this);
							}							
						}
						else if(input instanceof ExitPackage) {
							m_run = false;
						}
						else {
							m_parent.dataReceived(this, (NetPackage)input);
						}
					}
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
		m_parent.clientDisconnected(this);
		m_parent.unregisterClient(this);
		try {
			m_objInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			m_objOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			m_clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		super.run();
	}
	
	public void disconnect() {
		m_run = false;
	}
	
	public void send(NetPackage pack) {
		m_outQueue.add(pack);
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}
	
	public Socket getSocket() {
		return m_clientSocket;
	}
}
