package com.philipp_mandler.hexapod.hexapod.net;


public class ButtonPackage implements NetPackage {

	private static final long serialVersionUID = 3369191046760427913L;

	private String m_group;
	private String m_label;
	private String m_command;
	private boolean m_delete = false;
	private String m_id;

	public ButtonPackage(String id, String group, String label, String command) {
		m_group = group;
		m_label = label;
		m_command = command;
		m_id = id;
	}

	public void setID(String id) {
		m_id = id;
	}

	public String getID() {
		return m_id;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public String getGroup() {
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

	public void setDelete(boolean delete) {
		m_delete = delete;
	}

	public boolean getDelete() {
		return m_delete;
	}
}
