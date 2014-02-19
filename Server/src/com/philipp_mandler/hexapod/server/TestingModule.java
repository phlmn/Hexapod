package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.net.NetPackage;
import org.openkinect.freenect.*;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;

public class TestingModule extends Module implements DepthHandler {

	private Context m_Context;
	private Device m_kinect;
	private JFrame m_frame;
	private KinectDisplay m_kinectDisplay;

	public TestingModule() {
		m_Context = Freenect.createContext();

	}

	@Override
	public String getName() {
		return "test";
	}

	@Override
	protected void onStart() {
		DebugHelper.log("Module started");

		// detect devices
		DebugHelper.log("Devices detected: " + m_Context.numDevices());
		m_kinect = Main.getSensorManager().getKinect();

		// start Kinect
		if(m_kinect != null) {
			m_kinect.setLed(LedStatus.GREEN);
			m_kinect.setDepthFormat(DepthFormat.D11BIT);
			m_kinect.startDepth(this);
		}

		// create Kinect output window
		if(!GraphicsEnvironment.isHeadless()) {
			m_kinectDisplay = new KinectDisplay();

			m_frame = new JFrame();
			m_frame.add(m_kinectDisplay);
			m_frame.setSize(640, 480);
			m_kinectDisplay.init();
			m_frame.setLocationRelativeTo(null);
			m_frame.setVisible(true);
			m_frame.toFront();
			m_frame.setResizable(false);
			m_frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}

	}

	@Override
	protected void onStop() {
		// stop Kinect
		if(m_kinect != null) {
			m_kinect.setLed(LedStatus.BLINK_GREEN);
			m_kinect.stopDepth();
			m_kinect.close();
			m_kinect.stopVideo();
			m_kinect = null;
		}

		// dispose Kinect output window
		if(m_kinectDisplay != null) {
			m_kinectDisplay.dispose();
			m_kinectDisplay = null;
		}

		// dispose Kinect display frame
		if(m_frame != null) {
			m_frame.dispose();
			m_frame = null;
		}
	}

	@Override
	public void tick(Time elapsedTime) {

	}

	@Override
	public void onFrameReceived(FrameMode frameMode, ByteBuffer byteBuffer, int i) {
		// send received Kinect frame  to KinectDisplay
		if(m_kinectDisplay != null)
			m_kinectDisplay.setKinectData(byteBuffer);
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {

	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {

	}

	@Override
	public void onClientDisconnected(ClientWorker client) {

	}

	@Override
	public void onClientConnected(ClientWorker client) {

	}
}
