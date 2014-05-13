package com.philipp_mandler.hexapod.android.controller;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class ConnectDialog extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.dialog_connect, container, false);
		final EditText text_address = (EditText)v.findViewById(R.id.edit_address);

		v.findViewById(R.id.button_connect).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				String address = text_address.getText().toString();
				if(address.isEmpty()) {
					address = "hexapod";
				}
				MainActivity.getNetworking().connect(address, 8888);
				dismiss();
			}
		});
		return v;
	}

}
