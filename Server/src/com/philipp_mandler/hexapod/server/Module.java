package com.philipp_mandler.hexapod.server;

public interface Module extends NetworkingEventListener, Runnable {
	public void stop();
	public String getName();
}
