package MainPack;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;

import Messages.LoginMessage;
import Messages.Message;
import Messages.MessageOutputStream;
import Messages.UpdateMessage;

/*
 * This is the client.
 * Responsible for user input an sending messages
 */

public class Client {
	//commands
	final static String EXIT = "exit";
	final static String END = "end";
	final static String WHO = "?who?";
	final static String ALL = "ALL";
	final static String HELP = "HELP";
	final static String[] COMMANDS = {EXIT, END, WHO, HELP};
	final static String HELPMESSAGE = "Exit: " +EXIT+", End: "+END+", Who is online: "+WHO+", Send to all: "+ALL+", this message: "+HELP;
	
	static BufferedReader br;
	static Socket sock;
	static InetAddress serverAddress;
	static Message msg = null;
	static String serverAddressString = "127.0.0.1";
	static int port = 1234;
	static String theMessage = "Send this message!";
	static String sendFrom   = "MEEE<3";
	static String sendTo     = "you...";
	static OutputStream out;
	static MessageOutputStream mos; 
	static Thread t1;
	static boolean nameAccepted;
	static boolean waitForAccept;

	public static void main(String[] args) {	
		try {
			//Ask user for information
			System.out.println("Choose a username (Per)");
			br = new BufferedReader(new InputStreamReader(System.in));
			do {
				sendFrom = br.readLine();
			}while (!validateName(sendFrom));

			//set up a connection to the server
			serverAddress = InetAddress.getByName(serverAddressString);
			sock = new Socket(serverAddress, port);
			out = sock.getOutputStream();
			mos = new MessageOutputStream(out);

			//Make a thread to receive messages.
			t1 = new Thread(new Receive(sock.getInputStream()));
			t1.setDaemon(true);
			t1.start();
			
			
			//Send my name to the server followed by a request to see who else is online
			mos.writeMessage(new LoginMessage(sendFrom, "pass", LoginMessage.LOGIN));
			mos.writeMessage(new UpdateMessage(sendFrom, UpdateMessage.REQUEST_USERS_ONLINE));
			
			//Choose who you would like to send messages to
			whoToSendTo();
			
			//Send a status update to inform that you are online
			UpdateMessage umsg = new UpdateMessage(sendFrom, UpdateMessage.MY_NAME_IS);
			mos.writeMessage(umsg);
			
			//send messages
			System.out.println("Ready to send messages! "+END+" = end application " + HELP +" for more info." );
			while(!sock.isClosed()) {
				//Read message from user
				theMessage = br.readLine();
				//determine if it is a command or a message
				if(isCommand(theMessage)) {
					processCommand(theMessage);
				}else {
					sendMessage(theMessage);
				}
				if(!sock.isConnected())
					System.out.println("Lost connection, socket is no longer connected");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Exiting -> io exception");
		}finally {
			if(sock != null)
				if(!sock.isClosed()) {
					try {
						sock.close();
					} catch (IOException e) {/*ignore*/}					
				}
			System.out.println("Connection closed, closing application");
		}
	}
	
	//loops through the commands to see if the input is a command
	private static boolean isCommand(String input) {
		for(int i = 0;i < COMMANDS.length; i++) {
			if(input.equals(COMMANDS[i]))
				return true;
		}
		return false;
	}
	
	//determines what command was called and acts accordingly
	private static void processCommand(String command) throws IOException {
		if(command.equals(WHO)){
			mos.writeMessage(new UpdateMessage(sendFrom, UpdateMessage.REQUEST_USERS_ONLINE));
		}
		else if(command.equals(HELP)){
			System.out.println(HELPMESSAGE);
		}
		else if(command.equals(EXIT)){
			whoToSendTo();
		}
		else if(command.equals(END)) {
			if(sock != null)
				sock.close();
			System.out.println("Closing sender");
			System.exit(0);
		}
	}
	
	//Lets the user choose a valid recipient or use commands
	private static void whoToSendTo() throws IOException {
		System.out.println("Who would you like to send messages to?"
				+ "\nWrite \""+ALL+"\" to send to everyone,"
				+ "\n\""+HELP+"\" for more info");
		do {
			sendTo = br.readLine();
			if(isCommand(sendTo)) {
				if(sendTo.equals(EXIT)) {
					processCommand(END);
				}else {
					processCommand(sendTo);						
				}
			}else {
				if(!sendTo.equals(ALL)) {
					//Asks the server if the recipient is online and blocks until answered
					if(canISendTo(sendTo)) {
						System.out.println("User: " + sendTo +" is online");
					}else {
						System.out.println("User: " + sendTo +" is not online, try again");
					}
				}else {
					break;
				}				
			}
		}while(!(validateName(sendTo) && nameAccepted));
	}
	
	//Asks the server if the user is online and blocks until answered
	private static boolean canISendTo(String sendTo) throws SocketException {
		//Ask server if the user is online 
		waitForAccept = false;
		mos.writeMessage(new UpdateMessage(sendTo, UpdateMessage.ASK_IF_ONLINE));
		//wait for a reply
		while(!waitForAccept) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {/*ignore*/}
		}
		return nameAccepted;
	
	}
	
	//simply sends the string entered as a message
	private static void sendMessage(String msg) throws SocketException {
		mos.writeMessage(new Message(msg, sendTo, sendFrom));
	}
	
	//used to prevent user from picking a bad name, Allows a user to be logged in from several locations
	private static boolean validateName(String name) {
		if(name.length() >= Byte.MAX_VALUE) {		
			System.out.println("Venligst velg et kortere navn..");
			return false;
		}
		if(isCommand(name) || name.equals(ALL)) {
			return false;
		}
		return true;
	}
	
	//used by the Receiver class to inform what the server replied
	//to the UpdateMessage ask if online message
	public static void targetOnline(boolean online) {
		nameAccepted = online;
		waitForAccept = true;
	}
	
}
