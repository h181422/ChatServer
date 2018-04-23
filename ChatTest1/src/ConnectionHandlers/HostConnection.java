package ConnectionHandlers;

/*
 * This is run in its own thread.
 * Its purpose is to interact with the client
 */


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import MainPack.DatabaseStuff;
import Messages.LoginMessage;
import Messages.Message;
import Messages.MessageInputStream;
import Messages.MessageOutputStream;
import Messages.MotherOfAllMessages;
import Messages.UpdateMessage;
import Messages.UsersOnline;


public class HostConnection implements Runnable{
	
	Socket sock;
	InputStream in;
	OutputStream out;
	String username;
	List<HostConnection> otherConnectionsObject;
	boolean announced = false;
	
	MessageInputStream mis;
	MessageOutputStream mos;
	MotherOfAllMessages mom;
	
	public Socket getSock() {
		return sock;
	}


	public String getUsername() {
		return username;
	}

	
	public HostConnection(Socket sock, List<HostConnection> otherConnectionsObject) throws IOException{
		this.sock = sock;
		in = sock.getInputStream();
		out = sock.getOutputStream();
		this.otherConnectionsObject = otherConnectionsObject;
		mis = new MessageInputStream(in);
		mos = new MessageOutputStream(out);
	}
	
	
	@Override
	public void run() {
		try {
			while(sock.isConnected()) {	
				
				//Reads from the socket
				mom = mis.readMessage();
				
				//null indicates that the host has ended the connection
				//also terminates connection if the client attempts to send
				//an unsupported message format
				if(mom == null) {
					System.out.println(sock.getInetAddress() + ":" + sock.getPort()
					+ " has ended the connection.");
					return;
				}

				//Login
				if(username == null) {
					if(mom.getType() == MotherOfAllMessages.LOGIN_MESSAGE) {
						LoginMessage lm = (LoginMessage)mom;
						if(lm.getTypeL() == LoginMessage.LOGIN) {
							if(DatabaseStuff.authenticate(lm.getUsername(), lm.getPassword())) {
								mos.writeMessage(new UpdateMessage("Success", UpdateMessage.LOGIN_OK));
								username = lm.getUsername();
							}else {
								mos.writeMessage(new UpdateMessage("Invalid username or password", UpdateMessage.LOGIN_FAILED));
							}
						}
						else if(lm.getTypeL() == LoginMessage.CREATE_ACCOUNT) {
							if(DatabaseStuff.makeAccount(lm.getUsername(), lm.getPassword())) {
								mos.writeMessage(new UpdateMessage("Success", UpdateMessage.CREATE_ACCOUNT_OK));
							}else {
								mos.writeMessage(new UpdateMessage("Already in use", UpdateMessage.CREATE_ACCOUNT_FAILED));
							}
						}
					}					
				}
				
				//announce that this threads client is online (has to wait until it has the username)
				if(username != null) {
					if(!announced) {
						announced = true;
						sendToAll(new Message(username+" has logged on","ALL","Server"));
					}
				}
				
				//Check what type of message was received
				if(mom.getType() == MotherOfAllMessages.MESSAGE) {
					Message msg = (Message)mom;
					if(msg.getTo().equals("ALL")) {
						//Send the message 
						sendToAll(msg);
					}else {
						//Send to someone specific
						sendTo(msg);
					}
				}
				//This message contains commands from the client to the server
				else if(mom.getType() == MotherOfAllMessages.UPDATE_MESSAGE){
					UpdateMessage umsg = (UpdateMessage)mom;			
					if(umsg.getStatus() == UpdateMessage.EXIT) {
						System.out.println("Ending connection to " + sock.getInetAddress() + ":" + sock.getPort());
						return;
					}
					//replaced with authentication
					if(umsg.getStatus() == UpdateMessage.MY_NAME_IS) {
						//username = umsg.getUsername();	
					}
					if(umsg.getStatus() == UpdateMessage.REQUEST_USERS_ONLINE) {
						informClientOfUsersOnline();
					}
					if(umsg.getStatus() == UpdateMessage.ASK_IF_ONLINE) {
						if(isOnline(umsg.getUsername())) {
							mos.writeMessage(new UpdateMessage("Server", UpdateMessage.IS_ONLINE));
						}else {
							mos.writeMessage(new UpdateMessage("Server", UpdateMessage.NOT_ONLINE));
						}
					}
				}

			}
		}catch(SocketException ex) {
			System.out.println("Lost Connection to " + ((sock == null) ? "host" :
				sock.getInetAddress().toString()) + ":" + sock.getPort());
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			//announce that this threads client has left
			announceClientLeft();
			
			//close the socket and remove this connection from the list of online connections
			try {
				synchronized(otherConnectionsObject) {
					otherConnectionsObject.remove(this);				
				}
				if(out != null)
					out.close();
				if(in != null)
					in.close();
				if(sock != null)
					sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	//Sends a message containing all online users to the client
	private void informClientOfUsersOnline() throws SocketException, IOException {
		synchronized(otherConnectionsObject) {
			UsersOnline uo = new UsersOnline(otherConnectionsObject);
			new MessageOutputStream(sock.getOutputStream()).writeMessage(uo);		
		}
	}
	
	//Sends the mesage to someone specific
	private void sendTo(Message msg) throws SocketException, IOException {
		//Find receiver by username from list of other hostConnections
		synchronized(otherConnectionsObject) {
			for(HostConnection r : otherConnectionsObject) {
				if(r == null) {
					otherConnectionsObject.remove(r);									
				}else if(r.getUsername().equals(msg.getTo())) {
					new MessageOutputStream(r.getSock().getOutputStream()).writeMessage(msg);		
				}
			}						
		}
	}
	
	//Sends a message to everyone
	private void sendToAll(MotherOfAllMessages msg) throws SocketException, IOException {
		//For each host if it is null remove it, otherwise make a message outputStream
		//from the sockets its socket outputstream and send the message
		synchronized(otherConnectionsObject) {
			for(HostConnection r : otherConnectionsObject) {
				if(r == null) {
					otherConnectionsObject.remove(r);
				}else if(r.getSock() != null){
					if(r.getSock().isConnected()) {
						if(r.username != this.username) {
							new MessageOutputStream(r.getSock().getOutputStream()
									).writeMessage(msg);									
						}
					}
				}
			}
		}
	}
	
	//Send a message to all online clients that this client has left
	private void announceClientLeft() {
		synchronized(otherConnectionsObject) {
			for(HostConnection r : otherConnectionsObject) {
				if(r == null) {
					otherConnectionsObject.remove(r);
				}else if(r.getSock() != null){
					if(r.getSock().isConnected() && r.username != this.username) {
						try {
							new MessageOutputStream(r.getSock().getOutputStream()
									).writeMessage(new Message(username+" has left","ALL","Server"));										
						}catch(SocketException ex) {
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
	//Checks if someone with the given username is in the list of connections
	private boolean isOnline(String username) {
		synchronized(otherConnectionsObject) {
			for(HostConnection r : otherConnectionsObject) {
				if(r == null) {
					otherConnectionsObject.remove(r);
				}else if(r.getSock() != null){
					if(r.getSock().isConnected()) {
						if(username.equals(r.getUsername())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public String toString() {
		String s = "Connected to ";
		if(sock != null)
			s+= sock.getInetAddress().toString() + ":" + sock.getPort();
		return s;
	}
}
