package com.philipp_mandler.hexapod.hexapod;


public class NotificationPackage implements NetPackage {
	private String m_text;

	public NotificationPackage(String text) {
		m_text = text;
	}

	public void setText(String text) {
		m_text = text;
	}

	public String getText() {
		return m_text;
	}
}
