package com.philipp_mandler.hexapod.hexapod.net;

import com.philipp_mandler.hexapod.hexapod.Vec3;

public class RotationPackage implements NetPackage {

	private static final long serialVersionUID = -4153521036909299977L;

	private Vec3 m_value;

	public RotationPackage() {
		m_value = new Vec3();
	}

	public RotationPackage(Vec3 value) {
		m_value = value;
	}

	public void setValue(Vec3 value) {
		m_value = value;
	}

	public Vec3 getValue() {
		return m_value;
	}
}
