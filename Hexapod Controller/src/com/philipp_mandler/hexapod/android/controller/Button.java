package com.philipp_mandler.hexapod.android.controller;


import android.view.View;
import com.philipp_mandler.hexapod.hexapod.net.ButtonPackage;

public class Button {

	private ButtonGroup m_group;
	private String m_label;
	private String m_command;
	private String m_id;
	private View m_view;

	public Button(String id, String label, String command) {
		m_label = label;
		m_command = command;
		m_id = id;
	}

	public Button(ButtonPackage pack, View view) {
		m_label = pack.getLabel();
		m_command = pack.getCommand();
		m_id = pack.getID();
		m_view = view;
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

	public void setView(View view) {
		m_view = view;
	}

	public View getView() {
		return m_view;
	}

}
