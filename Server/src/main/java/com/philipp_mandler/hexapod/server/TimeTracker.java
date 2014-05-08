package com.philipp_mandler.hexapod.server;


import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TimeTracker {

	private String m_name;
	private List<TimeTrackerEntry> m_entries = new CopyOnWriteArrayList<>();
	private List<TimeTrackerAction> m_actions = new CopyOnWriteArrayList<>();
	private long m_tick = 0;
	private boolean m_running = false;
	private Time m_startTime;
	private int m_size;

	public TimeTracker(String name, int size) {
		m_size = size;
		m_name = name;
	}

	public TimeTrackerAction trackAction(String action) {
		for(TimeTrackerAction a : m_actions) {
			if(action.equals(a.getName())) {
				a.startTracking(m_tick);
				return a;
			}
		}

		TimeTrackerAction a = new TimeTrackerAction(action, m_size);
		m_actions.add(a);
		a.startTracking(m_tick);
		return a;
	}

	public void startTracking(long tick) {
		m_tick = tick;
		m_startTime = Time.fromNanoseconds(System.nanoTime());
		m_running = true;
	}

	public TimeTrackerEntry stopTracking() {
		if(m_running) {
			TimeTrackerEntry entry = new TimeTrackerEntry(m_tick, m_startTime, Time.fromNanoseconds(System.nanoTime()));
			m_entries.add(0, entry);
			while(m_entries.size() > m_size) {
				m_entries.remove(m_entries.size() - 1);
			}
			m_running = false;
			return entry;
		}
		return null;
	}

	public Time getAverage(int size) {
		int count = 0;
		double value = 0;
		for(TimeTrackerEntry entry : m_entries) {
			value += entry.getDuration().getNanoseconds();
			count++;
			if(count >= size) break;
		}
		if(count == 0) return Time.fromNanoseconds(0);
		return Time.fromNanoseconds(Math.round(value / count));
	}

	public List<TimeTrackerAction> getActions() {
		return m_actions;
	}

	public String getName() {
		return m_name;
	}
}
