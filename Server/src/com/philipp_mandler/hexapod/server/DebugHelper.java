package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.List;

import com.philipp_mandler.hexapod.hexapod.ConsolePackage;

public class DebugHelper {	
	static List<Log> loggs = new ArrayList<Log>();
	
	public static void log(String text) {
		DebugHelper.log(text, Log.INFO);
	}
	
	public static void log(String text, int level) {
		System.out.println(text);
		Log log = new Log(text, level);
		loggs.add(log);
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
