package com.philipp_mandler.hexapod.hexapod.orientation;

import com.philipp_mandler.hexapod.hexapod.Vec2i;

import java.io.Serializable;

public class HeightMap implements Serializable {

	private static final long serialVersionUID = 8205458160988181701L;

	private Integer m_heightMap[][] = new Integer[64][64];
	private Vec2i m_origin = new Vec2i();

	public HeightMap() {

	}

	public HeightMap(Vec2i origin) {
		m_origin = origin;
	}

	public Integer[][] getHeightMap() {
		return m_heightMap;
	}

	public void clear() {
		m_heightMap = new Integer[64][64];
	}

	public void setHeight(Vec2i pos, Integer height) {
		m_heightMap[pos.getX()][pos.getY()] = height;
	}

	public Integer getHeight(Vec2i pos) {
		return m_heightMap[pos.getX()][pos.getY()];
	}

	public void setOrigin(Vec2i origin) {
		m_origin = origin;
	}

	public Vec2i getOrigin() {
		return m_origin;
	}
}
