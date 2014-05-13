package com.philipp_mandler.hexapod.hexapod.net;

import com.philipp_mandler.hexapod.hexapod.ModuleStatus;

import java.util.ArrayList;

public class ModuleStatusPackage implements NetPackage {

	private static final long serialVersionUID = 6720819113449395070L;
	private ArrayList<ModuleStatus> m_modules = new ArrayList<>();

	public ModuleStatusPackage() {

	}

	public void addModule(String name, boolean running) {
		m_modules.add(new ModuleStatus(name, running));
	}

	public void removeModule(String name) {
		for(ModuleStatus module : m_modules) {
			if(module.getName().equals(name)) {
				m_modules.remove(module);
				break;
			}
		}
	}

	public void clearModules() {
		m_modules.clear();
	}

	public ArrayList<ModuleStatus> getModules() {
	 	return m_modules;
	}
}
