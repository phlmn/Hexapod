package com.philipp_mandler.hexapod.infoscreen;

import java.io.IOException;
import java.net.URL;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application implements NetworkingEventListener {

	static Networking m_networking = new Networking();
	MainWindowController controller;	
	
	@Override
	public void start(Stage primaryStage) {
		Main.getNetworking().addEventListener(this);
		URL location = getClass().getResource("MainWindow.fxml");
		FXMLLoader fxmlLoader = new FXMLLoader();
		fxmlLoader.setLocation(location);
		Parent root;

		try {
			root = (Parent)fxmlLoader.load(location.openStream());
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		controller = fxmlLoader.getController();
		
		primaryStage.setScene(new Scene(root));
		primaryStage.sizeToScene();
		primaryStage.setTitle("Hexapod Infoscreen");
		primaryStage.show();
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Main.getNetworking().disconnect();
			}
		});
	}

	public static void main(String[] args) {
		launch(args);
	}
	
	public static Networking getNetworking() {
		return m_networking;
	}

	public void onDataReceived(NetPackage pack) {
		
	}

	public void onConnected() {
		
	}

	public void onDisconnected() {
		
	}

	public void onConnectionError() {
		
	}
}
