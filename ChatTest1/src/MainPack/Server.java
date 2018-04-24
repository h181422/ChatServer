package MainPack;

/*
 * This class starts a thread with each hostconnection
 */

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ConnectionHandlers.HostConnection;
import Messages.LoginMessage;

public class Server {
	

	public static void main(String[] args) throws IOException {
		Socket sock;
		ServerSocket servSocket = null;
		List<HostConnection> activeConnectionsObject = Collections.synchronizedList(new ArrayList<HostConnection>());
		//As i do networking in synchronized wrappers, this list would have been better off as a CopyOnWriteArrayList
		Executor executor= Executors.newCachedThreadPool();
		try {
			
			int port = 1234;
			servSocket = new ServerSocket(port);				
			
			//setting up database:
			DatabaseStuff.createNewDatabase("authentication.db");
			DatabaseStuff.createNewTable("people");
			
			System.out.println("Server has started. Waiting for clients..");
			while(true) {
				//wait for a new connection
				sock = servSocket.accept();
				//Then create a thread which will take care of that connection 
				System.out.println("Connected to " + sock.getInetAddress() + ":" + sock.getPort());
				HostConnection hcon = new HostConnection(sock, activeConnectionsObject);
				executor.execute(hcon);
				activeConnectionsObject.add(hcon);
				
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if(servSocket != null)
				servSocket.close();
		}
		
	}

}
