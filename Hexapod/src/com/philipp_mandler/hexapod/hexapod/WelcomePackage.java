package com.philipp_mandler.hexapod.hexapod;

public class WelcomePackage implements NetPackage {

	private static final long serialVersionUID = 3480135183540296655L;
	
	private DeviceType deviceType;
	
	public WelcomePackage(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
	public void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}
	
	public DeviceType getDeviceType() {
		return deviceType;
	}
}
