package MainPack;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import Messages.Message;
import Messages.MessageInputStream;
import Messages.MessageOutputStream;

public class TestingClass {
	
	
	//NOT IN USE - ONLY FOR TESTING PURPOSES
	//NOT IN USE - ONLY FOR TESTING PURPOSES
	//NOT IN USE - ONLY FOR TESTING PURPOSES
	//NOT IN USE - ONLY FOR TESTING PURPOSES
	//NOT IN USE - ONLY FOR TESTING PURPOSES

	static InetAddress serverAddress;
	static Socket socket;
	
	public static void main(String[] args) {
		
		try {
			//serverAddress = InetAddress.getByName("127.0.0.1");
			//socket = new Socket(serverAddress, 1234);
			//OutputStream out = socket.getOutputStream();

			//MessageOutputStream mos = new MessageOutputStream(out);
			//mos.writeMessage(new Message("msg", "to", "from"));
			
			FileInputStream in = null;
	        FileOutputStream out = null;

	        try {
	            out = new FileOutputStream("makethis.myownfiletype");
	            MessageOutputStream mos = new MessageOutputStream(out);
	            mos.writeMessage(new Message("msg", "to", "from"));
	            
	            in = new FileInputStream("makethis.myownfiletype");
	            MessageInputStream mis = new MessageInputStream(in);
	            Message m = (Message) mis.readMessage();
	            
	            System.out.println(m);
	            
	        } finally {
	            if (in != null) {
	                in.close();
	            }
	            if (out != null) {
	                out.close();
	            }
	        }
			
		
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
