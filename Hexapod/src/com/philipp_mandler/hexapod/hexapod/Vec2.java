package com.philipp_mandler.hexapod.hexapod;

import java.io.Serializable;

public class Vec2 implements Serializable {
	
	private static final long serialVersionUID = 5292692273784938654L;
	
	private double x;
	private double y;
	
	public Vec2() {
		x = 0;
		y = 0;
	}
	
	public Vec2(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public Vec2(Vec2 obj) {
		x = obj.getX();
		y = obj.getY();
	}
	
	public double getLength() {
		return Math.sqrt(x*x + y*y);
	}
	
	public void set(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public void set(Vec2 source) {
		x = source.x;
		y = source.y;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public double getX() {
		return x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public double getY() {
		return y;
	}
	
	public void multiply(double factor) {
		x *= factor;
		y *= factor;
	}
	
	public void rotate(Vec2 center, double angle) {
		double sinValue = Math.sin(angle);
		double cosValue = Math.cos(angle);
		
		double ox = x - center.getX();
		double oy = y - center.getY();
		
		x = center.getX() + ox * cosValue + oy * sinValue;
		y = center.getY() - ox * sinValue + oy * cosValue;
	}
	
	public void rotate(double angle) {
		double sinValue = Math.sin(angle);
		double cosValue = Math.cos(angle);
		
		x = x * cosValue - y * sinValue;
		y = x * sinValue - y * cosValue;
	}
}
