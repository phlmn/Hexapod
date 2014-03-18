package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.Vec3;
import org.openkinect.freenect.Context;
import org.openkinect.freenect.Device;
import org.openkinect.freenect.Freenect;

public class SensorManager {

	private Device m_kinect;
	private Context m_context;
	private boolean m_running = false;

	public SensorManager() {
  		m_context = Freenect.createContext();

		if(m_context.numDevices() > 0) {
			m_kinect = m_context.openDevice(0);
		}

		if(m_kinect != null) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					m_running = true;
					while(m_running) {
						if(m_kinect != null) {
							m_kinect.refreshTiltState();
						}

						try {
							Thread.sleep(0, 500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		}
	}

	public void shutdown() {
		m_running = false;
		if(m_kinect != null) {
			m_kinect.close();
		}

		m_context.shutdown();
	}

	public Vec3 getAccel() {
		if(m_kinect != null) {
			double[] data = m_kinect.getAccel();
			return new Vec3(data[0], data[1], data[2]);
		}
		return null;
	}

	public Vec3 getLevel() {
		if(m_kinect != null) {
			double[] data = m_kinect.getAccel();
			Vec3 level = new Vec3(data[0], data[1], data[2]);
			level.normalize();
			return level;
		}
		return null;
	}

	public Device getKinect() {
		return m_kinect;
	}


}
