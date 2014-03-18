package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;

public class TimeManager {

	private ArrayList<TimeTracker> m_timeTracker = new ArrayList<>();
	private boolean m_running = true;
	private int m_size;

	public TimeManager(int size) {
		m_size = size;

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(m_running) {

					String text = "";
					for(TimeTracker tracker : m_timeTracker)  {
						text += "Tracker: " + tracker.getName() + "    avg: " + tracker.getAverage(100).getNanoseconds() + "ns";

						for(TimeTrackerAction a : tracker.getActions()) {
							text += "\n\t" + a.getName() + "    avg: " + a.getAverage(100).getNanoseconds() + "ns";
						}

						text += "\n\n";
					}

					DebugHelper.log(text);

					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		thread.start();

	}

	public TimeTracker createTracker(String name) {
		TimeTracker tracker = new TimeTracker(name, m_size);
		m_timeTracker.add(tracker);
		return tracker;
	}

	public void removeTracker(TimeTracker tracker) {
		m_timeTracker.remove(tracker);
	}

	public void stop() {
		m_running = false;
	}
}
