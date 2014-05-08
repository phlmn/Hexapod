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

	public final static int DX_BEGIN = 0xFF;
	public final static int DX_BROADCAST_ID = 0xFE;

	public final static int DX_LAST_ID = 0xFD;

	// errors
	public final static int DX_ERROR_NO = 0;
	public final static int DX_ERROR_INVOLT = 1;
	public final static int DX_ERROR_ANGLELIMIT = 1 << 1;
	public final static int DX_ERROR_OVERHEAT = 1 << 2;
	public final static int DX_ERROR_RANGE = 1 << 3;
	public final static int DX_ERROR_CHECKSUM = 1 << 4;
	public final static int DX_ERROR_OVERLOAD = 1 << 5;
	public final static int DX_ERROR_INST = 1 << 6;

	public final static int DX_ERROR_USR_ID = 1 << 10;
	public final static int DX_ERROR_USR_READSTATUS = 1 << 11;
	public final static int DX_ERROR_USR_NO_BEGIN = 1 << 12;
	public final static int DX_ERROR_USR_DATA_TIMEOUT = 1 << 13;

	// Instructions
	public final static int DX_INST_PING = 0x01;
	public final static int DX_INST_READ_DATA = 0x02;
	public final static int DX_INST_WRITE_DATA = 0x03;
	public final static int DX_INST_REG_WRITE = 0x04;
	public final static int DX_INST_ACTION = 0x05;
	public final static int DX_INST_RESET = 0x06;
	public final static int DX_INST_SYNC_WRITE = 0x83;

	// commands
	public final static int DX_CMD_MODELNR = 0x00;
	public final static int DX_CMD_FIRMWARE = 0x02;
	public final static int DX_CMD_ID = 0x03;
	public final static int DX_CMD_BAUDRATE = 0x04;
	public final static int DX_CMD_DELAYTIME = 0x05;
	public final static int DX_CMD_CW_ANGLE_LIMIT = 0x06;
	public final static int DX_CMD_CCW_ANGLE_LIMIT = 0x08;
	public final static int DX_CMD_HIGH_LIMIT_TEMP = 0x0B;
	public final static int DX_CMD_LOW_LIMIT_VOLT = 0x0C;
	public final static int DX_CMD_HIGH_LIMIT_VOLT = 0x0D;
	public final static int DX_CMD_MAX_TORQUE = 0x0E;
	public final static int DX_CMD_STATUSRETURNLEVEL = 0x10;
	public final static int DX_CMD_ALARM_LED = 0x11;
	public final static int DX_CMD_ALARM_SHUTDOWN = 0x12;
	public final static int DX_CMD_TORQUE_ENABLE = 0x18;
	public final static int DX_CMD_LED_ENABLE = 0x19;
	public final static int DX_CMD_D_GAIN = 0x1A;
	public final static int DX_CMD_I_GAIN = 0x1B;
	public final static int DX_CMD_P_GAIN = 0x1C;
	public final static int DX_CMD_GOAL_POS = 0x1E;
	public final static int DX_CMD_MOV_SPEED = 0x20;
	public final static int DX_CMD_LIMIT_TORQUE = 0x22;
	public final static int DX_CMD_PRESENT_POS = 0x24;
	public final static int DX_CMD_PRESENT_SPEED = 0x26;
	public final static int DX_CMD_PRESENT_LOAD = 0x28;
	public final static int DX_CMD_PRESENT_VOLT = 0x2A;
	public final static int DX_CMD_PRESENT_TEMP = 0x2B;
	public final static int DX_CMD_REGISTER = 0x2C;
	public final static int DX_CMD_LOCK = 0x2F;


	public final static int DX_CMD_COMPLIANCE_MARGIN_CW = 0x1A;
	public final static int DX_CMD_COMPLIANCE_MARGIN_CCW = 0x1B;

	public final static int DX_CMD_COMPLIANCE_SLOPE_CW = 0x1C;
	public final static int DX_CMD_COMPLIANCE_SLOPE_CCW = 0x1D;

	public final static int DX_CMD_MOVING = 0x2E;
	public final static int DX_CMD_PUNCH = 0x30;

	public final static int DX_DIR_CCW = 0;
	public final static int DX_DIR_CW = 1;

	public final static int DX_MOTOR_SERIE_MX = 0;
	public final static int DX_MOTOR_SERIE_AX = 1;
	public final static int DX_MOTOR_SERIE_RX = 2;
	public final static int DX_MOTOR_SERIE_EX = 3;
	public final static int DX_MOTOR_SERIE_DX = 4;

	public final static int DX_TYPE_DX_113 = 0x0071;
	public final static int DX_TYPE_DX_116 = 0x0074;
	public final static int DX_TYPE_DX_117 = 0x0075;
	public final static int DX_TYPE_AX_12W = 0x012C;
	public final static int DX_TYPE_AX_12 = 0x000C;
	public final static int DX_TYPE_AX_18 = 0x0012;
	public final static int DX_TYPE_RX_10 = 0x000A;
	public final static int DX_TYPE_RX_24F = 0x00184;
	public final static int DX_TYPE_RX_28 = 0x001C;
	public final static int DX_TYPE_RX_64 = 0x0040;
	public final static int DX_TYPE_EX_104 = 0x006B;
	public final static int DX_TYPE_MX_28 = 0x001D;
	public final static int DX_TYPE_MX_64 = 0x0136;
	public final static int DX_TYPE_MX_106 = 0x0140;


	protected boolean m_regWriteFlag = false;


	protected final Object m_lock = new Object();

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

	private SerialPort m_serial;
	private InputStream m_serialInputStream;
	private OutputStream m_serialOutputStream;
	private int m_curChecksum;
	private int m_error;
	private int m_timeout = 40;
	private int m_delay = 2;
	private ReturnPacket m_returnPacket = new ReturnPacket();
	private boolean m_initialized = false;

	public ServoController() {

	}

	public void init(String serialDev, int baudRate) throws PortInUseException, UnsupportedCommOperationException, NoSuchPortException, IOException {
		try {
			m_serial = (SerialPort) CommPortIdentifier.getPortIdentifier(serialDev).open("Dynamixel", 5000);
			m_serial.setSerialPortParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			m_serial.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			m_serialInputStream = m_serial.getInputStream();
			m_serialOutputStream = m_serial.getOutputStream();
			m_initialized = true;
		} catch (PortInUseException e) {
			System.out.println("Serial: Port already in use.");
			throw e;
		} catch (NoSuchPortException e) {
			System.out.println("Serial: No such port.");
			throw e;
		} catch (IOException e) {
			System.out.println("Serial: Could not get input or output stream.");
			throw e;
		} catch (UnsupportedCommOperationException e) {
			System.out.println("Serial: Could not set port parameters.");
			throw e;
		}
	}

	public boolean initialized() {
		return m_initialized;
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


	public int error() {
		return m_error;
	}

	public String errorStr() {
		return errorStr(m_error);
	}

	public SerialPort serial() {
		return m_serial;
	}


	public final static int getMotorSerie(int modelNr) {
		switch (modelNr) {
			// dx
			case DX_TYPE_DX_113:
			case DX_TYPE_DX_116:
			case DX_TYPE_DX_117:
				return DX_MOTOR_SERIE_DX;
			// ax
			case DX_TYPE_AX_12W:
			case DX_TYPE_AX_12:
			case DX_TYPE_AX_18:
				return DX_MOTOR_SERIE_AX;
			// rx
			case DX_TYPE_RX_10:
			case DX_TYPE_RX_24F:
			case DX_TYPE_RX_28:
			case DX_TYPE_RX_64:
				return DX_MOTOR_SERIE_RX;
			// ex
			case DX_TYPE_EX_104:
				return DX_MOTOR_SERIE_EX;
			// mx
			case DX_TYPE_MX_28:
			case DX_TYPE_MX_64:
			case DX_TYPE_MX_106:
				return DX_MOTOR_SERIE_MX;
			default:
				return -1;
		}
	}

	public static int getMotorRange(int modelNr) {
		switch (modelNr) {
			// dx
			case DX_TYPE_DX_113:
			case DX_TYPE_DX_116:
			case DX_TYPE_DX_117:
				return 0x3FF;
			// ax
			case DX_TYPE_AX_12W:
			case DX_TYPE_AX_12:
			case DX_TYPE_AX_18:
				return 0x3FF;
			// rx
			case DX_TYPE_RX_10:
			case DX_TYPE_RX_24F:
			case DX_TYPE_RX_28:
			case DX_TYPE_RX_64:
				return 0x3FF;
			// ex
			case DX_TYPE_EX_104:
				return 0xFFF;
			// mx
			case DX_TYPE_MX_28:
			case DX_TYPE_MX_64:
			case DX_TYPE_MX_106:
				return 0xFFF;
			default:
				return 0;
		}
	}

	public static float getMotorDeadAngle(int modelNr) {
		switch (modelNr) {
			// dx
			case DX_TYPE_DX_113:
			case DX_TYPE_DX_116:
			case DX_TYPE_DX_117:
				return 360.0f - 300.0f;
			// ax
			case DX_TYPE_AX_12W:
			case DX_TYPE_AX_12:
			case DX_TYPE_AX_18:
				return 360.0f - 300.0f;
			// rx
			case DX_TYPE_RX_10:
			case DX_TYPE_RX_24F:
			case DX_TYPE_RX_28:
			case DX_TYPE_RX_64:
				return 360.0f - 300.0f;
			// ex
			case DX_TYPE_EX_104:
				return 360.0f - 251.0f;
			// mx
			case DX_TYPE_MX_28:
			case DX_TYPE_MX_64:
			case DX_TYPE_MX_106:
				return 0.0f;
			default:
				return 0.0f;
		}
	}

	public int modelNr(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_MODELNR, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public int firmware(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_FIRMWARE, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setId(int id, int newId) {
		synchronized (m_lock) {
			if (newId >= DX_BROADCAST_ID)
				return false;

			writeDataByte(id, DX_CMD_ID, newId);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public boolean setBaudrate(int id, int baudrate) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_BAUDRATE, baudrate);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int baudrate(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_BAUDRATE, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public boolean setDelayTime(int id, int delayTime) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_DELAYTIME, delayTime);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int delayTime(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_DELAYTIME, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setHighLimitTemp(int id, int limitTemp) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_HIGH_LIMIT_TEMP, limitTemp);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int highLimitTemp(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_HIGH_LIMIT_TEMP, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setLowLimitVolt(int id, int limitVolt) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_LOW_LIMIT_VOLT, limitVolt);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int lowLimitVolt(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_LOW_LIMIT_VOLT, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setHightLimitVolt(int id, int limitVolt) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_HIGH_LIMIT_VOLT, limitVolt);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int highLimitVolt(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_HIGH_LIMIT_VOLT, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setMaxTorque(int id, int maxTorque) {
		synchronized (m_lock) {
			writeData2Bytes(id, DX_CMD_MAX_TORQUE, maxTorque, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int maxTorque(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_MAX_TORQUE, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return (m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public boolean setStatusReturnLevel(int id, int statusReturnLevel) {
		synchronized (m_lock) {
			if (statusReturnLevel >= 3)
				return false;
			writeDataByte(id, DX_CMD_STATUSRETURNLEVEL, statusReturnLevel);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int statusReturnLevel(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_STATUSRETURNLEVEL, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setAlarmLed(int id, int alarmLed) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_ALARM_LED, alarmLed);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int alarmLed(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_ALARM_LED, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setAlarmShutdown(int id, int alarmShutdown) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_ALARM_SHUTDOWN, alarmShutdown);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int alarmShutdown(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_ALARM_SHUTDOWN, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return (m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	/*
		public synchronized boolean ping(int id,int timeout)
			{

			}
	*/
	// watch out, this resets the servo to the default factory settings
	public boolean reset(int id) {
		synchronized (m_lock) {

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
			write(DX_INST_ACTION);
			m_curChecksum += DX_INST_ACTION;

			// no param

			// checksum
			write(calcChecksum(m_curChecksum));

			// handle reply

			return handleReturnStatus(id);
		}
	}

	public synchronized boolean ping(int id) {
		synchronized (m_lock) {

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
			int oldTimeout = m_timeout;
			m_timeout = 80;
			boolean ret = handleReturnStatus(id);
			m_timeout = oldTimeout;

			return ret;
		}
	}

	public synchronized int[] pingAll() {
		ArrayList<Integer> servoList = new ArrayList<>();
		for (int i = 0; i < DX_LAST_ID; i++) {
			if (ping(i))
				servoList.add(i);
		}

		int[] retArray = new int[servoList.size()];
		for (int i = 0; i < servoList.size(); i++)
			retArray[i] = servoList.get(i);

		return retArray;
	}

	public synchronized int[] pingRange(int start, int end) {
		if (start > DX_LAST_ID)
			start = DX_LAST_ID;
		if (end > DX_LAST_ID)
			end = DX_LAST_ID;

		ArrayList<Integer> servoList = new ArrayList<Integer>();
		for (int i = start; i <= end; i++) {
			if (ping(i))
				servoList.add(i);
		}

		int[] retArray = new int[servoList.size()];
		for (int i = 0; i < servoList.size(); i++)
			retArray[i] = servoList.get(i);

		return retArray;
	}


	public synchronized boolean action(int id) {
		synchronized (m_lock) {
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
			write(DX_INST_ACTION);
			m_curChecksum += DX_INST_ACTION;

			// no param

			// checksum
			write(calcChecksum(m_curChecksum));

			if (id != DX_BROADCAST_ID)
				// handle reply
				return handleReturnStatus(id);
			else
				return true;
		}
	}

	public synchronized void beginRegWrite() {
		if (m_regWriteFlag)
			return;

		m_regWriteFlag = true;
	}

	public synchronized boolean endRegWrite() {
		if (!m_regWriteFlag)
			return false;

		m_regWriteFlag = false;
		// activate the commands
		return action(DX_BROADCAST_ID);
	}


	public boolean syncWrite(int address, int length, int[] idList, int[][] dataList) {
		synchronized (m_lock) {
			int curChecksum = 0;

			write(DX_BEGIN);
			write(DX_BEGIN);

			// id
			write(DX_BROADCAST_ID);
			curChecksum += DX_BROADCAST_ID;

			// length
			int l = (length + 1) * idList.length + 4;

			write(l);
			curChecksum += l;  // 3 bytes param

			// instruction
			write(DX_INST_SYNC_WRITE);
			curChecksum += DX_INST_SYNC_WRITE;

			// param - address
			write(address);
			curChecksum += address;

			// param - length
			write(length);
			curChecksum += length;

			// write data
			for (int i = 0; i < idList.length; i++) {
				// param - id
				write(idList[i]);
				curChecksum += idList[i];

				for (int j = 0; j < length; j++) {
					// param - data
					write(dataList[i][j]);
					curChecksum += dataList[i][j];
				}
			}

			// checksum
			write(calcChecksum(curChecksum));

			// no return because of broadcast sending
			return false;
		}
	}

	public boolean syncWriteGoalPosition(int[] idList, int[] posList) {
		int[][] dataList = new int[idList.length][2];
		for (int i = 0; i < idList.length; i++) {
			dataList[i] = new int[2];
			dataList[i][0] = posList[i] & 0x00FF;
			dataList[i][1] = (posList[i] & 0xFF00) >> 8;
		}

		return syncWrite(DX_CMD_GOAL_POS, 2, idList, dataList);
	}

	public boolean syncWriteMovingSpeed(int[] idList, int[] speedList) {
		int[][] dataList = new int[idList.length][2];
		for (int i = 0; i < idList.length; i++) {
			dataList[i] = new int[2];
			dataList[i][0] = speedList[i] & 0x00FF;
			dataList[i][1] = (speedList[i] & 0xFF00) >> 8;
		}

		return syncWrite(DX_CMD_MOV_SPEED, 2, idList, dataList);
	}

	public boolean syncWriteMovingSpeed(int[] idList, int speed) {
		int[][] dataList = new int[idList.length][2];
		for (int i = 0; i < idList.length; i++) {
			dataList[i] = new int[2];
			dataList[i][0] = speed & 0x00FF;
			dataList[i][1] = (speed & 0xFF00) >> 8;
		}

		return syncWrite(DX_CMD_MOV_SPEED, 2, idList, dataList);
	}

	public boolean syncWriteTorqueEnable(int[] idList, boolean[] torqueList) {
		int[][] dataList = new int[idList.length][1];
		for (int i = 0; i < idList.length; i++) {
			dataList[i] = new int[1];
			dataList[i][0] = torqueList[i] ? 1 : 0;
		}

		return syncWrite(DX_CMD_TORQUE_ENABLE, 1, idList, dataList);
	}

	public boolean syncWriteTorqueEnable(int[] idList, boolean torque) {
		int[][] dataList = new int[idList.length][1];
		for (int i = 0; i < idList.length; i++) {
			dataList[i] = new int[1];
			dataList[i][0] = torque ? 1 : 0;
		}

		return syncWrite(DX_CMD_TORQUE_ENABLE, 1, idList, dataList);
	}

	public boolean setWheelMode(int id, boolean enable, int modelNr) {
		if (enable) {   // on
			return setAngleLimitCW(id, 0) &&
					setAngleLimitCCW(id, 0);
		} else {   // off
			return setAngleLimitCW(id, 0) &&
					setAngleLimitCCW(id, getMotorRange(modelNr));
			/*
            // mx etc...
            setAngleLimitCCW(id,0xFFF);
            // ax etc...
            setAngleLimitCCW(id,0x3FF);
            */
		}
	}

	public boolean setAngleLimitCW(int id, int limit) {
		synchronized (m_lock) {
			writeData2Bytes(id, DX_CMD_CW_ANGLE_LIMIT, limit, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int angleLimitCW(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_CW_ANGLE_LIMIT, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setAngleLimitCCW(int id, int limit) {
		synchronized (m_lock) {
			writeData2Bytes(id, DX_CMD_CCW_ANGLE_LIMIT, limit, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int angleLimitCCW(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_CCW_ANGLE_LIMIT, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setMovingSpeed(int id, int speed) {
		synchronized (m_lock) {
			writeData2Bytes(id, DX_CMD_MOV_SPEED, speed, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int movingSpeed(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_MOV_SPEED, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean setTorqueLimit(int id, int torqueLimit) {
		synchronized (m_lock) {
			writeData2Bytes(id, DX_CMD_LIMIT_TORQUE, torqueLimit, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int torqueLimit(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_LIMIT_TORQUE, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	// mx commands
	public boolean setDGain(int id, int gain) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_D_GAIN, gain, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int dGain(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_D_GAIN, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public boolean setIGain(int id, int gain) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_I_GAIN, gain, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int iGain(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_I_GAIN, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public boolean setPGain(int id, int gain) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_P_GAIN, gain, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int pGain(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_P_GAIN, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	// ax commands
	public boolean setComplianceMarginCW(int id, int value) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_COMPLIANCE_MARGIN_CW, value, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int complianceMarginCW(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_COMPLIANCE_MARGIN_CW, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public boolean setComplianceMarginCCW(int id, int value) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_COMPLIANCE_MARGIN_CCW, value, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int complianceMarginCCW(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_COMPLIANCE_MARGIN_CCW, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public boolean setComplianceSlopeCW(int id, int value) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_COMPLIANCE_SLOPE_CW, value, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int complianceSlopeCW(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_COMPLIANCE_SLOPE_CW, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public boolean setComplianceSlopeCCW(int id, int value) {
		synchronized (m_lock) {
			writeDataByte(id, DX_CMD_COMPLIANCE_SLOPE_CCW, value, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int complianceSlopeCCW(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_COMPLIANCE_SLOPE_CCW, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}


	public boolean setGoalPosition(int id, int pos) {
		synchronized (m_lock) {
			writeData2Bytes(id, DX_CMD_GOAL_POS, pos, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int goalPosition(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_GOAL_POS, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return (m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0);
			} else
				return -1;
		}
	}

	public int presentPosition(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_PRESENT_POS, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2) {
					return -1;
				}
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else {
				return -1;
			}
		}
	}

	public int presentSpeed(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_PRESENT_SPEED, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public int presentLoad(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_PRESENT_LOAD, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public double presentVolt(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_PRESENT_VOLT, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return -1;
				return m_returnPacket.param.get(0) / 10.0;
			} else
				return -1;
		}
	}

	public int presentTemp(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_PRESENT_TEMP, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}

	public boolean register(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_REGISTER, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return false;
				return (m_returnPacket.param.get(0) > 0);
			} else
				return false;
		}
	}


	public boolean moving(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_MOVING, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return false;
				return (m_returnPacket.param.get(0) > 0);
			} else
				return false;
		}
	}


	public boolean setTorqueEnable(int id, boolean enable) {
		synchronized (m_lock) {
			if (enable)
				writeDataByte(id, DX_CMD_TORQUE_ENABLE, 1, m_regWriteFlag);
			else
				writeDataByte(id, DX_CMD_TORQUE_ENABLE, 0, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public boolean torqueEnable(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_TORQUE_ENABLE, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return false;
				return (m_returnPacket.param.get(0) > 0);
			} else
				return false;
		}
	}

	public boolean setLock(int id, boolean enable) {
		synchronized (m_lock) {
			if (enable)
				writeDataByte(id, DX_CMD_LOCK, 1, m_regWriteFlag);
			else
				writeDataByte(id, DX_CMD_LOCK, 0, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public boolean lock(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_LOCK, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return false;
				return (m_returnPacket.param.get(0) > 0);
			} else
				return false;
		}
	}

	public boolean setLed(int id, boolean enable) {
		synchronized (m_lock) {
			if (enable)
				writeDataByte(id, DX_CMD_LED_ENABLE, 1, m_regWriteFlag);
			else
				writeDataByte(id, DX_CMD_LED_ENABLE, 0, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public boolean led(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_LED_ENABLE, 1);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 1)
					return false;
				return m_returnPacket.param.get(0) > 0;
			} else
				return false;
		}
	}


	public boolean setPunch(int id, int punch) {
		synchronized (m_lock) {
			writeData2Bytes(id, DX_CMD_PUNCH, punch, m_regWriteFlag);

			// handle reply
			return handleReturnStatus(id);
		}
	}

	public int punch(int id) {
		synchronized (m_lock) {
			readData(id, DX_CMD_PUNCH, 2);
			if (handleReturnStatus(id)) {
				if (m_returnPacket.param.size() != 2)
					return -1;
				return ((m_returnPacket.param.get(1) << 8) + m_returnPacket.param.get(0));
			} else
				return -1;
		}
	}


	protected synchronized boolean writeData2Bytes(int id, int addr, int data) {
		return writeData2Bytes(id, addr, data, false);
	}

	protected synchronized boolean writeData2Bytes(int id, int addr, int data, boolean regWrite) {
//		if(_serialType == DX_SERIALTYPE_SYNC)
//			// block next few bytes for receiving
//			_serial.addReadBlockCount(9);

		m_curChecksum = 0;

		write(DX_BEGIN);
		write(DX_BEGIN);

		// id
		write(id);
		m_curChecksum += id;

		// length
		write(2 + 3);
		m_curChecksum += 2 + 3;  // 3 bytes param

		// instruction
		if (regWrite) {
			write(DX_INST_REG_WRITE);
			m_curChecksum += DX_INST_REG_WRITE;
		} else {
			write(DX_INST_WRITE_DATA);
			m_curChecksum += DX_INST_WRITE_DATA;
		}

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

	protected synchronized boolean writeDataByte(int id, int addr, int data) {
		return writeDataByte(id, addr, data, false);
	}

	protected synchronized boolean writeDataByte(int id, int addr, int data, boolean regWrite) {
//		if(m_serialType == DX_SERIALTYPE_SYNC)
//			// block next few bytes for receiving
//			_serial.addReadBlockCount(8);

		m_curChecksum = 0;

		write(DX_BEGIN);
		write(DX_BEGIN);

		// id
		write(id);
		m_curChecksum += id;

		// length
		write(1 + 3);
		m_curChecksum += 1 + 3;  // 3 bytes param

		// instruction
		if (regWrite) {
			write(DX_INST_REG_WRITE);
			m_curChecksum += DX_INST_REG_WRITE;
		} else {
			write(DX_INST_WRITE_DATA);
			m_curChecksum += DX_INST_WRITE_DATA;
		}

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

	public int lastError() {
		return m_error;
	}

	public ReturnPacket returnPacket() {
		return m_returnPacket;
	}

	protected boolean handleReturnStatus(int id) {
		if (!readStatus(m_returnPacket))
			return false;

		m_error = m_returnPacket.error;
		if (m_returnPacket.id != id || m_error != 0) {
			m_error |= DX_ERROR_USR_ID;
			return false;
		} else
			return true;
	}

	protected boolean handleReturnStatus() {
		if (!readStatus(m_returnPacket)) {
			m_error = DX_ERROR_USR_READSTATUS;
			return false;
		}

		m_error = m_returnPacket.error;
		return true;
	}

	protected synchronized boolean readStatus(ReturnPacket returnPacket) {
		if (!readStart(m_timeout)) {
			m_error |= DX_ERROR_USR_NO_BEGIN;
			return false;
		}

		if (!waitForData(m_serial, m_timeout, m_delay, 3)) {
			m_error |= DX_ERROR_USR_DATA_TIMEOUT;
			return false;
		}

		m_curChecksum = 0;

		// read id
		returnPacket.id = read();


		// read length
		returnPacket.length = read();

		// read error
		returnPacket.error = read();

		// wait for the rest of the data
		if (!waitForData(m_serial, m_timeout, m_delay, returnPacket.length - 1)) {
			m_error |= DX_ERROR_USR_DATA_TIMEOUT;
			return false;
		}

		// read param
		returnPacket.param.clear();
		for (int i = 0; i < returnPacket.length - 2; i++)
			returnPacket.param.add(read());

		return returnPacket.checksum() == read();
	}

	protected boolean readStart(int timeout) {
		m_error = 0;

		int origTimeout = timeout;
		int failCount = 100;

		timeout = origTimeout;
		while (failCount > 0) {
			timeout = origTimeout;
			if (!waitForData(m_serial, timeout, m_delay, 1))
				return false;

			if (read() == DX_BEGIN)
				break;
			else
				failCount--;
		}

		// second begin
		timeout = origTimeout;
		if (!waitForData(m_serial, timeout, m_delay, 1))
			return false;

		if (read() != DX_BEGIN)
			return false;

		// data
		timeout = origTimeout;
		return waitForData(m_serial, timeout, m_delay, 4);

	}

	public static boolean waitForData(SerialPort serial, int timeout, int delay, int dataCount) {
		try {
			while (serial.getInputStream().available() < dataCount && (timeout--) >= 0) {
				if (!locSleep(delay))
					return false;
			}

			return !(timeout < 0 && serial.getInputStream().available() < dataCount);

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
		} catch (InterruptedException exception) {
			System.out.println("InterruptedException: " + exception);
			return false;
		}
		return true;
	}


	public static String errorStr(int error) {
		String retStr = "";

		if ((error & DX_ERROR_INVOLT) > 0)
			retStr += "Input Voltage Error\n";

		if ((error & DX_ERROR_ANGLELIMIT) > 0)
			retStr += "Angle Limit Error\n";

		if ((error & DX_ERROR_OVERHEAT) > 0)
			retStr += "OverHeating Error\n";

		if ((error & DX_ERROR_RANGE) > 0)
			retStr += "Range Error\n";

		if ((error & DX_ERROR_CHECKSUM) > 0)
			retStr += "CheckSum Error\n";

		if ((error & DX_ERROR_OVERLOAD) > 0)
			retStr += "Overload Error\n";

		if ((error & DX_ERROR_INST) > 0)
			retStr += "Instruction  Error\n";


		if ((error & DX_ERROR_USR_ID) > 0)
			retStr += "Status Packet - Wrong id\n";

		if ((error & DX_ERROR_USR_READSTATUS) > 0)
			retStr += "Status Packet - \n";

		if ((error & DX_ERROR_USR_NO_BEGIN) > 0)
			retStr += "Status Packet - No begin found\n";

		if ((error & DX_ERROR_USR_DATA_TIMEOUT) > 0)
			retStr += "Status Packet - Data timeout\n";

		return retStr;
	}

}
