package com.philipp_mandler.hexapod.hexapod.net;


public class VideoPackage implements NetPackage {

	private static final long serialVersionUID = -5123211213760114512L;
	private byte[] m_byteBuffer;

	public VideoPackage() {

	}

	public VideoPackage(byte[] byteBuffer) {
		m_byteBuffer = byteBuffer;
	}

	public void setByteBuffer(byte[] buffer) {
		m_byteBuffer = buffer;
	}

	public byte[] getByteBuffer() {
		return m_byteBuffer;
	}
}
