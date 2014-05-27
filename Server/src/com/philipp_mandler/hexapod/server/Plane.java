package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec3;


public class Plane {

	private Vec3[] m_points = new Vec3[3];

	public Plane(Vec3 p1, Vec3 p2, Vec3 p3) {
		m_points[0] = p1;
		m_points[1] = p2;
		m_points[2] = p3;
	}

	public double getZ(Vec2 pos) {
		Vec3 o = new Vec3(m_points[0]);

		Vec3 p1 = m_points[1].sub(o);
		Vec3 p2 = m_points[2].sub(o);

		Vec3 norm = p1.crossP(p2);

		return (-norm.getX() * pos.getX() - norm.getY() * pos.getY()) / norm.getZ();
	}
}
