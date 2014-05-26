package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.net.VideoPackage;
import org.openkinect.freenect.FrameMode;
import org.openkinect.freenect.VideoHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoStreamer implements VideoHandler {

	private byte[] m_compressedData;

	private byte[] m_data = new byte[640 * 480 * 3];

	private Time m_elapsedTime = Time.fromNanoseconds(0);

	private AtomicBoolean m_send = new AtomicBoolean(false);


	public VideoStreamer() {

	}

	@Override
	public void onFrameReceived(FrameMode frameMode, ByteBuffer byteBuffer, int i) {
		if(m_send.get()) {
			try {
				byteBuffer.asReadOnlyBuffer().get(m_data);

				BufferedImage image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);

				int pos = 0;
				for(int y = 0; y < 480; y++) {
					for(int x = 0; x < 640; x++) {
						int color =  m_data[pos];
						color = color << 8;
						pos++;
						color += m_data[pos];
						pos++;
						color = color << 8;
						color += m_data[pos];
						pos++;
						image.setRGB(x, y, color);
					}
				}

				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


				ImageIO.write(image, "jpg", outputStream);

				m_compressedData = outputStream.toByteArray();

				Main.getNetworking().broadcast(new VideoPackage(m_compressedData));
			} catch (IOException e) {
				e.printStackTrace();
			}

			m_send.set(false);
		}
	}

	public void tick(Time elapsedTime) {
		m_elapsedTime.add(elapsedTime);
		if(m_elapsedTime.getSeconds() > 0.2) {
			m_send.set(true);
			m_elapsedTime.setNanoseconds(0);
		}
	}
}
