package com.didithemouse.didicol;

public class Point {
	private float x;
	private float y;
	private float z;
	
	public Point(float _x, float _y, float _z)
	{
		this.x = _x;
		this.y = _y;
		this.z = _z;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}
}
