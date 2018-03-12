package MainPack;

/*
 * This class is run in a thread to print all 
 * received messages to the console for the client
 */


import Messages.Message;
import Messages.MessageInputStream;
import Messages.MotherOfAllMessages;
import Messages.UsersOnline;

import java.io.*;
import java.net.SocketException;

public class Receive implements Runnable{

	InputStream in;
	Message msg;
	MotherOfAllMessages mom;
	MessageInputStream mis;
	
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
				if(mom.getType() == 0) {
					Message msg = (Message)mom;
					System.out.println(msg);
				}
				if(mom.getType()==2) {
					UsersOnline uo = (UsersOnline)mom;
					System.out.print("Online: ");
					System.out.println(uo.toString());
				}
				
			}
		}catch(SocketException ex) {
			System.out.println("exiting");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
