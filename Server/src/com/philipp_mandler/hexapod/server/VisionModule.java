package com.philipp_mandler.hexapod.server;


import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.net.NetPackage;
import com.philipp_mandler.hexapod.hexapod.net.NotificationPackage;
import com.philipp_mandler.hexapod.hexapod.orientation.BooleanMapManager;
import org.openkinect.freenect.*;

import java.nio.ByteBuffer;

public class VisionModule extends Module implements DepthHandler {

	private SingleServo m_servoRotate = Main.getActuatorManager().getKinectServo(0);
	private SingleServo m_servoTilt = Main.getActuatorManager().getKinectServo(1);

	private Device m_kinect;

	private KinectWorker m_kinectWorker;

	private VideoStreamer m_videoStreamer;

	private double m_rotation = 0;
	private double m_tilt = 0;

	private double m_rotationGoal = 0;
	private double m_tiltGoal = 0;

	private TimeTracker m_timeTracker;

	private ButtonGroup m_buttonGroup;

	private VisionServoUpdater m_servoUpdater;


	public VisionModule() {
		super.setName("vision");

		m_timeTracker = Main.getTimeManager().createTracker("vision");

		m_buttonGroup = new ButtonGroup(getName(), "Vision Module");
		m_buttonGroup.addButton(new Button("left", "Left", getName() + " left"));
		m_buttonGroup.addButton(new Button("right", "Right", getName() + " right"));
		m_buttonGroup.addButton(new Button("up", "Up", getName() + " up"));
		m_buttonGroup.addButton(new Button("down", "Down", getName() + " down"));
	}

	@Override
	protected void onStart() {
		m_servoUpdater = new VisionServoUpdater(m_servoRotate, m_servoTilt);
		m_servoUpdater.start();

		m_kinect = Main.getSensorManager().getKinect();

		m_kinectWorker = new KinectWorker();
		m_kinectWorker.start();

		m_videoStreamer = new VideoStreamer();

		if(m_kinect != null) {
			m_kinect.setLed(LedStatus.GREEN);
			m_kinect.setDepthFormat(DepthFormat.D11BIT);
			m_kinect.startDepth(this);
			m_kinect.setVideoFormat(VideoFormat.RGB);
			m_kinect.startVideo(m_videoStreamer);
		}

		Main.getNetworking().addEventListener(this);
		Main.getNetworking().addButtonGroup(m_buttonGroup);
	}

	@Override
	protected void onStop() {
		m_kinectWorker.end();
		m_servoUpdater.shutdown();

		if(m_kinect != null) {
			m_kinect.setLed(LedStatus.BLINK_GREEN);
			m_kinect.stopDepth();
			m_kinect.close();
			m_kinect.stopVideo();
			m_kinect = null;
		}

		Main.getNetworking().addEventListener(this);
		Main.getNetworking().removeButtonGroup(m_buttonGroup);
	}

	@Override
	public void tick(long tick, Time elapsedTime) {
		m_timeTracker.startTracking(tick);
		m_videoStreamer.tick(elapsedTime);
		m_timeTracker.stopTracking();

		m_rotation = ((m_rotationGoal - m_rotation) * (elapsedTime.getSeconds() * 2) + m_rotation);
		m_tilt = ((m_tiltGoal - m_tilt) * (elapsedTime.getSeconds() * 2) + m_tilt);
		m_servoUpdater.setTilt(m_tilt);
		m_servoUpdater.setRotation(m_rotation);
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {

	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {
		if(cmd.length > 1) {
			if(cmd[0].toLowerCase().equals(getName())) {
				if(cmd[1].toLowerCase().equals("up")) {
					setTilt(m_tiltGoal - 0.2);
				}
				else if(cmd[1].toLowerCase().equals("down")) {
					setTilt(m_tiltGoal + 0.2);
				}
				else if(cmd[1].toLowerCase().equals("right")) {
					setRoation(m_rotationGoal - 0.2);
				}
				else if(cmd[1].toLowerCase().equals("left")) {
					setRoation(m_rotationGoal + 0.2);
				}
			}
		}
	}

	@Override
	public void onClientDisconnected(ClientWorker client) {

	}

	@Override
	public void onClientConnected(ClientWorker client) {

	}

	public BooleanMapManager getObstacleMap() {
		return m_kinectWorker.getObstacleMap();
	}

	@Override
	public void onFrameReceived(FrameMode frameMode, ByteBuffer byteBuffer, int i) {
		if(m_kinectWorker != null)
			m_kinectWorker.setKinectData(byteBuffer);
	}

	public double getRotation() {
		return m_rotationGoal;
	}

	public void setRoation(double rot) {
		m_rotationGoal = rot;
	}

	public double getTilt() {
		return m_rotationGoal;
	}

	public void setTilt(double tilt) {
		m_tiltGoal = tilt;
	}
}