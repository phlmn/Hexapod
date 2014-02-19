package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.List;

import com.philipp_mandler.hexapod.hexapod.net.ConsolePackage;

public class DebugHelper {
	// storage for all log entries
	static List<Log> m_logs = new ArrayList<>();
	
	public static void log(String text) {
		// default logging
		DebugHelper.log(text, Log.INFO);
	}
	
	public static void log(String text, int level) {
		// create log with specified level
		System.out.println(text);
		Log log = new Log(text, level);
		m_logs.add(log);
		if(Main.getNetworking() != null) {
			if(level == Log.INFO) {
				Main.getNetworking().broadcast(new ConsolePackage(text));
			}
			else if(level == Log.ERROR) {
				Main.getNetworking().broadcast(new ConsolePackage("ERROR: " + text));
			}
			else {
				Main.getNetworking().broadcast(new ConsolePackage("WARNING: " + text));
			}
		}
	}
}
