package Messages;

//This could be replaced by the Updatemessage, but for now, it is a byte smaller at least

import java.nio.ByteBuffer;
import java.util.Arrays;

public class MyNameIsMessage extends MotherOfAllMessages {

	String username;
	
	public MyNameIsMessage(String username) {
		this.username = username;
		length = (short) (4+username.length());
		setType();
	}
	public MyNameIsMessage(byte[] fromBytes) {
		extractHeader(fromBytes);
		username = new String(Arrays.copyOfRange(fromBytes, 4, length));
	}
	
	public String getUsername() {
		return username;
	}
	
	@Override
	public byte[] serialize() {
		length = (short) (4+username.length());
		ByteBuffer bb = ByteBuffer.allocate(length);
		bb.put(serializeHeader());
		bb.put(username.getBytes());
		return bb.array();
	}

	@Override
	protected void setType() {
		type = 4;
	}
	public String toString() {
		return type + " " + length + " " + username;
	}

}
