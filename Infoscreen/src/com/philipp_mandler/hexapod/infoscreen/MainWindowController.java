package com.philipp_mandler.hexapod.infoscreen;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

import com.philipp_mandler.hexapod.hexapod.LegPositionPackage;
import com.philipp_mandler.hexapod.hexapod.LegServoPackage;
import com.philipp_mandler.hexapod.hexapod.NetPackage;
import com.philipp_mandler.hexapod.hexapod.Vec3;

public class MainWindowController implements Initializable, NetworkingEventListener {

	@FXML Button button_connect;
	@FXML TextField textfield_connectAddress;	
	@FXML VBox vbox_infoholder;
	@FXML Pane pane_canvasHolder;
	@FXML TextArea textarea_console;
	@FXML TextField textfield_consoleInput;
	
	TextInfo m_servo1;
	TextInfo m_servo2;
	TextInfo m_servo3;
	TextInfo[] m_goalPosition;
	
	Canvas m_legGoalCanvas;
	
	Vec3[] m_legGoalPoints;
	
	Paint m_canvasBg;
	Paint m_legEndColor;
	
	Image m_hexapodFrame;
	
	public MainWindowController() {
		Main.getNetworking().addEventListener(this);
		m_legGoalPoints = new Vec3[6];
		m_goalPosition = new TextInfo[6];
		m_hexapodFrame = new Image("images/hexapod-frame.png");
	}
	
	public void initialize(URL location, ResourceBundle resources) {
		
		m_canvasBg = Color.web("#fff");
		m_legEndColor = Color.web("#369ed3");
		
		m_legGoalCanvas = new Canvas();
		m_legGoalCanvas.setWidth(300);
		m_legGoalCanvas.setHeight(300);
		pane_canvasHolder.getChildren().add(m_legGoalCanvas);
		
		drawLegGoals();
	}
	
	@FXML
	protected void button_connect_click(ActionEvent event) {
		if(Main.getNetworking().isConnected()) {
			Main.getNetworking().disconnect();				
		}
		else {
			if(textfield_connectAddress.getText().isEmpty()) return;
			Main.getNetworking().connect(textfield_connectAddress.getText(), 8888);
		}
	}

	public void onDataReceived(NetPackage pack) {
		if(pack instanceof LegServoPackage) {
			LegServoPackage legServoPack = (LegServoPackage)pack;
			if(legServoPack.getLegID() == 0) {
				m_servo1.setContent(String.valueOf(legServoPack.getServoPos1()));
				m_servo2.setContent(String.valueOf(legServoPack.getServoPos2()));
				m_servo3.setContent(String.valueOf(legServoPack.getServoPos3()));
			}
		}
		else if(pack instanceof LegPositionPackage) {
			LegPositionPackage posPack = (LegPositionPackage)pack;
			if(m_goalPosition[posPack.getLegIndex()] == null)
				m_goalPosition[posPack.getLegIndex()] = new TextInfo(vbox_infoholder, "Leg " + posPack.getLegIndex() + " Goal");
			
			m_goalPosition[posPack.getLegIndex()].setContent(posPack.getGoalPosition().getX() + " | " + posPack.getGoalPosition().getY() + " | " + posPack.getGoalPosition().getZ());
			m_legGoalPoints[posPack.getLegIndex()] = new Vec3(posPack.getGoalPosition());
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					drawLegGoals();
				}
			});
		}
	}
	
	private void drawLegGoals() {
		GraphicsContext gc = m_legGoalCanvas.getGraphicsContext2D();
		gc.setFill(m_canvasBg);
		gc.fillRect(0, 0, m_legGoalCanvas.getWidth(), m_legGoalCanvas.getHeight());
		
		gc.drawImage(m_hexapodFrame, (m_legGoalCanvas.getWidth() / 2) - (m_hexapodFrame.getWidth() / 2), (m_legGoalCanvas.getHeight() / 2)  - (m_hexapodFrame.getHeight() / 2));
		
		gc.setFill(m_legEndColor);
		for(Vec3 point : m_legGoalPoints) {
			if(point == null) continue;
			gc.fillOval((point.getX() / 5) + (m_legGoalCanvas.getWidth() / 2) - 5, (m_legGoalCanvas.getHeight() / 2) - (point.getY() / 5) - 5, 10, 10);
		}
	}
	
	@FXML
	public void onConsoleKeyPressed(KeyEvent event) {
		if(Main.getNetworking().isConnected() && (event).getCode() == KeyCode.ENTER) {
			textarea_console.appendText("\n> " + textfield_consoleInput.getText());
			textfield_consoleInput.setText("");
		}
	}

	public void onConnected() {
		if(Platform.isFxApplicationThread()) {
			button_connect.setText("Disconnect");
		}
		else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					button_connect.setText("Disconnect");				
				}
			});	
		}
		if(m_servo1 != null) m_servo1.remove();
		if(m_servo2 != null) m_servo2.remove();
		if(m_servo3 != null) m_servo3.remove();
		for(int i = 0; i < m_goalPosition.length; i++) {
			if(m_goalPosition[i] != null) {
				m_goalPosition[i].remove();
				m_goalPosition[i] = null;
			}
		}
		
		m_servo1 = new TextInfo(vbox_infoholder, "Servo 1");
		m_servo2 = new TextInfo(vbox_infoholder, "Servo 2");
		m_servo3 = new TextInfo(vbox_infoholder, "Servo 3");
	}

	public void onDisconnected() {
		if(Platform.isFxApplicationThread()) {
			button_connect.setText("Connect");
		}
		else {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					button_connect.setText("Connect");				
				}
			});	
		}
	}

	public void onConnectionError() {
		
	}
}
