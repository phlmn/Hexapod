package com.philipp_mandler.hexapod.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Toast;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

public class MainActivity extends FragmentActivity implements NetworkingEventListener {
	
	static Networking m_networking = new Networking();
	Handler m_handler;
	ProgressDialog m_connectProgress;
	Dialog m_connectDialog;
	List<MainActivityListener> m_listeners = new ArrayList<MainActivityListener>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        m_handler = new Handler();
        m_networking.addEventListener(this);
        
        m_connectProgress = new ProgressDialog(this);
        m_connectProgress.setTitle(R.string.progress_connect_title);
        m_connectProgress.setMessage(getString(R.string.progress_connect_message));
        m_connectProgress.setCancelable(false);
        
        m_connectDialog = new Dialog(this);
        m_connectDialog.setContentView(R.layout.dialog_connect);
        m_connectDialog.setTitle(R.string.dialog_connect_title);
		m_connectDialog.findViewById(R.id.button_connect).setOnClickListener(new OnClickListener() {				
			public void onClick(View v) {
				connect(String.valueOf(((TextView)m_connectDialog.findViewById(R.id.text_address)).getText()));
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_connect:
    		if(m_networking.isConnected()) {
    			m_networking.disconnect();
    		}
    		else {	
    			m_connectDialog.show();
    		}
    		break;
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    	return true;
    }
    
    public void connect(String address) {
    	m_networking.connect(address, 8888);
    	m_connectProgress.show();
    }

	public void onDataReceived(NetPackage pack) {
		
	}
	
	public void onConnected() {
		m_connectProgress.hide();
		m_connectDialog.hide();
		invalidateOptionsMenu();
	}
	
	public void onDisconnected() {
		Toast.makeText(this, "Disconnected from Hexapod", Toast.LENGTH_SHORT).show();
		invalidateOptionsMenu();
	}

	public void onConnectionError() {
		Toast.makeText(this, "Could not connect to Hexapod", Toast.LENGTH_SHORT).show();
		m_connectProgress.hide();
	}
	
	public Handler getHandler() {
		return m_handler;
	}
	
	public static Networking getNetworking() {
		return m_networking;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if(m_networking.isConnected())
			menu.findItem(R.id.menu_connect).setTitle(R.string.menu_disconnect);
		else
			menu.findItem(R.id.menu_connect).setTitle(R.string.menu_connect);
		
		return super.onPrepareOptionsMenu(menu);
	}
	
	public void onRadioButtonClicked(View view) {
		for(MainActivityListener listener : m_listeners) {
			listener.onRadioButtonClicked(view);
		}
	}
	
	public void addListener(MainActivityListener listener) {
		m_listeners.add(listener);
	}
	
	public void removeListener(MainActivityListener listener) {
		m_listeners.remove(listener);
	}
    
}
