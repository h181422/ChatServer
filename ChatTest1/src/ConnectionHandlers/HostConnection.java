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
				//announce that this threads client is online (has to wait until it has the username)
				if(username != null) {
					if(!announced) {
						announced = true;
						sendToAll(new Message(username+" has logged on","ALL","Server"));
					}
				}
				
				//Reads from the socket
				mom = mis.readMessage();
				
				//null indicates that the host has ended the connection
				if(mom == null) {
					System.out.println(sock.getInetAddress() + ":" + sock.getPort() + " has ended the connection.");
					return;
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
						//if status is -1 client has told us that it is disconnecting
						System.out.println("Ending connection to " + sock.getInetAddress() + ":" + sock.getPort());
						return;
					}
					if(umsg.getStatus() == UpdateMessage.MY_NAME_IS) {
						username = umsg.getUsername();	
					}
					if(umsg.getStatus() == UpdateMessage.REQUEST_USERS_ONLINE) {
						informClientOfUsersOnline();
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
	
	
	private void informClientOfUsersOnline() throws SocketException, IOException {
		synchronized(otherConnectionsObject) {
			UsersOnline uo = new UsersOnline(otherConnectionsObject);
			new MessageOutputStream(sock.getOutputStream()).writeMessage(uo);		
		}
	}
	private void sendTo(Message msg) throws SocketException, IOException {
		//Find receiver by username from list of other hostconnections
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
	public String toString() {
		String s = "Connected to ";
		if(sock != null)
			s+= sock.getInetAddress().toString() + ":" + sock.getPort();
		return s;
	}
}
