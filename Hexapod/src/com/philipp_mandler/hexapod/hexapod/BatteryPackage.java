package com.philipp_mandler.hexapod.hexapod;


public class BatteryPackage implements NetPackage {

	private static final long serialVersionUID = -1120937655979433804L;

	private double m_charge;

	public BatteryPackage(double charge) {
		m_charge = charge;
	}

	public void setCharge(double charge) {
		m_charge = charge;
	}

	public double getCharge() {
		return m_charge;
	}
}
