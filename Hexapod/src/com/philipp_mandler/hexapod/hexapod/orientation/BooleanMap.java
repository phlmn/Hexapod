package com.philipp_mandler.hexapod.hexapod.orientation;

import com.philipp_mandler.hexapod.hexapod.Vec2i;

import java.io.Serializable;

public class BooleanMap implements Serializable {

	private static final long serialVersionUID = 8205458160988181701L;

	private boolean m_booleanMap[][] = new boolean[64][64];
	private Vec2i m_origin = new Vec2i();

	public BooleanMap() {

	}

	public BooleanMap(Vec2i origin) {
		m_origin = origin;
	}

	public boolean[][] getBooleanMap() {
		return m_booleanMap;
	}

	public void clear() {
		m_booleanMap = new boolean[64][64];
	}

	public void setValue(Vec2i pos, boolean value) {
		m_booleanMap[pos.getX()][pos.getY()] = value;
	}

	public boolean getValue(Vec2i pos) {
		return m_booleanMap[pos.getX()][pos.getY()];
	}

	public void setOrigin(Vec2i origin) {
		m_origin = origin;
	}

	public Vec2i getOrigin() {
		return m_origin;
	}
}
