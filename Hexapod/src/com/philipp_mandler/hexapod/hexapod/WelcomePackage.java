package com.philipp_mandler.hexapod.hexapod;

public class WelcomePackage implements NetPackage {

	private static final long serialVersionUID = 3480135183540296655L;
	
	private DeviceType m_deviceType;
	
	public WelcomePackage(DeviceType deviceType) {
		this.m_deviceType = deviceType;
	}
	
	public void setDeviceType(DeviceType deviceType) {
		this.m_deviceType = deviceType;
	}
	
	public DeviceType getDeviceType() {
		return m_deviceType;
	}
}
