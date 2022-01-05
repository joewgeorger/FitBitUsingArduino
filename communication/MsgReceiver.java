package communication;

import java.lang.Thread.State;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import jssc.*;

public class MsgReceiver {

	final private SerialComm port;

	enum State{
		idle,
		magicNumber,
		keyTime,
		keyInfo,
		keyError,
		keyAcc,
		keyButton,
		countStep,

	}

	State currState = State.idle;
	public int arrCount = 600;
	public int[] X = new int[arrCount];	
	public int[] Y = new int[arrCount];	
	public int[] Z = new int[arrCount];
	public int timeCount = 0;
	public int[] r = new int[arrCount];
	public int count = 0;
	public boolean button = false;
	public int steps = 0;
	public int sleep = 0;

	public boolean movCheck( int[] a, int c) {
		int b = 0;
		if((a[c] + 5) > a[c-1] ){
			b++;
		}
		if((a[c] - 5) < a[c-1]) {
			b++;
		}
		if((a[c] + 5) > a[c+1] ){
			b++;
		}
		if((a[c] - 5) < a[c+1]) {
			b++;
		}
		if(a[c] < 6000) {
			b++;
		}
		if(b == 5) {
			return true;
		}
		else {
			return false;
		}

	}


	public MsgReceiver(String portname) throws SerialPortException {
		port = new SerialComm(portname);
	}

	public State nextState( State state) throws SerialPortException   {
		switch (state) {
		case idle:
			if(count == (arrCount - 1)) {
				state = State.countStep;
				break;
			}
			if(timeCount == (arrCount - 1)) {
				int rr = r[arrCount-1];
				r[0] = rr;
				timeCount = 1;
			}
			if(String.format("%02x", port.readByte()).equals("21")) {
				//System.out.println("idle");
				state = State.magicNumber;
			}
			break;	

		case magicNumber:
			String key = String.format("%02x", port.readByte());
			if(key.equals("30")) {
				state = State.keyInfo;
			}
			else if(key.equals("31")) {
				state = State.keyError;
			}
			else if(key.equals("32")) {
				state = State.keyTime;
			}
			else if(key.equals("35")) {
				state = State.keyAcc;
			}
			else if(key.equals("36")) {
				state = State.keyButton;
			}
			else {
				System.out.println(" !!ERROR!! ");
				state = State.idle;
			}
			break;

		case keyInfo:
			state = State.idle;
			break;

		case keyTime:
			state = State.idle;
			break;

		case keyError:
			state = State.idle;
			break;
		case keyButton:
			state = State.idle;
			break;
		case keyAcc:
			state = State.idle;
			break;
		case countStep:
			state = State.idle;
			break;
		}
		return state;
	}

	@SuppressWarnings("null")
	public void run() throws SerialPortException {
		// insert FSM code here to read msgs from port
		// and write to console
		while(true) {
			if(port.available()) {
				port.setDebug(false);
				switch(currState) {
				case idle:
					//System.out.println("-------");
					currState = nextState(currState);
					break;

				case keyInfo:
					String s = "";
					for(int i = 0; i < 2; i++) {
						s += String.format( "%02x" , port.readByte());
					}
					int l = Integer.parseInt( s , 16);
					byte[] b = new byte[l];
					for(int j = 0; j < l; j++) {
						b[j] = port.readByte();
						System.out.println(b[j]);
					}
					String db = new String(b, StandardCharsets.UTF_8);
					System.out.println("Debug" + db);
					currState = nextState(currState);
					break;

				case keyError:
					String m = "";
					for(int i = 0; i < 2; i++) {
						m += String.format( "%02x" , port.readByte());
					}
					int v = Integer.parseInt( m , 16);
					//System.out.println(" v = " + v);
					byte[] b1 = new byte[v];
					for(int j = 0; j < v; j++) {
						b1[j] = port.readByte();
					}
					String error = new String(b1, StandardCharsets.UTF_8);
					System.out.println(error);
					currState = nextState(currState);
					break;

				case keyTime:
					String t = "";
					for(int i = 0; i < 4; i++) {
						t += String.format( "%02x" , port.readByte());
					}
					r[timeCount] = Integer.parseInt( t , 16);
					//System.out.println("TimeStamp " + r[timeCount]);
					timeCount++;
					currState = nextState(currState);
					break;

				case magicNumber:
					currState = nextState(currState);
					break;
				case keyButton:
					if(button == true) {
						button = false; 	// steps
						//System.out.println("--Counting Steps--");
					}
					else if(button == false) {
						button = true;		// sleep
						//System.out.println("--Counting Sleep--");
					}
					currState = nextState(currState);
					break;
				case keyAcc:
					String x = "";
					String y = "";
					String z = "";
					for(int i = 0; i < 2; i++) {
						x += String.format( "%02x" , port.readByte());
					}
					for(int i = 0; i < 2; i++) {
						y += String.format( "%02x" , port.readByte());
					}
					for(int i = 0; i < 2; i++) {
						z += String.format( "%02x" , port.readByte());
					}
					int xx = Integer.parseInt( x, 16);
					int yy = Integer.parseInt( y, 16);
					int zz = Integer.parseInt( z, 16);
					//System.out.println("x="+xx+": y="+yy+": z="+zz);
					if(button == true) {
						if(movCheck(X,count) && movCheck(Y,count) && movCheck(Z,count)  ) {
							sleep += r[timeCount] - r[timeCount - 1];
							System.out.println("--Time asleep =" + sleep + "--");
						}
						else {
							sleep = 0;
						}

					}
					X[count] = xx;
					Y[count] = yy;
					Z[count] = zz;
					//System.out.println(count);
					count++;
					currState = nextState(currState);
					break;

				case countStep:
					if(button == false) {
						for( int q = 1; q < X.length; ++q) {
							if((X[q] > 30) && (X[q] < 6000)){
								if((X[q] > X[q-1]) && (X[q] > X[q+1])) {
									if((X[q] > (X[q-1]+ 15)) || X[q] > (X[q+1]+ 15)) {

									}
									else {
										steps++;
										//System.out.println(q);
										q += 10;
									}

								}
							}
						}
						System.out.println("Amount of steps = " + steps);
					}
					int x1 = X[arrCount - 1];
					X[0] = x1;
					int y1 = Y[arrCount - 1];
					Y[0] = y1;
					int z1 = Z[arrCount - 1];
					Z[0] = z1;
					count = 1;
					currState = nextState(currState);
					break;
				}
			}
		}

	}


	public static void main(String[] args) throws SerialPortException {
		MsgReceiver msgr = new MsgReceiver("COM7"); // Adjust this to be the right port for your machine
		msgr.run();
	}
}
