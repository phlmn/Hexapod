package com.philipp_mandler.hexapod.server;

public class TimeTrackerEntry {

	private long m_tick;
	private Time m_end;
	private Time m_start;

	public TimeTrackerEntry(long tick, Time start, Time end) {
		m_tick = tick;
		m_start = start;
		m_end = end;
	}

	public Time getDuration() {
		return Time.fromNanoseconds(m_end.getNanoseconds() - m_start.getNanoseconds());
	}

	public Time getStart() {
		return m_start;
	}

	public Time getEnd() {
		return m_end;
	}

	public long getTick() {
		return m_tick;
	}
}
