package com.philipp_mandler.hexapod.server;

public class Log {	
	static final int INFO = 0;
	static final int ERROR = 1;
	static final int CRITICAL = 2;
	
	private String text;
	private int level;
	
	public Log(String text, int level) {
		this.text = text;
		this.level = level;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public int getLevel() {
		return level;
	}
	
	public void setLevel(int level) {
		this.level = level;
	}
}
