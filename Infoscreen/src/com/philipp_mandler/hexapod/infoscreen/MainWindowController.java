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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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
	
	TextInfo servo1;
	TextInfo servo2;
	TextInfo servo3;
	TextInfo[] goalPosition;
	
	Canvas legGoalCanvas;
	
	Vec3[] m_legGoalPoints;
	
	Paint canvasBg;
	
	Image hexapodFrame;
	
	public MainWindowController() {
		Main.getNetworking().addEventListener(this);
		m_legGoalPoints = new Vec3[6];
		goalPosition = new TextInfo[6];
		hexapodFrame = new Image("images/hexapod-frame.png");
	}
	
	public void initialize(URL location, ResourceBundle resources) {
		
		canvasBg = Color.web("#eee");
		
		legGoalCanvas = new Canvas();
		legGoalCanvas.setWidth(300);
		legGoalCanvas.setHeight(300);
		vbox_infoholder.getChildren().add(legGoalCanvas);
		
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
				servo1.setContent(String.valueOf(legServoPack.getServoPos1()));
				servo2.setContent(String.valueOf(legServoPack.getServoPos2()));
				servo3.setContent(String.valueOf(legServoPack.getServoPos3()));
			}
		}
		else if(pack instanceof LegPositionPackage) {
			LegPositionPackage posPack = (LegPositionPackage)pack;
			if(goalPosition[posPack.getLegIndex()] == null)
				goalPosition[posPack.getLegIndex()] = new TextInfo(vbox_infoholder, "Leg " + posPack.getLegIndex() + " Goal");
			
			goalPosition[posPack.getLegIndex()].setContent(posPack.getGoalPosition().getX() + " | " + posPack.getGoalPosition().getY() + " | " + posPack.getGoalPosition().getZ());
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
		GraphicsContext gc = legGoalCanvas.getGraphicsContext2D();
		gc.setFill(canvasBg);
		gc.fillRect(0, 0, legGoalCanvas.getWidth(), legGoalCanvas.getHeight());
		
		gc.drawImage(hexapodFrame, (legGoalCanvas.getWidth() / 2) - (hexapodFrame.getWidth() / 2), (legGoalCanvas.getHeight() / 2)  - (hexapodFrame.getHeight() / 2));
		
		gc.setFill(Color.BLUE);
		for(Vec3 point : m_legGoalPoints) {
			if(point == null) continue;
			gc.fillOval((point.getX() / 5) + (legGoalCanvas.getWidth() / 2) - 5, (legGoalCanvas.getHeight() / 2) - (point.getY() / 5) - 5, 10, 10);
		}
		gc.setStroke(Color.GREY);
		gc.strokeRect(0, 0, legGoalCanvas.getWidth(), legGoalCanvas.getHeight());
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
		if(servo1 != null) servo1.remove();
		if(servo2 != null) servo2.remove();
		if(servo3 != null) servo3.remove();
		for(int i = 0; i < goalPosition.length; i++) {
			if(goalPosition[i] != null) {
				goalPosition[i].remove();
				goalPosition[i] = null;
			}
		}
		
		servo1 = new TextInfo(vbox_infoholder, "Servo 1");
		servo2 = new TextInfo(vbox_infoholder, "Servo 2");
		servo3 = new TextInfo(vbox_infoholder, "Servo 3");
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
