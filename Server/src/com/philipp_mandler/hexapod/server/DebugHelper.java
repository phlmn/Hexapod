package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;
import java.util.List;

public class DebugHelper {	
	static List<Log> loggs = new ArrayList<Log>();
	
	public static void log(String text) {
		DebugHelper.log(text, Log.INFO);
	}
	
	public static void log(String text, int level) {
		System.out.println(text);
		Log log = new Log(text, level);
		loggs.add(log);
	}
}
