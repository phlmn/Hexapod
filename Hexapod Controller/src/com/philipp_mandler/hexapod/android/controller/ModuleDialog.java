package com.philipp_mandler.hexapod.android.controller;

import android.app.DialogFragment;
import android.os.Bundle;

public class ModuleDialog extends DialogFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);
	}

}
