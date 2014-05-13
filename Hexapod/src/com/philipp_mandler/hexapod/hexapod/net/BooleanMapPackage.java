package com.philipp_mandler.hexapod.hexapod.net;


import com.philipp_mandler.hexapod.hexapod.orientation.BooleanMap;

public class BooleanMapPackage implements NetPackage {

	private static final long serialVersionUID = 1483225323189270483L;

	private BooleanMap m_map;

	public BooleanMapPackage() {

	}

	public BooleanMapPackage(BooleanMap map) {
		m_map = map;
	}

	public void setMap(BooleanMap map) {
		m_map = map;
	}

	public BooleanMap getMap() {
		return m_map;
	}
}
