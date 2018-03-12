package Messages;

/*
 * Code to serialize and deserialize a regular message
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Message extends MotherOfAllMessages{

	final int MAXNAMELENGTH = Byte.MAX_VALUE;
	
	String from;
	String to;
	String content;
	byte fromSize;
	byte toSize;

	public Message(String message, String receiver, String me) {
		type = (short)msgType.string.ordinal();
		setTo(receiver);
		setFrom(me);
		setContent(message);
		length = (short)(2+2+2+to.length()+from.length()+content.length());
		toSize = (byte)to.length();
		fromSize = (byte)from.length();
	}
	public Message(byte[] fromBytes) {
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
		toSize = fromBytes[4];
		to = new String(Arrays.copyOfRange(fromBytes, 5, 5+toSize));
		fromSize = fromBytes[5+toSize];
		from = new String(Arrays.copyOfRange(fromBytes, 5+toSize+1, 5+toSize+1+fromSize));
		content = new String(Arrays.copyOfRange(fromBytes, 5+toSize+1+fromSize, length));
	}
	
	public short getType() {
		return type;
	}

	protected void setType(msgType mt) {
		this.type =(short)mt.ordinal();
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		if(from.length() >= MAXNAMELENGTH)
			throw new IllegalArgumentException("Too long username."+
										"Max length = " + MAXNAMELENGTH);
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		if(to.length() >= MAXNAMELENGTH)
			throw new IllegalArgumentException("Too long recivername."+
										"Max length = " + MAXNAMELENGTH);
		this.to = to;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		if(content.length() > 65000)
			throw new IllegalArgumentException("Too long message."+
				"Split up the message or use a different message type");
		this.content = content;
	}


	
	@Override
	public byte[] serialize() {
		length = (short)(2+2+2+to.length()+from.length()+content.length());
		
		ByteBuffer serialized = ByteBuffer.allocate(length);
		
		serialized.put(serializeHeader());
		serialized.put((byte)to.length());
		serialized.put(to.getBytes());
		serialized.put((byte)from.length());
		serialized.put(from.getBytes());
		serialized.put(content.getBytes());
		// [MsgType Size toSize To fromSize From Content]
		return serialized.array();
	}
	
	public Message toMessage(byte[] fromBytes) {
		extractHeader(fromBytes);
		toSize = fromBytes[4];
		to = new String(Arrays.copyOfRange(fromBytes, 5, 5+toSize));
		fromSize = fromBytes[5+toSize];
		from = new String(Arrays.copyOfRange(fromBytes, 5+toSize+1, 5+toSize+1+fromSize));
		content = new String(Arrays.copyOfRange(fromBytes, 5+toSize+1+fromSize, length));
		// [MsgType Size toSize To fromSize From Content]
		return this;		
	}
	
	public String toString() {
		return from + ": " + content;
	}
	@Override
	protected void setType() {
		type = 0;
	}
	
}
