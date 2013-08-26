package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;

public class ModuleManager {

	private ArrayList<Module> m_modules = new ArrayList<>();
	private Time m_lastTime = new Time();

	public ModuleManager() {
		Thread m_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				Time tempTime = Time.fromNanoseconds(System.nanoTime());
				Time m_elapsedTime = Time.fromNanoseconds(tempTime.getNanoseconds() - m_lastTime.getNanoseconds());
				m_lastTime = tempTime;

				for(Module module : m_modules) {
					if(module.isRunning())
						module.tick(m_elapsedTime);
				}
			}
		});

		m_lastTime.setNanoseconds(System.nanoTime());

		m_thread.start();
	}

	public void registerModule(Module module) {
		for(Module tempModule : m_modules) {
			if(module.getName().equals(tempModule.getName())) {
				return;
			}
		}
		m_modules.add(module);
	}

	public void removeModule(String moduleName) {
		for(Module module : m_modules) {
			if(module.getName().equals(moduleName)) {
				m_modules.remove(module);
				return;
			}
		}
	}

	public void removeModule(Module module) {
		m_modules.remove(module);
	}

	public void startModule(Module module) {
		if(m_modules.contains(module) && !module.isRunning())
			module.start();
	}

	public boolean startModule(String moduleName) {
		for(Module module : m_modules) {
			if(module.getName().equals(moduleName) && !module.isRunning()) {
				if(!module.isRunning())
					module.start();
				return true;
			}
		}
		return false;
	}

	public void stopModule(Module module) {
		if(m_modules.contains(module) && module.isRunning())
			module.stop();
	}

	public boolean stopModule(String moduleName) {
		for(Module module : m_modules) {
			if(module.getName().equals(moduleName)) {
				if(module.isRunning())
					module.stop();
				return true;
			}
		}
		return false;
	}

	public Module getModule(String moduleName) {
		for(Module module : m_modules) {
			if(module.getName().equals(moduleName)) {
				return module;
			}
		}
		return null;
	}

}
