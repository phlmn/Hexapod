package com.philipp_mandler.hexapod.server;

import java.util.ArrayList;

class ReturnPacket {
	public int id;
	public int length;
	public int error;
	public ArrayList<Integer> param;

	public ReturnPacket() {
		id = -1;
		length = 0;
		param = new ArrayList<>();
	}

	public int checksum() {
		int ret = 0;
		ret += id;
		ret += length;
		ret += error;

		for (Integer aParam : param) ret += aParam;

		return ServoController.calcChecksum(ret);
	}

	public String toString() {
		String retStr = "";
		retStr += "id: " + id + "\n";
		retStr += "length: " + length + "\n";
		retStr += "error: " + error + "\n";
		for (int i = 0; i < param.size(); i++)
			retStr += "param" + i + ": " + param.get(i) + "\n";

		return retStr;
	}
}
