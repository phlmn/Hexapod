package com.philipp_mandler.hexapod.hexapod;


import java.io.Serializable;

public class Vec2i implements Serializable {
	private static final long serialVersionUID = -6154730666213981165L;
	private int m_x = 0;
	private int m_y = 0;


	public Vec2i() {

	}

	public Vec2i(int x, int y) {
		m_x = x;
		m_y = y;
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

	public boolean equals(Vec2i obj) {
		if(obj.m_x == m_x && obj.m_y == m_y)
			return true;
		return false;
	}

	public Vec2i sum(Vec2i obj) {
		return new Vec2i(m_x + obj.m_x, m_y + obj.m_y);
	}
}
