package Messages;

public class RequestUsersOnline extends MotherOfAllMessages {

	//This could be replaced by a status in the Updatemessage
	
	public RequestUsersOnline() {
		length = 4;
	}
	public RequestUsersOnline(byte[] fromBytes) {
		extractHeader(fromBytes);
	}
	
	@Override
	public byte[] serialize() {
		return serializeHeader();
	}

	@Override
	protected void setType() {
		type = 3;
	}

}
