package com.philipp_mandler.hexapod.hexapod;


import java.io.Serializable;

public class Vec3i implements Serializable{
	private static final long serialVersionUID = -6154730666213981165L;
	private int m_x = 0;
	private int m_y = 0;
	private int m_z = 0;


	public Vec3i() {

	}

	public Vec3i(int x, int y, int z) {
		m_x = x;
		m_y = y;
		m_z = z;
	}

	public void setX(int x) {
		m_x = x;
	}

	public int getX() {
		return m_x;
	}

	public void setY(int y) {
		m_y = y;
	}

	public int getY() {
		return m_y;
	}

	public void setZ(int z) {
		m_z = z;
	}

	public int getZ() {
		return m_z;
	}

	public boolean equals(Vec3i obj) {
		if(obj.m_x == m_x && obj.m_y == m_y && obj.m_z == m_z)
			return true;
		return false;
	}

	public Vec3i sum(Vec3i obj) {
		return new Vec3i(m_x + obj.m_x, m_y + obj.m_y, m_z + obj.m_z);
	}
}
