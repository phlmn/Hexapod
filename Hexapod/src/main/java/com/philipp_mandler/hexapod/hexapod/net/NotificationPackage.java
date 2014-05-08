package com.philipp_mandler.hexapod.hexapod.net;


public class NotificationPackage implements NetPackage {

	private static final long serialVersionUID = 198061305320904649L;

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
