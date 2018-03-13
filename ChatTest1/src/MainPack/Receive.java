package MainPack;

/*
 * This class is run in a thread to print all 
 * received messages to the console for the client
 */


import Messages.Message;
import Messages.MessageInputStream;
import Messages.MotherOfAllMessages;
import Messages.UpdateMessage;
import Messages.UsersOnline;

import java.io.*;
import java.net.SocketException;

public class Receive implements Runnable{

	InputStream in;
	Message msg;
	MotherOfAllMessages mom;
	MessageInputStream mis;
	Client c;
	
	public Receive(InputStream in) {
		this.in = in;
		mis = new MessageInputStream(in);
	}
	
	@Override
	public void run() {
		try {
			while(true) {
				//Using the MessageInputStream to read a message from the socket
				mom = mis.readMessage();
				
				if(mom == null) {
					System.out.println("Server closed the connection prematurely");
					return;
				}
				
				//Determine what type of message you received, to properly print it
				if(mom.getType() == MotherOfAllMessages.MESSAGE) {
					Message msg = (Message)mom;
					System.out.println(msg);
				}
				else if(mom.getType()==MotherOfAllMessages.USERS_ONLINE_MESSAGE) {
					UsersOnline uo = (UsersOnline)mom;
					System.out.print("Online: ");
					System.out.println(uo.toString());
				}
				else if(mom.getType() == MotherOfAllMessages.UPDATE_MESSAGE) {
					UpdateMessage umsg = (UpdateMessage)mom;
					if(umsg.getStatus() == UpdateMessage.NOT_ONLINE) {
						Client.targetOnline(false);
					}
					if(umsg.getStatus() == UpdateMessage.IS_ONLINE) {
						Client.targetOnline(true);
					}
				}
				
			}
		}catch(SocketException ex) {
			System.out.println("Closing receiver");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
