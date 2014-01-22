package com.philipp_mandler.hexapod.android.controller;


import android.view.View;
import com.philipp_mandler.hexapod.hexapod.ButtonGroupPackage;

import java.util.ArrayList;

public class ButtonGroup {

	private String m_id;
	private String m_label;
	private ArrayList<Button> m_buttons;
	private View m_view;

	public ButtonGroup(String id, String label) {
		m_id = id;
		m_label = label;
		m_buttons = new ArrayList<>();
	}

	public ButtonGroup(ButtonGroupPackage pack, View view) {
		m_id = pack.getID();
		m_label = pack.getID();
		m_buttons = new ArrayList<>();
		m_view = view;
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

	public void setView(View view) {
		m_view = view;
	}

	public View getView() {
		return m_view;
	}

}
