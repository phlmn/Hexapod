package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.List;

public class LegUpdater {
	
	private boolean m_running = false;
	private List<Leg> m_legs = new ArrayList<>();
	private ServoController m_servoController;

	public LegUpdater(ServoController servoController) {
		m_servoController = servoController;
	}
	
	public void addLeg(Leg leg) {
		m_legs.add(leg);
	}
	
	public void removeLeg(Leg leg) {
		m_legs.remove(leg);
	}

	public void start() {
		if(m_running) return;
		m_running = true;
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(m_running) {
					List<Integer> values = new ArrayList<>();
					List<Integer> ids = new ArrayList<>();
					for(Leg leg: m_legs) {
						leg.updateServos();
						for(int i = 0; i < 3; i++) {
							if(leg.getServos()[i].isConnected()) {
								ids.add(leg.getServos()[i].getID());
								values.add(leg.getServos()[i].getPosValue());
							}
						}
					}

					int[] valueArray = new int[values.size()];
					int[] idArray = new int[ids.size()];

					for(int i = 0; i < ids.size(); i++) {
						idArray[i] = ids.get(i);
					}

					for(int i = 0; i < values.size(); i++) {
						valueArray[i] = values.get(i);
					}

					m_servoController.syncWriteGoalPosition(idArray, valueArray);

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
