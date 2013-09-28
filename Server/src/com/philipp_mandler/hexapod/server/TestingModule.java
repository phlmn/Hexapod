package com.philipp_mandler.hexapod.server;

import org.openkinect.freenect.*;

import javax.swing.*;
import java.awt.*;
import java.nio.ByteBuffer;

public class TestingModule extends Module implements DepthHandler {

	private Context m_Context;
	private Device m_Kinect;
	private JFrame m_frame;
	private KinectDisplay m_kinectDisplay;

	public TestingModule() {

	}

	@Override
	public String getName() {
		return "test";
	}

	@Override
	protected void onStart() {
		DebugHelper.log("Module started");
		m_Context = Freenect.createContext();
		DebugHelper.log("Devices detected: " + m_Context.numDevices());
		if(m_Context.numDevices() > 0)
			m_Kinect = m_Context.openDevice(0);

		if(m_Kinect != null) {
			m_Kinect.setLed(LedStatus.GREEN);
			m_Kinect.setDepthFormat(DepthFormat.D11BIT);
			m_Kinect.startDepth(this);
		}

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
		if(m_Kinect != null) {
			m_Kinect.setLed(LedStatus.BLINK_GREEN);
			m_Kinect.stopDepth();
			m_Kinect.close();
			m_Kinect = null;
		}
		m_Context.shutdown();

		if(m_kinectDisplay != null) m_kinectDisplay.dispose();
		if(m_frame != null) m_frame.dispose();
	}

	@Override
	public void tick(Time elapsedTime) {

	}

	@Override
	public void onFrameReceived(FrameMode frameMode, ByteBuffer byteBuffer, int i) {
		if(m_kinectDisplay != null)
			m_kinectDisplay.setKinectData(byteBuffer);
	}
}
