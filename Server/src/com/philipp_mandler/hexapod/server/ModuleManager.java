package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.NetPackage;

import java.util.ArrayList;

public class ModuleManager implements NetworkingEventListener {

	private ArrayList<Module> m_modules = new ArrayList<>();
	private Time m_lastTime = new Time();
	private boolean m_running = true;

	public ModuleManager() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(m_running) {
					Time tempTime = Time.fromNanoseconds(System.nanoTime());
					Time m_elapsedTime = Time.fromNanoseconds(tempTime.getNanoseconds() - m_lastTime.getNanoseconds());
					//System.out.println("Tick took: " + m_elapsedTime.getMilliseconds() + " ms");
					m_lastTime = tempTime;

					for(Module module : m_modules) {
						if(module.isRunning())
							module.tick(m_elapsedTime);
					}
					try {
						Thread.sleep(0, 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		m_lastTime.setNanoseconds(System.nanoTime());

		thread.start();
	}

	public void stop() {
		m_running = false;
		for(Module module : m_modules) {
			if(module.isRunning())
				module.stop();
		}
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

	public final ArrayList<Module> getModules() {
		return m_modules;
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {
		for(Module module : m_modules) {
			if(module.isRunning()) {
				module.onDataReceived(client, pack);
			}
		}
	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {
		for(Module module : m_modules) {
			if(module.isRunning()) {
				module.onCmdReceived(client, cmd);
			}
		}
	}

	@Override
	public void onClientDisconnected(ClientWorker client) {
		for(Module module : m_modules) {
			if(module.isRunning()) {
				module.onClientDisconnected(client);
			}
		}
	}

	@Override
	public void onClientConnected(ClientWorker client) {
		for(Module module : m_modules) {
			if(module.isRunning()) {
				module.onClientConnected(client);
			}
		}
	}
}
