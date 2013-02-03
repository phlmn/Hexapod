package com.philipp_mandler.hexapod.android.controller;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.View;

import com.philipp_mandler.hexapod.android.controller.Joystick.JoystickListener;
import com.philipp_mandler.hexapod.android.controller.Joystick.JoystickView;
import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class MainActivity extends Activity implements NetworkingEventListener {
	
	JoystickView joystick1;
	JoystickView joystick2;
	
	static Networking m_networking = new Networking();
	
	Handler m_handler;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        joystick1 = (JoystickView)findViewById(R.id.joystickView1);
        joystick2 = (JoystickView)findViewById(R.id.joystickView2);
        
        joystick1.addListener(new JoystickListener() {
			
			public void joystickPositionChanged(View view, float x, float y) {
				
			}
		});
        
        joystick2.addListener(new JoystickListener() {
			
			public void joystickPositionChanged(View view, float x, float y) {
				
			}
		});
        
        m_handler = new Handler();        
        m_networking.addEventListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
	public void onDataReceived(NetPackage pack) {
		
	}
	
	@Override
	public void onConnected() {
		
	}

	@Override
	public void onDisconnected() {
		
	}

	@Override
	public void onConnectionError() {
		
	}

	public Handler getHandler() {
		return m_handler;
	}
}
