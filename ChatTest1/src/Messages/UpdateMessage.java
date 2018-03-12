package Messages;

//This message should be sent as a message to request information

import java.nio.ByteBuffer;
import java.util.Arrays;

public class UpdateMessage extends MotherOfAllMessages{

	String username;
	byte status;
	
	public UpdateMessage(String username) {
		this.username = username;
		length = (short) (username.length() + 2 + 2 + 1);
		status = 0;
	}
	public UpdateMessage(String username, byte status) {
		this.username = username;
		length = (short) (username.length() + 2 + 2 + 1);
		this.status = status;
	}
	
	public UpdateMessage(byte[] fromBytes) {
		extractHeader(fromBytes);
		username = new String(Arrays.copyOfRange(fromBytes, 4, length-1));
		status = fromBytes[length-1];
	}
	
	@Override
	public byte[] serialize() {
		length = (short) (username.length() + 2 + 2 + 1);
		ByteBuffer serialized = ByteBuffer.allocate(length);
		
		serialized.put(serializeHeader());
		serialized.put(username.getBytes());
		serialized.put((byte)status);
		//[type size username status]
		return serialized.array();
	}

	public String getUsername() {
		return username;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(byte status) {
		this.status = status;
	}
	public String toString() {
		return type +" "+ length +" "+ username +" "+ status;
	}

	@Override
	protected void setType() {
		type = 1;
	}
}
