package Messages;

public interface ISendable {

	//converts the message to a byte array that can be
	//used by the input and output streams
	public byte[] serialize();
}
