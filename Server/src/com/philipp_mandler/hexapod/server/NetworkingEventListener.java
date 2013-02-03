package com.philipp_mandler.hexapod.server;

import java.util.EventListener;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public interface NetworkingEventListener extends EventListener {
	public void onDataReceived(Client client, NetPackage pack);
	public void onCmdReceived(Client client, String[] cmd);
	public void onClientDisconnected(Client client);
	public void onClientConnected(Client client);
}
