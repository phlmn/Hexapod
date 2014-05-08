package com.philipp_mandler.hexapod.server;

public class Time {

	private long m_nanoseconds = 0;

	public Time() {

	}

	public void setNanoseconds(long nanoseconds) {
		m_nanoseconds = nanoseconds;
	}

	public void set(Time time) {
		m_nanoseconds = time.m_nanoseconds;
	}

	public void add(Time time) {
		m_nanoseconds += time.m_nanoseconds;
	}

	public long getNanoseconds() {
		return m_nanoseconds;
	}
	public double getMicroseconds() {
		// convert nanoseconds to microseconds
		return m_nanoseconds / 1000.0;
	}

	public double getMilliseconds() {
		// convert nanoseconds to milliseconds
		return m_nanoseconds / 1000000.0;
	}

	public double getSeconds() {
		// convert nanoseconds to seconds
		return m_nanoseconds / 1000000000.0;
	}

	public static Time fromNanoseconds(long nanoseconds) {
		Time time = new Time();
		time.setNanoseconds(nanoseconds);
		return time;
	}

	public static Time fromMicroseconds(long microseconds) {
		Time time = new Time();
		time.setNanoseconds(microseconds * 1000);
		return time;
	}

	public static Time fromMilliseconds(double milliseconds) {
		Time time = new Time();
		time.m_nanoseconds = Math.round(milliseconds * 1000000);
		return time;
	}

	public static Time fromSeconds(double seconds) {
		Time time = new Time();
		time.m_nanoseconds = Math.round(seconds * 1000000000);
		return time;
	}

}
