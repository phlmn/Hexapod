package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.List;

import SimpleDynamixel.Servo;

public class LegUpdater extends Thread {
	
	private boolean m_running = false;
	private List<Leg> m_legs = new ArrayList<Leg>();
	private Servo m_servoController;

	public LegUpdater(Servo servoController) {
		m_servoController = servoController;
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
			List<Integer> values = new ArrayList<Integer>();
			List<Integer> ids = new ArrayList<Integer>();
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
				sleep(20);
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
