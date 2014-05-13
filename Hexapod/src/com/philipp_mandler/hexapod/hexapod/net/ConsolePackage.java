package com.philipp_mandler.hexapod.hexapod.net;

public class ConsolePackage implements NetPackage {

	private static final long serialVersionUID = 1754181619084998648L;
	
	private String m_text;
	
	public ConsolePackage(String text) {
		m_text = text;
	}
	
	public void setText(String text) {
		m_text = text;
	}
	
	public String getText() {
		return m_text;
	}

}
