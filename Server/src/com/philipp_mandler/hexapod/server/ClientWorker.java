package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.*;
import com.philipp_mandler.hexapod.hexapod.net.ExitPackage;
import com.philipp_mandler.hexapod.hexapod.net.NetPackage;
import com.philipp_mandler.hexapod.hexapod.net.WelcomePackage;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientWorker extends Thread {
	
	private ServerSocket m_serverSocket;
	private Socket m_clientSocket;
	private ObjectOutputStream m_objOutputStream;
	private ObjectInputStream m_objInputStream;
	private NetworkManager m_parent;
	private List<NetPackage> m_outQueue;
	private DeviceType deviceType;
	private boolean m_run;
	
	public ClientWorker(NetworkManager parent, ServerSocket serverSocket) {
		m_parent = parent;
		m_serverSocket = serverSocket;
		m_outQueue = new ArrayList<>();
		m_run = true;
		super.setName("Network client worker");
	}
	
	@Override
	public void run() {

		// wait while no client is connected
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

		// register the client to NetworkManager
		m_parent.registerClient(this);

		// create a new ClientWorker to handle next client
		new ClientWorker(m_parent, m_serverSocket).start();

		// handle communication
		while(m_clientSocket.isConnected() && m_run) {
			try {

				// handle network output
				while(!m_outQueue.isEmpty()) {
					m_objOutputStream.writeObject(m_outQueue.get(0));
					m_outQueue.remove(0);
				}

				// handle network input
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
				m_run = false;
			} catch (ClassNotFoundException e) {
				DebugHelper.log(e.toString(), Log.ERROR);
				e.printStackTrace();
			}
			
			try {
				sleep(1);
			} catch (InterruptedException e) {
				DebugHelper.log(e.toString(), Log.ERROR);
			}
		}

		m_parent.clientDisconnected(this);

		// unregister client from NetworkManager
		m_parent.removeClient(this);
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
		// break loop
		m_run = false;
	}
	
	public void send(NetPackage pack) {
		// add NetPackage to outgoing queue
		m_outQueue.add(pack);
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}
	
	public Socket getSocket() {
		return m_clientSocket;
	}
}
