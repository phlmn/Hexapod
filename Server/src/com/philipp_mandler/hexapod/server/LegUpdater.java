package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;

public class LegUpdater {
	
	private boolean m_running = false;

	private ArrayList<Leg> m_legs = new ArrayList<>();

	public LegUpdater() {

	}

	public void removeLeg(Leg leg) {
		m_legs.remove(leg);
	}

	public void clearLegs(Leg leg) {
		m_legs.clear();
	}

	public void addLeg(Leg leg) {
		m_legs.add(leg);
	}

	public void start() {
		if(m_running) return;
		m_running = true;
		// start new Thread for sync updating the leg servos
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(m_running) {

					for(Leg leg : m_legs) {
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
		}).start();
	}
	
	public void stop() {
		m_running = false;
	}
	
	public boolean isRunning() {
		return m_running;
	}
}
