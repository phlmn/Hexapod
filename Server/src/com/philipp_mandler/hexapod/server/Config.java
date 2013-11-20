package com.philipp_mandler.hexapod.server;

import java.io.*;

public class Config implements Serializable {

	private static final long serialVersionUID = 2216687135772733840L;

	private String m_filepath;

	private double m_servoOffset[] = new double[20];

	private Config() {
		for(int i = 0; i < m_servoOffset.length; i++) {
			m_servoOffset[i] = 0;
		}
	}

	public void setServoOffset(int id, double value) {
		if(id <= m_servoOffset.length && id > 0) {
			m_servoOffset[id - 1] = value;
		}
	}

	public double getServoOffset(int id) {
		if(id <= m_servoOffset.length && id > 0) {
			return m_servoOffset[id - 1];
		}
		return 0;
	}

	public boolean save(){
		File file = new File(m_filepath);
		if(!file.exists()) {
			DebugHelper.log("Config file couldn't be found. Creating a new one at \"" + file.getAbsoluteFile() + "\"");
			try {
				file.createNewFile();
			} catch (IOException e) {
				DebugHelper.log(e.toString());
			}
		}

		if(!file.exists()) {
			DebugHelper.log("Config file couldn't be created.");
			return false;
		}
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			ObjectOutputStream objInputStream = new ObjectOutputStream(fileOutputStream);

			objInputStream.writeObject(this);

			objInputStream.close();
			fileOutputStream.close();

			return true;
		} catch (IOException e) {
			DebugHelper.log(e.toString());
			return false;
		}
	}

	public static Config load(String filepath) {
		File file = new File(filepath);
		if(file.exists()) {
			try {
				FileInputStream fileInputStream = new FileInputStream(file);
				ObjectInputStream objInputStream = new ObjectInputStream(fileInputStream);
				Config config = (Config)(objInputStream.readObject());
				objInputStream.close();
				fileInputStream.close();
				config.m_filepath = filepath;
				return config;
			} catch (IOException | ClassNotFoundException e) {
				DebugHelper.log(e.toString());
			}
		}
		else {
			DebugHelper.log("Config file couldn't be found. Creating a new one at \"" + file.getAbsoluteFile() + "\"");
			try {
				file.createNewFile();
			} catch (IOException e) {
				DebugHelper.log(e.toString());
			}
		}

		Config config = new Config();
		config.m_filepath = filepath;
		return config;
	}
}
