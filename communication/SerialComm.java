package communication;

import jssc.SerialPort;
import jssc.SerialPortException;

public class SerialComm {

	//Import your SerialComm.java from Assignment 7
static SerialPort port;
	
	

	private static boolean debug;  // Indicator of "debugging mode"
	
	// This function can be called to enable or disable "debugging mode"
	void setDebug(boolean mode) {
		debug = mode;
	}	
	

	// Constructor for the SerialComm class
	public SerialComm(String name) throws SerialPortException {
		port = new SerialPort(name);		
		port.openPort();
		port.setParams(SerialPort.BAUDRATE_9600,
			SerialPort.DATABITS_8,
			SerialPort.STOPBITS_1,
			SerialPort.PARITY_NONE);
		
		debug = false; // Default is to NOT be in debug mode
	}
		
	// TODO: Add writeByte() method to write data to serial port (Studio Section 5)
	public void writeByte(byte b) throws SerialPortException {
		port.writeByte(b);
		if (debug == true) {
			int x = b & 0xFF;
			System.out.println(Integer.toHexString(x));
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	// TODO: Add available() method (Studio Section 6)
	public boolean available() throws SerialPortException {
		if(port.getInputBufferBytesCount() > 0) {
			return true;
		}
		else {
			return false;
		}
		
	}
	
	// TODO: Add readByte() method (Studio Section 6)
	@SuppressWarnings("resource")
	public byte readByte() throws SerialPortException {
		byte b[] = new byte[1];
		 b = port.readBytes(1);
		if(debug == true) {
			 String hex = String.format( "%02x", b[0]);
			 System.out.println("[0x" + hex + "]");
			 }
		return b[0];
		
	}
}
