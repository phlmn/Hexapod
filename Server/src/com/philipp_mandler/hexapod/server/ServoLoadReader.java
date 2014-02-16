package com.philipp_mandler.hexapod.server;

import java.util.HashMap;
import java.util.Map;

public class ServoLoadReader {


	private HashMap<SingleServo, Integer> m_values = new HashMap<>();

	private Thread m_worker;

	private boolean m_run = false;

	public ServoLoadReader() {

	}

	public void registerServo(SingleServo servo) {
		m_values.put(servo, 0);
	}

	public void removeServo(SingleServo servo) {
		m_values.remove(servo);
	}

	public void clearServos() {
		m_values.clear();
	}

	public int getLoad(SingleServo servo) {
		return m_values.get(servo);
	}

	public void start() {
		m_worker = new Thread(new Runnable() {
			@Override
			public void run() {
				while(m_run) {
					for(Map.Entry<SingleServo, Integer> entry: m_values.entrySet()) {
						entry.setValue(entry.getKey().getCurrentLoad());
						try {
							Thread.sleep(2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		m_run = true;
		m_worker.start();
	}

	public void stop() {
		m_run = false;
	}

}
