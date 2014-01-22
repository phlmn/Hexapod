package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.ButtonPackage;

public class Button {

	private ButtonGroup m_group;
	private String m_label;
	private String m_command;
	private String m_id;

	public Button(String id, String label, String command) {
		m_label = label;
		m_command = command;
		m_id = id;
	}

	public void setGroup(ButtonGroup group) {
		m_group = group;
	}

	public ButtonGroup getGroup() {
		return m_group;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	public String getLabel() {
		return m_label;
	}

	public void setCommand(String command) {
		m_command = command;
	}

	public String getCommand() {
		return m_command;
	}

	public void setID(String id) {
		m_id = id;
	}

	public String getID() {
		return m_id;
	}

	public ButtonPackage toPackage() {
		// create a ButtonPackage from this object
		return new ButtonPackage(m_id, m_group.getID(), m_label, m_command);
	}
}
