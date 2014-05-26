package com.philipp_mandler.hexapod.hexapod;

import java.io.Serializable;

public class Vec2 implements Serializable {
	
	private static final long serialVersionUID = 5292692273784938654L;
	
	private double m_x;
	private double m_y;
	
	public Vec2() {
		m_x = 0;
		m_y = 0;
	}
	
	public Vec2(double x, double y) {
		this.m_x = x;
		this.m_y = y;
	}
	
	public Vec2(Vec2 obj) {
		m_x = obj.getX();
		m_y = obj.getY();
	}
	
	public double getLength() {
		return Math.sqrt(m_x*m_x + m_y*m_y);
	}
	
	public void set(double x, double y) {
		this.m_x = x;
		this.m_y = y;
	}
	
	public void set(Vec2 source) {
		m_x = source.m_x;
		m_y = source.m_y;
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
	
	public void multiply(double factor) {
		m_x *= factor;
		m_y *= factor;
	}
	
	public void divide(double divisor) {
		m_x /= divisor;
		m_y /= divisor;
	}
	
	public void rotate(Vec2 center, double angle) {
		double sinValue = Math.sin(angle);
		double cosValue = Math.cos(angle);
		
		double ox = m_x - center.getX();
		double oy = m_y - center.getY();
		
		m_x = center.getX() + ox * cosValue + oy * sinValue;
		m_y = center.getY() - ox * sinValue + oy * cosValue;
	}
	
	public void rotate(double angle) {
		double sinValue = Math.sin(angle);
		double cosValue = Math.cos(angle);
		
		m_x = m_x * cosValue - m_y * sinValue;
		m_y = m_x * sinValue - m_y * cosValue;
	}
	
	public void normalize() {
		if(getLength() != 0) {
			divide(getLength());
		}
	}

	public Vec2 sub(Vec2 obj) {
		return new Vec2(m_x - obj.m_x, m_y - obj.m_y);
	}

	public void add(Vec2 obj) {
		m_x += obj.m_x;
		m_y += obj.m_y;
	}

}
