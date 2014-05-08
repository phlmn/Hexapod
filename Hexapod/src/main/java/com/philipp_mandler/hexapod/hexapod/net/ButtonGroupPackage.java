package com.philipp_mandler.hexapod.hexapod.net;

public class ButtonGroupPackage implements NetPackage {

	private static final long serialVersionUID = -47365510995139599L;
	private String m_id;
	private String m_label;
	private boolean m_delete = false;

	public ButtonGroupPackage(String id, String label) {
		m_id = id;
		m_label = label;
	}

	public void setID(String id) {
		m_id = id;
	}

	public String getID() {
		return m_id;
	}

	public void setLabel(String label) {
		m_label = label;
	}

	public String getLabel() {
		return m_label;
	}

	public void setDelete(boolean delete) {
		m_delete = delete;
	}

	public boolean getDelete() {
		return m_delete;
	}

}
