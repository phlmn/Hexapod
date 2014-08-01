package com.philipp_mandler.hexapod.hexapod;

import java.io.Serializable;

public class Vec3 implements Serializable {
	
	private static final long serialVersionUID = 862152504035620391L;
	
	private double m_x;
	private double m_y;
	private double m_z;
	
	public Vec3() {
		m_x = 0;
		m_y = 0;
		m_z = 0;
	}
	
	public Vec3(double x, double y, double z) {
		this.m_x = x;
		this.m_y = y;
		this.m_z = z;
	}
	
	public Vec3(Vec2 source, double z) {
		this.m_x = source.getX();
		this.m_y = source.getY();
		this.m_z = z;
	}
	
	public Vec3(Vec3 source) {
		this.m_x = source.getX();
		this.m_y = source.getY();
		this.m_z = source.getZ();
	}

	public Vec3(Vec2 source) {
		this.m_x = source.getX();
		this.m_y = source.getY();
	}
	
	public void set(double x, double y, double z) {
		this.m_x = x;
		this.m_y = y;
		this.m_z = z;
	}
	
	public void set(Vec3 source) {
		m_x = source.m_x;
		m_y = source.m_y;
		m_z = source.m_z;
	}
	
	public void setX(double x) {
		this.m_x = x;
	}
	
	public double getX() {
		return m_x;
	}
	
	public void setY(double y) {
		this.m_y = y;
	}
	
	public double getY() {
		return m_y;
	}
	
	public void setZ(double z) {
		this.m_z = z;
	}
	
	public double getZ() {
		return m_z;
	}
	
	public double getLength() {
		return Math.sqrt(m_x*m_x + m_y*m_y + m_z*m_z);
	}
	
	public Vec3 sum(Vec3 obj) {
		return new Vec3(m_x + obj.m_x, m_y + obj.m_y, m_z + obj.m_z);
	}
	
	public Vec3 divide(double number) {
		if(number == 0) return null;
		return new Vec3(m_x / number, m_y / number, m_z / number);
	}
	
	public void add(Vec3 obj) {
		m_x += obj.m_x;
		m_y += obj.m_y;
		m_z += obj.m_z;
	}

	public Vec3 sub(Vec3 obj) {
		return new Vec3(m_x - obj.m_x, m_y - obj.m_y, m_z - obj.m_z);
	}

	public void normalize() {
		if(getLength() != 0) {
			double length = getLength();
			m_x /= length;
			m_y /= length;
			m_z /= length;
		}
	}

	public void rotate(Vec3 angles) {

		// TODO: test

		double x;
		double y;
		double z;

		// z-axis

		double sinValue = Math.sin(angles.getZ());
		double cosValue = Math.cos(angles.getZ());

		x = m_x * cosValue - m_y * sinValue;
		y = m_x * sinValue + m_y * cosValue;

		m_x = x;
		m_y = y;

		// y-axis

		sinValue = Math.sin(angles.getY());
		cosValue = Math.cos(angles.getY());

		z = m_z * cosValue - m_x * sinValue;
		x = m_x * cosValue - m_z * sinValue ;

		m_z = z;
		m_x = x;


		// x-axis

		sinValue = Math.sin(angles.getX());
		cosValue = Math.cos(angles.getX());

		y = m_y * cosValue - m_z * sinValue;
		z = m_z * cosValue - m_y * sinValue;

		m_y = y;
		m_z = z;
	}

	public void rotate(Vec3 angles, Vec3 origin) {

		// TODO: Test

		double x;
		double y;
		double z;

		double moved_x = m_x - origin.getX();
		double moved_y = m_y - origin.getY();
		double moved_z = m_z - origin.getZ();

		// z-axis

		double sinValue = Math.sin(angles.getZ());
		double cosValue = Math.cos(angles.getZ());

		x = moved_x * cosValue - moved_y * sinValue;
		y = moved_x * sinValue + moved_y * cosValue;

		moved_x = x;
		moved_y = y;

		// y-axis

		sinValue = Math.sin(angles.getY());
		cosValue = Math.cos(angles.getY());

		z = moved_z * cosValue - moved_x * sinValue;
		x = moved_x * cosValue - moved_z * sinValue ;

		moved_z = z;
		moved_x = x;


		// x-axis

		sinValue = Math.sin(angles.getX());
		cosValue = Math.cos(angles.getX());

		y = moved_y * cosValue - moved_z * sinValue;
		z = m_z * cosValue - moved_y * sinValue;

		moved_y = y;
		moved_z = z;

		m_x = moved_x + origin.getX();
		m_y = moved_y + origin.getY();
		m_z = moved_z + origin.getZ();

	}

	public Vec3 crossP(Vec3 vec) {

		Vec3 result = new Vec3();


		result.setX( (m_y * vec.getZ()) - (m_z * vec.getY()) );
		result.setY( (m_z * vec.getX()) - (m_x * vec.getZ()) );
		result.setZ( (m_x * vec.getY()) - (m_y * vec.getX()) );

		return result;
	}

	public Vec3 pow(double a) {
		return new Vec3(Math.pow(m_x, a), Math.pow(m_y, a), Math.pow(m_z, a));
	}
}
