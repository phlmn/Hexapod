package com.philipp_mandler.hexapod.android.controller.Joystick;

import java.util.ArrayList;

import com.philipp_mandler.hexapod.android.controller.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
	
	private Bitmap m_knob;
	private Bitmap m_bg;
	
	private float m_touch_x = 130;
	private float m_touch_y = 130;
	
	private ArrayList<JoystickListener> m_listeners;

	public JoystickView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public JoystickView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public JoystickView(Context context) {
		super(context);
		init();		
	}
	
	private void init() {
		m_listeners = new ArrayList<JoystickListener>();
		
		m_knob = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_knob);
		m_bg = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_bg);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		canvas.save();
		
		canvas.drawBitmap(m_bg, canvas.getHeight() / 2 - m_bg.getHeight() / 2, canvas.getWidth() / 2 - m_bg.getWidth() / 2, null);
		canvas.drawBitmap(m_knob, m_touch_x - 90, m_touch_y - 90, null);
		
		canvas.restore();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		boolean accepted = false;
		
		if(event.getAction() == MotionEvent.ACTION_MOVE) {
			m_touch_x = event.getX();
			m_touch_y = event.getY();
			onPositionChanged();
			accepted = true;
		}
		else if(event.getAction() == MotionEvent.ACTION_DOWN) {
			if(Math.sqrt(Math.pow(event.getX() - 130, 2) + Math.pow(event.getY() - 130, 2)) <= 80) {
				m_touch_x = event.getX();
				m_touch_y = event.getY();
				onPositionChanged();
				accepted = true;
			}
		}
		else if(event.getAction() == MotionEvent.ACTION_UP) {
			m_touch_x = 130;
			m_touch_y = 130;
			onPositionChanged();
			accepted = true;
		}
		
		invalidate();
		
		return accepted;
	}
	
	private void onPositionChanged() {
		for(JoystickListener listener : m_listeners) {
			listener.joystickPositionChanged(this, (m_touch_x - 130) / 80f, -(m_touch_y - 130) / 80f);
		}
	}
	
	public void addListener(JoystickListener listener) {
		m_listeners.add(listener);
	}
	
	public void removeListener(JoystickListener listener) {
		m_listeners.remove(listener);
	}

}
