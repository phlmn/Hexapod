package com.philipp_mandler.hexapod.android.controller;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.philipp_mandler.hexapod.android.controller.Joystick.JoystickListener;
import com.philipp_mandler.hexapod.android.controller.Joystick.JoystickView;
import com.philipp_mandler.hexapod.hexapod.*;

import java.util.ArrayList;

public class MainActivity extends Activity implements NetworkingEventListener {
	
	private JoystickView joystick1;
	private JoystickView joystick2;
	
	private static Networking m_networking = new Networking();
	
	private Handler m_handler;

	private Menu m_optionsMenu;

	private static ArrayList<String> m_consoleLog = new ArrayList<>();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        joystick1 = (JoystickView)findViewById(R.id.joystickView1);
        joystick2 = (JoystickView)findViewById(R.id.joystickView2);
        
        joystick1.addListener(new JoystickListener() {
			
			public void joystickPositionChanged(View view, float x, float y) {
				if(m_networking.isConnected())
					m_networking.send(new JoystickPackage(new Vec2(x, y), JoystickType.Direction));
			}
		});
        
        joystick2.addListener(new JoystickListener() {
			
			public void joystickPositionChanged(View view, float x, float y) {
				if(m_networking.isConnected())
					m_networking.send(new JoystickPackage(new Vec2(x, y), JoystickType.Rotation));
			}
		});
        
        m_handler = new Handler(getMainLooper());
        m_networking.addEventListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		m_optionsMenu = menu;
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_connect:
				if(m_networking.isConnected())
					m_networking.disconnect();
				else
					new ConnectDialog().show(getFragmentManager(), "connect");
				break;
			case R.id.menu_modules: if(m_networking.isConnected()) m_networking.send(new ConsolePackage("modulestatus")); break;
			case R.id.menu_console: new ConsoleDialog().show(getFragmentManager(), "console"); break;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	@Override
	public void onDataReceived(NetPackage pack) {
		if(pack instanceof ConsolePackage) {
			final ConsolePackage conPack = (ConsolePackage)pack;

			final Context context = this.getApplicationContext();

			while(m_consoleLog.size() > 100) {
				m_consoleLog.remove(0);
			}
			m_consoleLog.add(conPack.getText());

			m_handler.post(new Runnable() {
				@Override
				public void run() {
					LinearLayout consoleContainer = (LinearLayout)findViewById(R.id.consoleView);
					ScrollView scrollView = (ScrollView)findViewById(R.id.scrollView);
					boolean autoScroll = false;
					if(scrollView.getHeight() - (scrollView.getScrollY() + consoleContainer.getHeight()) <= 40)
						autoScroll = true;

					TextView text = new TextView(context);
					text.setTextColor(Color.BLACK);
					text.setText(conPack.getText());
					consoleContainer.addView(text);
					if(autoScroll) {
						scrollView.setScrollY(consoleContainer.getHeight() - scrollView.getHeight());
					}
				}
			});
		}
		else if(pack instanceof ModuleStatusPackage) {
			ModuleDialog dialog = new ModuleDialog();
			dialog.show(getFragmentManager(), "modules");
			dialog.setModules(((ModuleStatusPackage) pack).getModules());
		}
	}
	
	@Override
	public void onConnected() {
		m_optionsMenu.findItem(R.id.menu_connect).setTitle(R.string.menu_disconnect);
	}

	@Override
	public void onDisconnected() {
		m_optionsMenu.findItem(R.id.menu_connect).setTitle(R.string.menu_connect);
	}

	@Override
	public void onConnectionError() {
		Toast.makeText(this, R.string.toast_connection_error, Toast.LENGTH_SHORT).show();
	}

	public Handler getHandler() {
		return m_handler;
	}

	public static Networking getNetworking() {
		return m_networking;
	}

	public static ArrayList<String> getConsoleLog() {
		return m_consoleLog;
	}

}
