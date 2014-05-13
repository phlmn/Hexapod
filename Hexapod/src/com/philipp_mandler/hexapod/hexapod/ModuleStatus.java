package com.philipp_mandler.hexapod.hexapod;

import java.io.Serializable;

public class ModuleStatus implements Serializable {

	private static final long serialVersionUID = -8836134610024899096L;
	private String m_name;
	private boolean m_running;

	public ModuleStatus(String name, boolean running) {
		m_name = name;
		m_running = running;
	}

	public void setName(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public void setRunning(boolean running) {
		m_running = running;
	}

	public boolean getRunning() {
		return m_running;
	}
}
