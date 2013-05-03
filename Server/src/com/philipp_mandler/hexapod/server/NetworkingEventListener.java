package com.philipp_mandler.hexapod.server;

import java.util.EventListener;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public interface NetworkingEventListener extends EventListener {
	public void onDataReceived(ClientWorker client, NetPackage pack);
	public void onCmdReceived(ClientWorker client, String[] cmd);
	public void onClientDisconnected(ClientWorker client);
	public void onClientConnected(ClientWorker client);
}
