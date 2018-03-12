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
import Messages.MyNameIsMessage;
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
						synchronized(otherConnectionsObject) {
							for(HostConnection r : otherConnectionsObject) {
								if(r == null) {
									otherConnectionsObject.remove(r);
								}else if(r.getSock() != null){
									if(r.getSock().isConnected()) {
										new MessageOutputStream(r.getSock().getOutputStream()
												).writeMessage(new Message(username+" has logged on",
														"ALL","Server"));		
									}
								}
							}
						}
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
				//in order to process it correctly
				
				if(mom.getType() == 0) {
					Message msg = (Message)mom;
					if(msg.getTo().equals("ALL")) {
						//Send to all
						//Gets all the other host connections and sends the message
						synchronized(otherConnectionsObject) {
							for(HostConnection r : otherConnectionsObject) {
								if(r == null) {
									otherConnectionsObject.remove(r);
								}else if(r.getSock() != null){
									if(r.getSock().isConnected() && r.username != this.username) {
										new MessageOutputStream(r.getSock().getOutputStream()
												).writeMessage(msg);		
									}
								}
							}
						}
					}else {
						//Send to someone specific
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
				}
				//This message is ment to update that the user is active
				//ToDo: make a keep-alive message flow
				else if(mom.getType() == 1){
						UpdateMessage umsg = (UpdateMessage)mom;
						username = umsg.getUsername();	
						if(umsg.getStatus() == -1) {
							//if status is -1 client has told us that it is disconnecting
							System.out.println("Ending connection to " + sock.getInetAddress() + ":" + sock.getPort());
							return;
						}
				}
				//if client requests useres online: send list of usernames
				else if(mom.getType() == 3) {
					//RequestUseresOnlineMessage
					synchronized(otherConnectionsObject) {
						UsersOnline uo = new UsersOnline(otherConnectionsObject);
						
						new MessageOutputStream(sock.getOutputStream()).writeMessage(uo);		
					}
				}
				//client sends his name
				else if(mom.getType() == 4) {
					MyNameIsMessage mnim = (MyNameIsMessage)mom;
					username = mnim.getUsername();
				}
				if(sock == null)
					return;
			}
		}catch(SocketException ex) {
			System.out.println("Lost Connection to " + ((sock == null) ? "host" :
				sock.getInetAddress().toString()) + ":" + sock.getPort());
			//System.out.println("Thread: " + Thread.currentThread().getName());
			//ex.printStackTrace();
		}catch(Exception ex) {
			ex.printStackTrace();
		}finally {
			
			//announce that this threads client has left
			synchronized(otherConnectionsObject) {
				for(HostConnection r : otherConnectionsObject) {
					if(r == null) {
						otherConnectionsObject.remove(r);
					}else if(r.getSock() != null){
						if(r.getSock().isConnected()) {
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
			
			try {
				if(out != null)
					out.close();
				if(in != null)
					in.close();
				if(sock != null)
					sock.close();
				synchronized(otherConnectionsObject) {
					otherConnectionsObject.remove(this);				
				}
			} catch (IOException e) {
				e.printStackTrace();
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
