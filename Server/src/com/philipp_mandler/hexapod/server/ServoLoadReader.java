package com.philipp_mandler.hexapod.server;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ServoLoadReader {

	private AtomicIntegerArray m_values = new AtomicIntegerArray(6);

	private boolean m_run = false;

	public ServoLoadReader() {

	}

	public int getLoad(int leg) {
		return m_values.get(leg);
	}

	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				m_run = true;
				while (m_run) {
					for (int i = 0; i < 6; i++) {
						SingleServo servo = Main.getActuatorManager().getLegServo(i, 1);
						if(servo != null) {
							m_values.set(i, servo.getCurrentLoad());
						}
						try {
							Thread.sleep(2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}).start();
	}

	public void stop() {
		m_run = false;
	}

}
