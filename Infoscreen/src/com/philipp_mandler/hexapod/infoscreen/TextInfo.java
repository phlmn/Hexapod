package com.philipp_mandler.hexapod.infoscreen;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TextInfo {

	private Label m_object;
	private String m_label;
	private String m_content;
	private VBox m_parent;
	
	public TextInfo(VBox parent, String label, String content) {
		m_parent = parent;
		m_content = content;
		m_label = label;
		
		m_object = new Label();
		m_object.setStyle("-fx-padding: 4px;");
		updateText();
		
		if(Platform.isFxApplicationThread()) {
			parent.getChildren().add(m_object);
		}
		else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					m_parent.getChildren().add(m_object);			
				}
			});	
		}
	}
	
	public TextInfo(VBox parent, String label) {
		m_parent = parent;
		m_content = "";
		m_label = label;
		
		m_object = new Label();
		m_object.setStyle("-fx-padding: 4px;");
		updateText();
		
		if(Platform.isFxApplicationThread()) {
			parent.getChildren().add(m_object);
		}
		else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					m_parent.getChildren().add(m_object);
				}
			});	
		}
	}
	
	private void updateText() {
		if(Platform.isFxApplicationThread()) {
			m_object.setText(m_label + ": " + m_content);
		}
		else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					m_object.setText(m_label + ": " + m_content);			
				}
			});	
		}
	}
	
	public void remove() {
		if(Platform.isFxApplicationThread()) {
			m_parent.getChildren().remove(m_object);
		}
		else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					m_parent.getChildren().remove(m_object);
				}
			});	
		}
	}
	
	public void setLabel(String label) {
		m_label = label;
		updateText();
	}
	
	public String getLabel() {
		return m_label;
	}
	
	public void setContent(String content) {
		m_content = content;
		updateText();
	}
	
	public String getContent() {
		return m_content;
	}
	
	public Label getObject() {
		return m_object;
	}

}
