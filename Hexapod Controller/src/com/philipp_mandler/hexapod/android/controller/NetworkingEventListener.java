package com.philipp_mandler.hexapod.android.controller;

import java.util.EventListener;

import android.os.Handler;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public interface NetworkingEventListener extends EventListener {
	public void onDataReceived(NetPackage pack);
	
	public void onConnected();
	public void onDisconnected();
	public void onConnectionError();
	
	public Handler getHandler();
}
