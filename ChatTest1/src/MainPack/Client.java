package MainPack;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

import Messages.Message;
import Messages.MessageOutputStream;
import Messages.MyNameIsMessage;
import Messages.RequestUsersOnline;
import Messages.UpdateMessage;

/*
 * This is the client.
 * Responsible for user input an sending messages
 */

public class Client {
	
	public static void main(String[] args) {
		//commands
		final String EXIT = "exit";
		final String END = "end";
		final String WHO = "?who?";
		final String ALL = "ALL";
		final String HELP = "HELP";
		final String HELPMESSAGE = "Exit: " +EXIT+", End: "+END+", Who is online: "+WHO+", Send to all: "+ALL+", this message: "+HELP;
		
		BufferedReader br;
		Socket sock;
		InetAddress serverAddress;
		Message msg = null;
		String serverAddressString = "127.0.0.1";
		int port = 1234;
		String theMessage = "Send this message!";
		String sendFrom   = "MEEE<3";
		String sendTo     = "you...";

		
		try {
			//Ask user for information
			System.out.println("Choose a username");
			br = new BufferedReader(new InputStreamReader(System.in));
			do {
				sendFrom = br.readLine();
				if(sendFrom.length() >= Byte.MAX_VALUE)
					System.out.println("Venligst velg et kortere navn..");
			}while (sendFrom.length() >= Byte.MAX_VALUE);

			//set up a connection to the server
			serverAddress = InetAddress.getByName(serverAddressString);
			sock = new Socket(serverAddress, port);
			OutputStream out = sock.getOutputStream();
			MessageOutputStream mos = new MessageOutputStream(out);

			//Make a thread to receive messages.
			Thread t1 = new Thread(new Receive(sock.getInputStream()));
			t1.setDaemon(true);
			t1.start();
			
			
			//Send my name to the server followed by a request to see who else is online
			mos.writeMessage(new MyNameIsMessage(sendFrom));
			mos.writeMessage(new RequestUsersOnline());
			
			
			
			System.out.println("Who would you like to send messages to? "+ALL+" = send to all, ");
			sendTo = br.readLine();
			System.out.println("Ready to send messages! "+END+" = end application " + HELP +" for more info." );
			if(sendTo.equals(END)) {
				if(sock!=null)
					sock.close();
				return;
			}
			//Send a status update to inform that you are online
			UpdateMessage umsg = new UpdateMessage(sendFrom);
			mos.writeMessage(umsg);
			
			//send messages
			while(sock.isConnected()) {
				//Read message from user
				theMessage = br.readLine();
				//determine if it is a command or a message
				if(!theMessage.equals(EXIT) && !theMessage.equals(END)) {
					if(theMessage.equals(WHO)){
						mos.writeMessage(new RequestUsersOnline());
					}else if(theMessage.equals(HELP)){
						System.out.println(HELPMESSAGE);
					}
					else {
						//Send the message
						msg = new Message(theMessage, sendTo, sendFrom);
						mos.writeMessage(msg);											
					}
				}else {
					if(theMessage.equals(END)) {
						if(sock!=null)
							sock.close();
						break;
					}
					System.out.println("Who would you like to send messages to?"
							+ "\nWrite "+ALL+" to send to everyone,"
							+ "\n"+END+" to close application");
					sendTo = br.readLine();
					if(sendTo.equals(END)) {
						if(sock!=null)
							sock.close();
						return;
					}
					System.out.println("Ready to send messages, \""+EXIT+"\" to change the receiver");
				}
				if(!sock.isConnected())
					System.out.println("Lost connection, socket is no longer connected");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("exiting -> io exception");
		}
	}
	
}
