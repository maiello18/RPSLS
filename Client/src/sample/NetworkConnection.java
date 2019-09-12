package sample;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Consumer;

public abstract class NetworkConnection {
	
	private ConnThread connthread = new ConnThread();
	private Consumer<Serializable> callback;

	public Consumer<Serializable> getCallback(){
		return this.callback;
	}

	public NetworkConnection(Consumer<Serializable> callback) {
		this.callback = callback;
		connthread.setDaemon(true);
	}
	
	public void startConn() throws Exception{
		connthread.start();
	}
	
	public void send(Serializable data) throws Exception{
		//null when its not the first client made....this isn't working
		//if(connthread.out == null){
			//connthread = new ConnThread();
			//connthread.fixNullOut();
		//}
		connthread.out.writeObject(data);
	}

	public void send(Serializable data, Socket s) throws Exception{
		connthread.out = new ObjectOutputStream( s.getOutputStream());
		connthread.out.writeObject(data);
	}
	
	public void closeConn() throws Exception{
		if(connthread.socket != null){
			connthread.socket.close();
		}
	}

	abstract protected boolean isServer();
	abstract protected String getIP();
	abstract protected int getPort();
	
	public class ConnThread extends Thread{
		private Socket socket;
		private ObjectOutputStream out;

		public void run() {
			try(ServerSocket server = null;
				Socket socket = new Socket(getIP(), getPort());
				ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
			)
			{

				this.socket = socket;
				this.out = out;
				socket.setTcpNoDelay(true);
				
				while(true) {
					Serializable data = (Serializable) in.readObject();
					if(((String) data).equals("clear screen")){
						Main.getMessages().clear();
					}
					else{
						callback.accept(data);
					}
				}
				
			}
			catch(Exception e) {
				callback.accept("connection Closed");
			}
		}

		public void fixNullOut() throws Exception{
			Socket socket = new Socket(getIP(), getPort());
			ObjectOutputStream out = new ObjectOutputStream( socket.getOutputStream());

			this.socket = socket;
			this.out = out;
		}
	}
	
}	

