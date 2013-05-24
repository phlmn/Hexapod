package com.philipp_mandler.hexapod.server;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class ServoController {

	public static int DX_BEGIN = 0xFF;
	public static int DX_BROADCAST_ID = 0xFE;

	public static int DX_LAST_ID = 0xFD;

	// return values
	public static int DX_RET_OK = 0;
	public static int DX_RET_ERROR_LEN = 1; // return package has wrong size
	public static int DX_RET_ERROR_START = 2; // can't find start
	public static int DX_RET_ERROR_CHECKSUM = 3; // can't find start

	// errors
	public static int DX_ERROR_INVOLT = 1 << 0;
	public static int DX_ERROR_ANGLELIMIT = 1 << 1;
	public static int DX_ERROR_OVERHEAT = 1 << 2;
	public static int DX_ERROR_RANGE = 1 << 3;
	public static int DX_ERROR_CHECKSUM = 1 << 4;
	public static int DX_ERROR_OVERLOAD = 1 << 5;
	public static int DX_ERROR_INST = 1 << 6;

	// Instructions
	public static int DX_INST_PING = 0x01;
	public static int DX_INST_READ_DATA = 0x02;
	public static int DX_INST_WRITE_DATA = 0x03;
	public static int DX_INST_REG_WRITE = 0x04;
	public static int DX_INST_ACTION = 0x05;
	public static int DX_INST_RESET = 0x06;
	public static int DX_INST_SYNC_WRITE = 0x83;

	// commands
	public static int DX_CMD_FIRMWARE = 0x02;
	public static int DX_CMD_BAUDRATE = 0x04;
	public static int DX_CMD_CW_ANGLE_LIMIT = 0x06;
	public static int DX_CMD_CCW_ANGLE_LIMIT = 0x08;
	public static int DX_CMD_TORQUE_ENABLE = 0x18;
	public static int DX_CMD_LED_ENABLE = 0x19;
	public static int DX_CMD_GOAL_POS = 0x1E;
	public static int DX_CMD_MOV_SPEED = 0x20;
	public static int DX_CMD_PRESENT_POS = 0x24;
	public static int DX_CMD_PRESENT_SPEED = 0x26;
	public static int DX_CMD_PRESENT_LOAD = 0x28;
	public static int DX_CMD_PRESENT_VOLT = 0x2A;
	public static int DX_CMD_PRESENT_TEMP = 0x2B;

	public static int DX_CMD_MAX_TORQUE = 0x0E;

	public static int DX_CMD_COMPLIANCE_MARGIN_CW = 0x1A;
	public static int DX_CMD_COMPLIANCE_MARGIN_CCW = 0x1B;

	public static int DX_CMD_COMPLIANCE_SLOPE_CW = 0x1C;
	public static int DX_CMD_COMPLIANCE_SLOPE_CCW = 0x1D;

	public static int DX_CMD_MOVING = 0x2E;
	public static int DX_CMD_PUNCH_LEFT = 0x30;
	public static int DX_CMD_PUNCH_RIGHT = 0x31;

	public static int DX_DIR_CCW = 0;
	public static int DX_DIR_CW = 1;

	class ReturnPacket {
		public ReturnPacket() {
			id = -1;
			length = 0;
			param = new ArrayList<Integer>();
		}

		public int checksum() {
			int ret = 0;
			ret += id;
			ret += length;
			ret += error;

			for (int i = 0; i < param.size(); i++)
				ret += param.get(i);

			return ServoController.calcChecksum(ret);
		}

		public String toString() {
			String retStr = "";
			retStr += "id: " + id + "\n";
			retStr += "length: " + length + "\n";
			retStr += "error: " + error + "\n";
			for (int i = 0; i < param.size(); i++)
				retStr += "param" + i + ": " + param.get(i).intValue() + "\n";

			return retStr;
		}

		public int id;
		public int length;
		public int error;
		public ArrayList<Integer> param;
	}

	private SerialPort m_serial;
	private InputStream m_serialInputStream;
	private OutputStream m_serialOutputStream;
	private int m_curChecksum;
	private int m_error;
	private int m_timeout = 40;
	private int m_delay = 2;
	private ReturnPacket m_returnPacket = new ReturnPacket();

	public ServoController() {

	}

	public void init(String serialDev, int baudRate) {
		try {
			m_serial = (SerialPort)CommPortIdentifier.getPortIdentifier(serialDev).open("Dynamixel", 5000);
			m_serial.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			m_serial.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			m_serialInputStream = m_serial.getInputStream();
			m_serialOutputStream = m_serial.getOutputStream();
		} catch (PortInUseException e) {
			System.out.println("Serial: Port already in use.");
		} catch (NoSuchPortException e) {
			System.out.println("Serial: No such port.");
		} catch (IOException e) {
			System.out.println("Serial: Could not get input or output stream.");
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			System.out.println("Serial: Could not set port parameters.");
			e.printStackTrace();
		}
	}

	public boolean setBaudrate(int id, int baudrate) {
		writeDataByte(id, DX_CMD_BAUDRATE, baudrate);

		// handle reply
		return handleReturnStatus(id);
	}

	public int baudrate(int id) {
		readData(id, DX_CMD_BAUDRATE, 1);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 1)
				return -1;
			return (m_returnPacket.param.get(0).intValue());
		} else
			return -1;
	}

	public boolean write(int data) {
		if (m_serialOutputStream == null)
			return false;
		try {
			m_serialOutputStream.write(data);
			return true;
		} catch (IOException e) {
			return false;
		}		
	}

	public int read() {
		if (m_serialInputStream == null)
			return 0;
		try {
			return m_serialInputStream.read();
		} catch (IOException e) {
		}
		return 0;
	}

	public boolean ping(int id) {
		m_curChecksum = 0;

		write(DX_BEGIN);
		write(DX_BEGIN);

		// id
		write(id);
		m_curChecksum += id;

		// length
		write(2);
		m_curChecksum += 2;

		// instruction
		write(DX_INST_PING);
		m_curChecksum += DX_INST_PING;

		// no param

		// checksum
		write(calcChecksum(m_curChecksum));

		// handle reply
		return handleReturnStatus(id);

	}

	public int[] pingAll() {
		ArrayList<Integer> servoList = new ArrayList<Integer>();
		for (int i = 0; i < DX_LAST_ID; i++) {
			if (ping(i))
				servoList.add(i);
		}

		int[] retArray = new int[servoList.size()];
		for (int i = 0; i < servoList.size(); i++)
			retArray[i] = servoList.get(i).intValue();

		return retArray;
	}

	public int[] pingRange(int start, int end) {
		if (start > DX_LAST_ID)
			start = DX_LAST_ID;
		if (end > DX_LAST_ID)
			end = DX_LAST_ID;

		ArrayList<Integer> servoList = new ArrayList<Integer>();
		for (int i = start; i < end; i++) {
			if (ping(i))
				servoList.add(i);
		}

		int[] retArray = new int[servoList.size()];
		for (int i = 0; i < servoList.size(); i++)
			retArray[i] = servoList.get(i).intValue();

		return retArray;
	}

	public boolean setAngleLimitCW(int id, int limit) {
		writeData2Bytes(id, DX_CMD_CW_ANGLE_LIMIT, limit);

		// handle reply
		return handleReturnStatus(id);
	}

	public int angleLimitCW(int id) {
		readData(id, DX_CMD_CW_ANGLE_LIMIT, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public boolean setAngleLimitCCW(int id, int limit) {
		writeData2Bytes(id, DX_CMD_CCW_ANGLE_LIMIT, limit);

		// handle reply
		return handleReturnStatus(id);
	}

	public int angleLimitCCW(int id) {
		readData(id, DX_CMD_CCW_ANGLE_LIMIT, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public boolean setMovingSpeed(int id, int speed) {
		writeData2Bytes(id, DX_CMD_MOV_SPEED, speed);

		// handle reply
		return handleReturnStatus(id);
	}

	public boolean setGoalPosition(int id, int pos) {
		writeData2Bytes(id, DX_CMD_GOAL_POS, pos);

		// handle reply
		return handleReturnStatus(id);
	}

	public int goalPosition(int id) {
		readData(id, DX_CMD_GOAL_POS, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			// System.out.println(_returnPacket.toString());
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public int presentPostition(int id) {
		readData(id, DX_CMD_PRESENT_POS, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			// System.out.println(_returnPacket.toString());
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public int presentSpeed(int id) {
		readData(id, DX_CMD_PRESENT_SPEED, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public int presentLoad(int id) {
		readData(id, DX_CMD_PRESENT_LOAD, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public int presentVolt(int id) {
		readData(id, DX_CMD_PRESENT_VOLT, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public int presentTemp(int id) {
		readData(id, DX_CMD_PRESENT_TEMP, 2);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 2)
				return -1;
			return ((m_returnPacket.param.get(1).intValue() << 8) + m_returnPacket.param
					.get(0).intValue());
		} else
			return -1;
	}

	public boolean moving(int id) {
		readData(id, DX_CMD_PRESENT_TEMP, 1);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 1)
				return false;
			return (m_returnPacket.param.get(0).intValue() > 0);
		} else
			return false;
	}

	public boolean setTorqueEnable(int id, boolean enable) {
		if (enable)
			writeDataByte(id, DX_CMD_TORQUE_ENABLE, 1);
		else
			writeDataByte(id, DX_CMD_TORQUE_ENABLE, 0);

		// handle reply
		return handleReturnStatus(id);
	}

	public boolean torqueEnable(int id) {
		readData(id, DX_CMD_TORQUE_ENABLE, 1);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 1)
				return false;
			return (m_returnPacket.param.get(0).intValue() > 0);
		} else
			return false;
	}

	public boolean setLed(int id, boolean enable) {
		if (enable)
			writeDataByte(id, DX_CMD_LED_ENABLE, 1);
		else
			writeDataByte(id, DX_CMD_LED_ENABLE, 0);

		// handle reply
		return handleReturnStatus(id);
	}

	public boolean led(int id) {
		readData(id, DX_CMD_LED_ENABLE, 1);
		if (handleReturnStatus(id)) {
			if (m_returnPacket.param.size() != 1)
				return false;
			return (m_returnPacket.param.get(0).intValue() > 0);
		} else
			return false;
	}

	protected boolean writeData2Bytes(int id, int addr, int data) {
		m_curChecksum = 0;

		write(DX_BEGIN);
		write(DX_BEGIN);

		// id
		write(id);
		m_curChecksum += id;

		// length
		write(2 + 3);
		m_curChecksum += 2 + 3; // 3 bytes param

		// instruction
		write(DX_INST_WRITE_DATA);
		m_curChecksum += DX_INST_WRITE_DATA;

		// param - addr
		write(addr);
		m_curChecksum += addr;
		// param - low byte
		write(data & 0x00FF);
		m_curChecksum += data & 0x00FF;
		// param - high byte
		write((data & 0xFF00) >> 8);
		m_curChecksum += (data & 0xFF00) >> 8;

		// checksum
		write(calcChecksum(m_curChecksum));

		return true;
	}

	protected boolean writeDataByte(int id, int addr, int data) {
		m_curChecksum = 0;

		write(DX_BEGIN);
		write(DX_BEGIN);

		// id
		write(id);
		m_curChecksum += id;

		// length
		write(2 + 2);
		m_curChecksum += 2 + 2; // 3 bytes param

		// instruction
		write(DX_INST_WRITE_DATA);
		m_curChecksum += DX_INST_WRITE_DATA;

		// param - addr
		write(addr);
		m_curChecksum += addr;
		// param - low byte
		write(data & 0x00FF);
		m_curChecksum += data & 0x00FF;

		// checksum
		write(calcChecksum(m_curChecksum));

		return true;
	}

	protected boolean readData(int id, int addr, int readLength) {
		m_curChecksum = 0;

		write(DX_BEGIN);
		write(DX_BEGIN);

		// id
		write(id);
		m_curChecksum += id;

		// length
		write(2 + 2);
		m_curChecksum += 2 + 2; // 2 bytes param

		// instruction
		write(DX_INST_READ_DATA);
		m_curChecksum += DX_INST_READ_DATA;

		// param - addr
		write(addr);
		m_curChecksum += addr;
		// param - read length
		write(readLength);
		m_curChecksum += readLength;

		// checksum
		write(calcChecksum(m_curChecksum));
		return true;

	}

	protected boolean handleReturnStatus(int id) {
		if (readStatus(m_returnPacket) == false)
			return false;

		m_error = m_returnPacket.error;
		if (m_returnPacket.id != id || m_error != 0)
			return false;
		else
			return true;
	}

	protected boolean readStatus(ReturnPacket returnPacket) {
		if (readStart(m_timeout) == false)
			return false;

		m_curChecksum = 0;

		// read id
		returnPacket.id = read();

		// read length
		returnPacket.length = read();

		// read error
		returnPacket.error = read();

		// read param
		returnPacket.param.clear();
		for (int i = 0; i < returnPacket.length - 2; i++)
			returnPacket.param.add(read());

		return (returnPacket.checksum() == read());
	}

	protected boolean readStart(int timeout) {
		m_error = 0;

		int origTimeout = timeout;
		int failCount = 30;

		timeout = origTimeout;
		while (failCount > 0) {
			timeout = origTimeout;
			if (waitForData(m_serial, timeout, m_delay, 1) == false)
				return false;

			if (read() == DX_BEGIN)
				break;
			else
				failCount--;
		}

		// second begin
		timeout = origTimeout;
		if (waitForData(m_serial, timeout, m_delay, 1) == false)
			return false;

		if (read() != DX_BEGIN)
			return false;

		// data
		timeout = origTimeout;
		if (waitForData(m_serial, timeout, m_delay, 4) == false)
			return false;

		return true;
	}

	public static boolean waitForData(SerialPort serial, int timeout,
			int delay, int dataCount) {
		try {
			while (serial.getInputStream().available() < dataCount
					&& (timeout--) >= 0) {
				if (locSleep(delay) == false)
					return false;
			}

			if (timeout < 0 && serial.getInputStream().available() < dataCount)
				return false;
			return true;

		} catch (IOException e) {
			return false;
		}
	}

	public static int calcChecksum(int checksumVal) {
		return (0xFF & ~checksumVal);
	}

	protected static boolean locSleep(int delay) {
		try {
			Thread.sleep(delay);
		} catch (InterruptedException excetpion) {
			System.out.println("InterruptedException: " + excetpion);
			return false;
		}
		return true;
	}

}
