package com.philipp_mandler.hexapod.server;

import processing.core.PApplet;
import processing.opengl.PGL;

import javax.media.opengl.GL2;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class KinectDisplay extends PApplet {

	private ByteBuffer m_kinectData;

	@Override
	public void setup() {
		size(640, 480, P3D);
	}

	@Override
	public void draw() {
		background(255);

		GL2 gl = PGL.gl.getGL2();

		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		gl.glOrtho(0, 640, 480, 0, 0, 1);
		gl.glMatrixMode(GL2.GL_MODELVIEW);

		g.beginPGL();


		gl.glPointSize(1.5f);


		gl.glBegin(GL2.GL_POINTS);

		if(m_kinectData != null) {
			for(int y = 0; y < 480; y++) {
				for(int x = 0; x < 640; x++) {
					int pos = (y * 640 + x);
					int value = m_kinectData.getShort(pos * 2);
					if(value == 2047) {
						gl.glColor3d(1, 0, 0);
					}
					else {
						double color = (value - 500) / 2000.0;
						gl.glColor3d(color, color, color);
					}

					gl.glVertex2i(x, y);
				}
			}

			gl.glEnd();
		}
		g.endPGL();
	}

	public void setKinectData(ByteBuffer buffer) {
	 	m_kinectData = buffer;
		m_kinectData.order(ByteOrder.LITTLE_ENDIAN);
	}
}
