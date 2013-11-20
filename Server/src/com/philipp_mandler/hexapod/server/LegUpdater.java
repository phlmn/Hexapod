package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.List;

public class LegUpdater {
	
	private boolean m_running = false;
	private WalkingModule m_walkingModule;

	public LegUpdater(WalkingModule walkingModule) {
		m_walkingModule = walkingModule;
	}

	public void start() {
		if(m_running) return;
		m_running = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(m_running) {

					for(Leg leg : m_walkingModule.getLegs()) {
						leg.updateServos();
					}

					Main.getActuatorManager().syncUpdateLegServos();

					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						DebugHelper.log(e.toString(), Log.ERROR);
					}
				}
			}
		});
	}
	
	public void stop() {
		m_running = false;
	}
	
	public boolean isRunning() {
		return m_running;
	}
}
