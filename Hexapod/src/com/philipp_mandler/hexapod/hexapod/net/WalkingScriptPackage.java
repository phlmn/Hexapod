package com.philipp_mandler.hexapod.hexapod.net;

public class WalkingScriptPackage implements NetPackage {

	private static final long serialVersionUID = 4957453157470164651L;
	
	private String m_script;
	
	public WalkingScriptPackage() {
		
	}
	
	public WalkingScriptPackage(String script) {
		m_script = script;
	}
	
	public String getScript() {
		return m_script;
	}
	
	public void setScript(String script) {
		m_script = script;
	}
	
}
