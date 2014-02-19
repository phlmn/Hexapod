package com.philipp_mandler.hexapod.android.controller;

import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.philipp_mandler.hexapod.hexapod.net.ConsolePackage;

import java.util.ArrayList;

public class ConsoleDialog extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		ArrayList<String> logList = MainActivity.getConsoleLog();
		View v = inflater.inflate(R.layout.dialog_console, container, false);

		LinearLayout logContainer = (LinearLayout)v.findViewById(R.id.consoleContainer);

		for(String log: logList) {
			TextView text = new TextView(getActivity().getApplicationContext());
			text.setText(log);
			text.setTextColor(Color.BLACK);
			logContainer.addView(text);
		}




		final EditText cmd = (EditText)v.findViewById(R.id.edit_cmd);

		v.findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(MainActivity.getNetworking().isConnected()) {
					MainActivity.getNetworking().send(new ConsolePackage(cmd.getText().toString()));
					MainActivity.getConsoleLog().add("> " + cmd.getText().toString());
					cmd.setText("");
				}
			}
		});

		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		final ScrollView scrollContainer = (ScrollView)view.findViewById(R.id.scrollView);

		scrollContainer.post(new Runnable() {
			@Override
			public void run() {
				scrollContainer.setScrollY(scrollContainer.getBottom());
			}
		});
	}
}
