package com.philipp_mandler.hexapod.android.controller;

import android.content.Context;
import android.graphics.*;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.philipp_mandler.hexapod.hexapod.Vec2i;
import com.philipp_mandler.hexapod.hexapod.orientation.BooleanMap;
import com.philipp_mandler.hexapod.hexapod.orientation.BooleanMapManager;

import java.io.ByteArrayInputStream;

public class ObstacleView extends SurfaceView implements SurfaceHolder.Callback {

	private Thread m_thread;
	private boolean m_running;
	private final BooleanMapManager m_obstacleData = new BooleanMapManager();

	private byte[] m_videoData;

	private final Object m_lock = new Object();

	public ObstacleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ObstacleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ObstacleView(Context context) {
		super(context);
		init();
	}

	private void init() {
		getHolder().addCallback(this);
	}

	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);

		canvas.drawColor(Color.WHITE);

		Paint rectPaint = new Paint();
		rectPaint.setColor(Color.parseColor("#333333"));

		Vec2i offset = new Vec2i(600, -100);
		int size = 5;

		synchronized (m_obstacleData) {
			for(BooleanMap map : m_obstacleData.getBooleanMaps()) {
				for(int y = 0; y < 64; y++) {
					for(int x = 0; x < 64; x++) {
						if(map.getValue(new Vec2i(x, y))) {
							Vec2i globalPos = new Vec2i(64 * map.getOrigin().getX() + x, 64 * map.getOrigin().getY() + y);
							canvas.drawRect(size * globalPos.getX() + offset.getX(), size * globalPos.getY() + offset.getY(), size * globalPos.getX() + offset.getX() + size, size * globalPos.getY() + offset.getY() + size, rectPaint);
						}
					}
				}
			}
		}

		if(m_videoData != null) {
			synchronized (m_lock) {
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(m_videoData);

				Bitmap bitmap = BitmapFactory.decodeStream(byteArrayInputStream);

				if(bitmap != null) {
					Paint bitmapPaint = new Paint();
					canvas.drawBitmap(bitmap, (canvas.getWidth() - bitmap.getWidth()) / 2, 0, bitmapPaint);
				}
			}
		}

	}

	public BooleanMapManager getObstacleData() {
		return m_obstacleData;
	}

	public byte[] getVideoData() {
		return m_videoData;
	}

	public void setVideoData(byte[] data) {
		synchronized (m_lock) {
			m_videoData = data.clone();
		}
	}


	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		m_thread  = new Thread(new Runnable() {
			@Override
			public void run() {
				while(m_running) {
					Canvas canvas = getHolder().lockCanvas();
					draw(canvas);
					getHolder().unlockCanvasAndPost(canvas);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		m_running = true;
		m_thread.start();

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

		m_running = false;

	}
}
