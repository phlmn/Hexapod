package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class TestingModule extends Module {

	@Override
	public String getName() {
		return "testing";
	}

	@Override
	protected void onStart() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	protected void onStop() {
		//To change body of implemented methods use File | Settings | File Templates.
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {

	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {

	}

	@Override
	public void onClientDisconnected(ClientWorker client) {

	}

	@Override
	public void onClientConnected(ClientWorker client) {

	}

	@Override
	public void tick(Time elapsedTime) {

	}
}
