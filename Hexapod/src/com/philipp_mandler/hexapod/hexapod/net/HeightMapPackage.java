package com.philipp_mandler.hexapod.hexapod.net;


import com.philipp_mandler.hexapod.hexapod.orientation.HeightMap;

public class HeightMapPackage implements NetPackage {

	private static final long serialVersionUID = -1787696855705293127L;

	private HeightMap m_map;

	public HeightMapPackage() {

	}

	public HeightMapPackage(HeightMap map) {
		m_map = map;
	}

	public void setMap(HeightMap map) {
		m_map = map;
	}

	public HeightMap getMap() {
		return m_map;
	}
}
