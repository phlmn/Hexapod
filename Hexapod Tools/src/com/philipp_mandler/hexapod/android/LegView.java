package com.philipp_mandler.hexapod.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.philipp_mandler.hexapod.hexapod.LegPositionPackage;
import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec3;

public class LegView extends SurfaceView implements SurfaceHolder.Callback {

	public static int dataKeyEvent = 99999;
	LegRenderer renderThread;
	
	public LegView(Context context) {
		super(context);
		init();
	}
	
	public LegView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public LegView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	private void init() {
		getHolder().addCallback(this);
		setFocusable(true);
		setFocusableInTouchMode(true);
		renderThread = new LegView.LegRenderer(getHolder());
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return renderThread.onTouchEvent(event);
	}
	
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		return renderThread.onGenericMotionEvent(event);
	}	

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		if(!renderThread.isAlive())
			renderThread = new LegView.LegRenderer(holder);
		renderThread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		renderThread.end();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(event.getAction() == LegView.dataKeyEvent) {
			renderThread.changeLeg(keyCode);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public class LegRenderer extends Thread {
		
		SurfaceHolder m_holder;
		
		long m_tick = 0;
		boolean m_run = true;
		
		float[] m_legPoints = {0, 0, 0, 0, 0, 0, 0, 0};
		float[] m_helpLinesX;
		float[] m_helpLinesY;
		
		Vec2[] m_legPositions = new Vec2[6];
		
		int m_leg = 1;
		
		Paint m_legPaint;
		Paint m_textPaint;
		Paint m_bodyPaint;
		Paint m_legJointPaint;
		Paint m_helpLinePaint;
		Paint m_legRadiusPaint;
		Paint m_legInnerRadiusPaint;
		
		Rect m_bodyRect;
		
		int m_bgColor;
		
		double m_upperLeg = Data.upperLeg;
		double m_lowerLeg = Data.lowerLeg;
		
		Vec2 m_gamepadPos;
		boolean gamepadInput = false;
		
		
		public LegRenderer(SurfaceHolder holder) {
			m_holder = holder;
		}
		
		@Override
		public void run() {
			init();
			while(m_run) {
				render();
				m_tick++;
			}
			dispose();
		}
		
		private void init() {
			setJoint(1, 100f, 300f);
			setJoint(3, relativeJointToAbsolute(new Vec2(m_lowerLeg, 0)));
			
			m_bgColor = Color.parseColor("#5dacef");
			
			m_legPaint = new Paint();
			m_legPaint.setColor(Color.WHITE);
			m_legPaint.setStrokeWidth(4);
			m_legPaint.setAntiAlias(true);
			m_legPaint.setStrokeCap(Cap.ROUND);
			
			m_helpLinePaint = new Paint();
			m_helpLinePaint.setColor(Color.parseColor("#92c6eb"));
			
			m_bodyPaint = new Paint();
			m_bodyPaint.setColor(Color.WHITE);
			m_bodyPaint.setAlpha(200);
			
			m_bodyRect = new Rect(0, 270, 100 , 330);
			
			m_textPaint = new Paint();
			m_textPaint.setColor(Color.WHITE);
			m_textPaint.setTextSize(12);
			m_textPaint.setAntiAlias(true);
			
			m_legJointPaint = new Paint();
			m_legJointPaint.setColor(Color.WHITE);
			m_legJointPaint.setAlpha(140);
			m_legJointPaint.setAntiAlias(true);
			m_legJointPaint.setStrokeWidth(20);
			m_legJointPaint.setStrokeCap(Cap.ROUND);
			
			m_legRadiusPaint = new Paint();
			m_legRadiusPaint.setColor(Color.parseColor("#6eb7f5"));
			m_legRadiusPaint.setAntiAlias(true);
			m_legRadiusPaint.setStrokeWidth((float) ((m_upperLeg + m_lowerLeg) * 2));
			m_legRadiusPaint.setStrokeCap(Cap.ROUND);
			
			m_legInnerRadiusPaint = new Paint();
			m_legInnerRadiusPaint.setColor(m_bgColor);
			m_legInnerRadiusPaint.setAntiAlias(true);
			m_legInnerRadiusPaint.setStrokeWidth((float) ((m_lowerLeg - m_upperLeg) * 2));
			m_legInnerRadiusPaint.setStrokeCap(Cap.ROUND);
			
			m_gamepadPos = new Vec2();			
			
			for(int i = 0; i < 6; i++) {
				m_legPositions[i] = new Vec2(m_lowerLeg, 0);
			}			
			
			m_helpLinesX = new float[(getWidth() / 25) + 4];
			for(int i = 1; i < (getWidth() / 100) + 1; i++) {
				m_helpLinesX[i * 4] = 100 * i;
				m_helpLinesX[i * 4 + 1] = 0;
				m_helpLinesX[i * 4 + 2] = 100 * i;
				m_helpLinesX[i * 4 + 3] = getHeight();
			}
			
			m_helpLinesY = new float[(getHeight() / 25) + 4];
			for(int i = 1; i < (getHeight() / 100) + 1; i++) {
				m_helpLinesY[i * 4] = 0;
				m_helpLinesY[i * 4 + 1] = 100 * i;
				m_helpLinesY[i * 4 + 2] = getWidth();
				m_helpLinesY[i * 4 + 3] = 100 * i;
			}
			
		}
		
		public void changeLeg(int leg) {
			m_legPositions[m_leg - 1].set(screenJointToRelative(getJoint(3)));
			m_leg = leg;
			setJoint(3, relativeJointToAbsolute(m_legPositions[m_leg - 1]));
		}
		
		private void render() {
					
			Canvas canvas = m_holder.lockCanvas();
			if(canvas == null) return;
			canvas.drawColor(m_bgColor);		
			
			canvas.drawPoint((float)getJoint(1).getX(), (float)getJoint(1).getY(), m_legRadiusPaint);
			canvas.drawPoint((float)getJoint(1).getX(), (float)getJoint(1).getY(), m_legInnerRadiusPaint);
			
			canvas.drawLines(m_helpLinesX, m_helpLinePaint);
			canvas.drawLines(m_helpLinesY, m_helpLinePaint);
			
			canvas.drawRect(m_bodyRect, m_bodyPaint);
			
			setJoint(3, new Vec2(getJoint(3).getX() + m_gamepadPos.getX(), getJoint(3).getY() + m_gamepadPos.getY()));
			
			double distance =  getStartGoalDistance(screenJointToRelative(getJoint(3)));
			
			double jointY = m_upperLeg * Math.cos( getS1Value(screenJointToRelative(getJoint(3)), distance) - (Math.PI /2) );
			double jointX = m_upperLeg * Math.sin( getS1Value(screenJointToRelative(getJoint(3)), distance) - (Math.PI /2) );
			
			setJoint(2, relativeJointToAbsolute( new Vec2(jointX, jointY ) ));
			
			canvas.drawPoint((float)getJoint(1).getX(), (float)getJoint(1).getY(), m_legJointPaint);
			canvas.drawPoint((float)getJoint(2).getX(), (float)getJoint(2).getY(), m_legJointPaint);
			canvas.drawPoint((float)getJoint(3).getX(), (float)getJoint(3).getY(), m_legJointPaint);
			
			canvas.drawLines(m_legPoints, m_legPaint);
			
			canvas.drawText("Tick: " + m_tick , 10, 20, m_textPaint);
			canvas.drawText(Math.round(Math.toDegrees(getS1Value(screenJointToRelative(getJoint(3)), distance)) * 100) / 100.0f + "°", (float)getJoint(1).getX() + 15, (float)getJoint(1).getY() + 15, m_textPaint);
			canvas.drawText(Math.round(Math.toDegrees(getS2Value(screenJointToRelative(getJoint(3)), distance)) * 100) / 100.0f + "°", (float)getJoint(2).getX() + 10, (float)getJoint(2).getY() - 10, m_textPaint);
			canvas.drawText(Math.round(distance * 100) / 100.0f + "mm", (float)getJoint(3).getX() + 10, (float)getJoint(3).getY() - 10, m_textPaint);
			
			m_holder.unlockCanvasAndPost(canvas);
			
		}
		
		private void dispose() {
			
		}
		
		protected void end() {
			m_run = false;
		}
		
		private void setJoint(int joint, float x, float y) {
			if(joint > 3 || joint < 1) return;
			if(joint == 1) {
				m_legPoints[0] = x;
				m_legPoints[1] = y;
			}
			else if(joint == 2) {
				m_legPoints[2] = x;
				m_legPoints[3] = y;
				m_legPoints[4] = x;
				m_legPoints[5] = y;
			}
			else if(joint == 3) {
				m_legPoints[6] = x;
				m_legPoints[7] = y;
			}
		}
		
		private void setJoint(int joint, Vec2 pos) {
			if(joint > 3 || joint < 1) return;
			if(joint == 1) {
				m_legPoints[0] = (float)pos.getX();
				m_legPoints[1] = (float)pos.getY();
			}
			else if(joint == 2) {
				m_legPoints[2] = (float)pos.getX();
				m_legPoints[3] = (float)pos.getY();
				m_legPoints[4] = (float)pos.getX();
				m_legPoints[5] = (float)pos.getY();
			}
			else if(joint == 3) {
				m_legPoints[6] = (float)pos.getX();
				m_legPoints[7] = (float)pos.getY();
			}
		}
		
		private Vec2 getJoint(int joint) {
			if(joint == 1) {
				return new Vec2(m_legPoints[0], m_legPoints[1]);
			}
			else if(joint == 2) {
				return new Vec2(m_legPoints[2], m_legPoints[3]);
			}
			else if(joint == 3) {
				return new Vec2(m_legPoints[6], m_legPoints[7]);
			}
			return null;
		}
		
		private Vec2 screenJointToRelative(Vec2 joint) {
			return new Vec2(joint.getX() - getJoint(1).getX(), getJoint(1).getY() - joint.getY());
		}
		
		private Vec2 relativeJointToAbsolute(Vec2 joint) {
			return new Vec2(getJoint(1).getX() + joint.getX(), getJoint(1).getY() - joint.getY());
		}
		
		public boolean onTouchEvent(MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
				setJoint(3, event.getX(), event.getY());
				if(m_gamepadPos.getLength() > 0)
					m_gamepadPos.set(0, 0);
				MainActivity.getNetworking().send(new LegPositionPackage(0, new Vec3(screenJointToRelative(getJoint(3)), 0)));
			}
			return true;
		}
		
		public boolean onGenericMotionEvent(MotionEvent event) {
			if(event.getAction() == MotionEvent.ACTION_MOVE) {
				if(m_gamepadPos != null) {
					m_gamepadPos.set(event.getX(), event.getY());
					if(m_gamepadPos.getLength() < 0.07) {
						m_gamepadPos.set(0,0);
					}
					m_gamepadPos.multiply(10);
				}
				MainActivity.getNetworking().send(new LegPositionPackage(0, new Vec3(screenJointToRelative(getJoint(3)), 0)));
			}
			return true;
		}
		
		public double getS1Value(Vec2 goal, double distance) {
			if(goal.getX() < 0) {
				if(goal.getY() < 0)
					return 2 * Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) + Math.asin( Math.abs(goal.getY()) / distance ) );
				
				return 2 * Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) - Math.asin( Math.abs(goal.getY()) / distance ) );
			}
			else {
				if(goal.getY() < 0)
					return Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) - Math.asin( Math.abs(goal.getY()) / distance ) );
				
				return Math.PI  - ( Math.acos( (Math.pow(distance, 2) + Math.pow(m_upperLeg, 2) - Math.pow(m_lowerLeg, 2)) / (2 * distance * m_upperLeg) ) + Math.asin( Math.abs(goal.getY()) / distance ) );
			}
		}
		
		public double getS2Value(Vec2 goal, double distance) {
			return Math.acos( (Math.pow(m_upperLeg, 2) + Math.pow(m_lowerLeg, 2) - Math.pow(distance, 2)) / (2 * m_upperLeg * m_lowerLeg) );
		}
		
		private double getStartGoalDistance(Vec2 goal) {
			return Math.sqrt( Math.pow(goal.getX(), 2) + Math.pow(goal.getY(), 2) );
		}
		
	}

}
