package com.philipp_mandler.hexapod.hexapod;

import java.io.Serializable;

public class Vec3 implements Serializable {
	
	private static final long serialVersionUID = 862152504035620391L;
	
	private double x;
	private double y;
	private double z;
	
	public Vec3() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Vec3(Vec2 source, double z) {
		this.x = source.getX();
		this.y = source.getY();
		this.z = z;
	}
	
	public Vec3(Vec3 source) {
		this.x = source.getX();
		this.y = source.getY();
		this.z = source.getZ();
	}
	
	public void set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public void set(Vec3 source) {
		x = source.x;
		y = source.y;
		z = source.z;
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
	
	public void setZ(double z) {
		this.z = z;
	}
	
	public double getZ() {
		return z;
	}
	
	public Vec3 sum(Vec3 obj) {
		return new Vec3(x + obj.x, y + obj.y, z + obj.z);
	}
	
	public Vec3 divide(double number) {
		if(number == 0) return null;
		return new Vec3(x / number, y / number, z / number);
	}
}
