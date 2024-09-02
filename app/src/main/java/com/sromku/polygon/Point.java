package com.sromku.polygon;

import java.util.Locale;

/**
 * Point on 2D landscape
 * 
 * @author Roman Kushnarenko (sromku@gmail.com)</br>
 */
public class Point
{
	public Point(float x, float y)
	{
		this.x = x;
		this.y = y;
	}

	public float x;
	public float y;

	@Override
	public String toString()
	{
		// FIXME - better Locale support needed
		return String.format(Locale.US, "(%.2f,%.2f)", x, y);
	}
}