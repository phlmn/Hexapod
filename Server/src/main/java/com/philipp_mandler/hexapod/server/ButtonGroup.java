package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.net.ButtonGroupPackage;

import java.util.ArrayList;

public class ButtonGroup {

	private String m_id;
	private String m_label;
	private ArrayList<Button> m_buttons;

	public ButtonGroup(String id, String label) {
		m_id = id;
		m_label = label;
		m_buttons = new ArrayList<>();
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

	public void addButton(Button button) {
		m_buttons.add(button);
		button.setGroup(this);
	}

	public void removeButton(Button button) {
		button.setGroup(null);
		m_buttons.remove(button);
	}

	public ArrayList<Button> getButtons() {
		return m_buttons;
	}

	public ButtonGroupPackage toPackage() {
		// create a ButtonGroupPackage from this object
		return new ButtonGroupPackage(m_id, m_label);
	}

}
