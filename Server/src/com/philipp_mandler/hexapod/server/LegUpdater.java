package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.List;

public class LegUpdater extends Thread {
	
	private boolean m_running = false;
	private List<Leg> m_legs = new ArrayList<Leg>();

	public LegUpdater() {
		
	}
	
	public void addLeg(Leg leg) {
		m_legs.add(leg);
	}
	
	public void removeLeg(Leg leg) {
		m_legs.remove(leg);
	}
	
	@Override
	public void run() {
		m_running = true;
		while(m_running) {
			for(Leg leg: m_legs) {
				leg.updateServos();
			}
			
			try {
				sleep(10);
			} catch (InterruptedException e) {
				DebugHelper.log(e.toString(), Log.ERROR);
			}
		}
	}
	
	public void end() {
		m_running = false;
	}
	
	public boolean isRunning() {
		return m_running;
	}
}
