package com.philipp_mandler.hexapod.android.controller;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.philipp_mandler.hexapod.hexapod.net.ConsolePackage;
import com.philipp_mandler.hexapod.hexapod.ModuleStatus;

import java.util.ArrayList;

public class ModuleDialog extends DialogFragment {

	private ArrayList<ModuleStatus> m_modules;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogTheme);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.dialog_modules, container, false);
		LinearLayout moduleContainer = (LinearLayout)v.findViewById(R.id.moduleContainer);
		if(m_modules != null) {
			for(final ModuleStatus module: m_modules) {
				View moduleEntry = inflater.inflate(R.layout.module_entry, null);
				TextView label = (TextView)moduleEntry.findViewById(R.id.textView);

				if(module.getRunning())
					label.setText(module.getName() + ": Running");
				else
					label.setText(module.getName() + ": Stopped");

				label.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if(module.getRunning())
							MainActivity.getNetworking().send(new ConsolePackage("module " + module.getName() + " stop"));
						else
							MainActivity.getNetworking().send(new ConsolePackage("module " + module.getName() + " start"));

						getDialog().dismiss();
					}
				});
				moduleContainer.addView(moduleEntry);
			}
		}
		return v;
	}

	public void setModules(ArrayList<ModuleStatus> modules) {
		m_modules = modules;
	}

}
