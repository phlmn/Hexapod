package com.philipp_mandler.hexapod.server;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by philipp on 22.05.14.
 */
public class VisionServoUpdater extends Thread {

	private SingleServo m_servoRotate;
	private SingleServo m_servoTilt;

	private AtomicInteger m_rotation = new AtomicInteger();
	private AtomicInteger m_tilt = new AtomicInteger();

	private boolean m_running = false;

	public VisionServoUpdater(SingleServo rotate, SingleServo tilt) {
		m_servoRotate = rotate;
		m_servoTilt = tilt;
	}

	@Override
	public void run() {
		m_running = true;
		while(m_running) {

			if(m_servoRotate.isConnected()) m_servoRotate.setGoalPosition(Math.PI + (m_rotation.get() / 4096.0));
			if(m_servoTilt.isConnected()) m_servoTilt.setGoalPosition(Math.PI + (m_tilt.get() / 4096.0));
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setTilt(double tilt) {
		m_tilt.set((int)(tilt * 4096));
	}

	public void setRotation(double rot) {
   		m_rotation.set((int)(rot * 4096));
	}

	public void shutdown() {
		m_running = false;
	}
}
