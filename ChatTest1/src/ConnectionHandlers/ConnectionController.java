package ConnectionHandlers;
import java.util.List;

public class ConnectionController implements Runnable{
	
	
	//NOT CURRENTLY IN USE
	//NOT CURRENTLY IN USE
	//NOT CURRENTLY IN USE
	//NOT CURRENTLY IN USE
	
	List<HostConnection> activeConnectionsObjet;
	
	public ConnectionController(List<HostConnection> activeConnectionsObjet) {
		this.activeConnectionsObjet = activeConnectionsObjet;
	}

	@Override
	public void run() {
		
	}
	
	public void updateList() {

	}
	public String toString() {
		String s = "";
		return s;
	}
	
}
