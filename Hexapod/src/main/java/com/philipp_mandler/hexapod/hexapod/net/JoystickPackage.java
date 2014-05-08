package com.philipp_mandler.hexapod.hexapod.net;

import com.philipp_mandler.hexapod.hexapod.JoystickType;
import com.philipp_mandler.hexapod.hexapod.Vec2;

public class JoystickPackage implements NetPackage {

	private static final long serialVersionUID = -3895220764273466433L;
	
	private Vec2 m_data;
	private JoystickType m_type;
	
	public JoystickPackage(Vec2 data, JoystickType type) {
		m_data = data;
		m_type = type;
	}
	
	public Vec2 getData() {
		return m_data;
	}
	
	public void setData(Vec2 data) {
		m_data = data;
	}
	
	public JoystickType getType() {
		return m_type;
	}
	
	public void setType(JoystickType type) {
		m_type = type;
	}

}
