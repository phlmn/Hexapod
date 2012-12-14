package com.philipp_mandler.hexapod.server;

import java.util.EventListener;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public interface NetworkingEventListener extends EventListener {
	public void dataReceived(NetPackage pack);
}
