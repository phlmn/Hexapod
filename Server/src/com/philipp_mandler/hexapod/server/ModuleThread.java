package com.philipp_mandler.hexapod.server;

public class ModuleThread extends Thread {
	private Module m_module;
	private Main m_parent;
	
	public ModuleThread(Main parent, Module module) {
		m_module = module;
		m_parent = parent;	
	}
	
	@Override
	public void run() {
		DebugHelper.log("Module \"" + m_module.getName() + "\" started.");
		m_module.run();
		m_parent.unregsiterModule(m_module);
		DebugHelper.log("Module \"" + m_module.getName() + "\" stopped.");
	}
	
	public Module getModule() {
		return m_module;
	}
}
