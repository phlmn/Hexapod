package com.philipp_mandler.hexapod.android.controller.Joystick;

import android.view.View;

public interface JoystickListener {
	
	public void joystickPositionChanged(View view, float x, float y);

}
