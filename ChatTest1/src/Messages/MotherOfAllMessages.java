package Messages;

//This is the parent of all other messages

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public abstract class MotherOfAllMessages implements ISendable{

	protected short type;
	protected short length;
	
	
	public MotherOfAllMessages() {
		setType();
	}
	public short getType() {
		return type;
	}
	
	protected enum msgType{ //Not really using this atm.
		string, update, UsersOnline, RequestUsersOnline, myNameIs;
	}
	protected abstract void setType();
	
	protected byte[] serializeHeader() {
		
		ByteBuffer bb = ByteBuffer.allocate(2);
		bb.putShort(type);
		byte[] ar1 = bb.array();
		ByteBuffer bb2 = ByteBuffer.allocate(2);
		bb2.putShort(length);
		byte[] ar2 = bb2.array();
		byte[] arBoth = new byte[4];
		arBoth[0] = ar1[0];
		arBoth[1] = ar1[1];
		arBoth[2] = ar2[0];
		arBoth[3] = ar2[1];
		return arBoth;
	}
	protected void extractHeader(byte[] fromBytes) {
		byte[] bType = new byte[2];
		bType[0] = fromBytes[0];
		bType[1] = fromBytes[1];
		ByteBuffer bb = ByteBuffer.wrap(bType).order(ByteOrder.BIG_ENDIAN);
		type = bb.getShort();
		byte[] bSize = new byte[2];
		bSize[0] = fromBytes[2];
		bSize[1] = fromBytes[3];
		ByteBuffer bb2 = ByteBuffer.wrap(bSize).order(ByteOrder.BIG_ENDIAN);
		length = bb2.getShort();
	}
	
}
