package com.philipp_mandler.hexapod.server;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimeTrackerAction {

	private String m_name;
	private Time m_startTime;
	private boolean m_running = false;
	private List<TimeTrackerEntry> m_values = new CopyOnWriteArrayList<>();
	private long m_tick = 0;

	public TimeTrackerAction(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public void startTracking(long tick) {
		m_tick = tick;
		m_startTime = Time.fromNanoseconds(System.nanoTime());
		m_running = true;
	}

	public TimeTrackerEntry stopTracking() {
		if(m_running) {
			TimeTrackerEntry entry = new TimeTrackerEntry(m_tick, m_startTime, Time.fromNanoseconds(System.nanoTime()));
			m_values.add(0, entry);
			m_running = false;
			return entry;
		}
		return null;
	}

	public Time getAverage(int size) {
		int count = 0;
		double value = 0;
		for(TimeTrackerEntry entry : m_values) {
			value += entry.getDuration().getNanoseconds();
			count++;
			if(count >= size) break;
		}
		if(count == 0) return Time.fromNanoseconds(0);
		return Time.fromNanoseconds(Math.round(value / count));
	}

	public List<TimeTrackerEntry> getEntries() {
		return m_values;
	}

}
