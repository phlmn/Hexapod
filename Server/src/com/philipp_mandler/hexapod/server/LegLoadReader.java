package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class LegLoadReader {

	private AtomicIntegerArray m_values;
	private Leg[] m_legs;

	private boolean m_run = false;


	public LegLoadReader(Leg[] legs) {
		m_values = new AtomicIntegerArray(legs.length);
		m_legs = legs;
	}

	public int getLoad(int leg) {
		return m_values.get(leg);
	}

	public int[] getLoads() {
		int[] data = new int[6];
		for(int i = 0; i < 6; i++) {
			data[i] = m_values.get(i);
		}
		return data;
	}

	public void start() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				m_run = true;
				while (m_run) {
					for (int i = 0; i < m_legs.length; i++) {
						SingleServo servo = m_legs[i].getServo(1);
						m_values.set(i, servo.getCurrentLoad());

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
