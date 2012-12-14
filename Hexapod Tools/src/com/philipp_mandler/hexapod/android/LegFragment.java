package com.philipp_mandler.hexapod.android;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;

public class LegFragment extends Fragment implements MainActivityListener {
	
	SurfaceView surfaceView;
	MainActivity parent;
	
	public LegFragment() {
		
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_leg, container, false);		
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		surfaceView = (SurfaceView)view.findViewById(R.id.legView);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if(activity instanceof MainActivity) {
			parent = (MainActivity)activity;
			parent.addListener(this);
		}
	}
	
	public void onRadioButtonClicked(View view) {
		if(!((RadioButton)view).isChecked())
			return;
		
		int leg = 0;
		switch(view.getId()) {
			case R.id.radio_leg1:
				leg = 1;
				break;
			case R.id.radio_leg2:
				leg = 2;
				break;
			case R.id.radio_leg3:
				leg = 3;
				break;
			case R.id.radio_leg4:
				leg = 4;
				break;
			case R.id.radio_leg5:
				leg = 5;
				break;
			case R.id.radio_leg6:
				leg = 6;
				break;
		}
		if(leg != 0)
			surfaceView.onKeyDown(leg, new KeyEvent(LegView.dataKeyEvent, leg));
	}
	
}
