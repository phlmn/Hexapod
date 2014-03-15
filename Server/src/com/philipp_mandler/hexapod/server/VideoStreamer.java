package com.philipp_mandler.hexapod.server;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VideoStreamer extends Thread {

	private boolean m_running = false;

	private byte[] m_videoData;

	private final Object m_lock = new Object();

	private Time m_elapsedTime = Time.fromMicroseconds(0);

	private byte[] m_compressedData;


	public VideoStreamer() {

	}

	@Override
	public void run() {
		m_running = true;

		while(m_running) {
			synchronized (m_lock) {
				if(m_videoData != null) {
					try {
						BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);

						int pos = 0;
						for(int y = 0; y < 480; y++) {
							for(int x = 0; x < 640; x++) {
								int color =  m_videoData[pos];
								color = color << 8;
								color += m_videoData[pos + 1];
								color = color << 8;
								color += m_videoData[pos + 2];
								image.setRGB(x, y, color);
								pos += 3;
							}
						}

						ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


						ImageIO.write(image, "jpg", outputStream);

						m_compressedData = outputStream.toByteArray();
					} catch (IOException e) {
						e.printStackTrace();
					}

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public void setVideoData(byte[] data) {
		synchronized (m_lock) {
			m_videoData = data;
		}
	}

	public byte[] getVideoData() {
		return m_videoData;
	}

	public void end() {
		m_running = false;
	}

	public void tick(Time elapsedTime)  {
		m_elapsedTime.add(elapsedTime);

		if(m_elapsedTime.getSeconds() > 0.3) {
			synchronized (m_lock) {
				if(m_compressedData != null) {
					//Main.getNetworking().broadcast(new VideoPackage(m_compressedData));
					DebugHelper.log(" " + m_compressedData.length);
				}
			}

			m_elapsedTime.setNanoseconds(0);
		}
	}
}
