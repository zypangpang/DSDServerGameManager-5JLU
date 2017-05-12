import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
	private static final int PORT = 12345;
	private ConcurrentLinkedQueue<ObjectSocket> _queue;

	public Server() {
		_queue = new ConcurrentLinkedQueue<ObjectSocket>();
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(new AcceptRunnable());
		executorService.execute(new IORunnable());
		executorService.shutdown();
	}	// end Server()

	private class ObjectSocket {
		private Socket _socket;
		private ObjectInputStream _inputStream;
		private ObjectOutputStream _outputStream;

		public ObjectSocket(Socket socket) {
			_socket = socket;
			try {
				_outputStream = new ObjectOutputStream(_socket.getOutputStream());
				_outputStream.flush();
				_inputStream = new ObjectInputStream(_socket.getInputStream());
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}	// end ObjectSocket(Socket socket)

		public ObjectInputStream getInputStream() {
			return _inputStream;
		}	// end getInputStream()

		public ObjectOutputStream getOutputStream() {
			return _outputStream;
		}	// end getOutputStream()
		
		public SocketAddress getSocketAddr() {
			return _socket.getLocalSocketAddress();
		}

	}	// end class ObjectSocket

	private class AcceptRunnable implements Runnable {
		private int clientNumber = 0;

		@Override
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(PORT);
				boolean running = true;
				while (running) {
					Socket socket = serverSocket.accept();
					_queue.offer(new ObjectSocket(socket));		// .offer() inserts ObjectSocket at the tail of _queue
					System.out.println("Client #" + ++clientNumber + " connected to " + socket.getLocalSocketAddress());
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}	// end run()
	}	// end class AcceptRunnable

	private class IORunnable implements Runnable {
		@Override
		public void run() {
			boolean running = true;
			System.out.println("SGM initialized, awaiting client connection(s)...");
			while (running) {
				for (ObjectSocket objectSocket : _queue) {
					// Try/catch inside loop so if a read fails,
					// it will continue to read the rest of the clients
					ObjectInputStream inputStream = objectSocket.getInputStream();
					try {
						// Check for available bytes to prevent block on readUTF() call
						if (inputStream.available() > 0) {
							String message = inputStream.readUTF();
							broadcastMessage(message);
						}
					} catch (IOException ex) {
						System.out.println("[ERROR]: Unable to read from " + objectSocket);
					}
				}
			}
		}	// end run()
	}	// end class IORunnable


	private void broadcastMessage(String message) {
		System.out.println("\n[BROADCAST]: " + message);
		
		for (ObjectSocket objectSocket : _queue) {
			// Try/catch inside loop so that one failed
			// write does not prevent remaining writes
			ObjectOutputStream outputStream = objectSocket.getOutputStream();
			try {
				outputStream.writeUTF(message);
				outputStream.flush();
				
				System.out.println("Sent to " + objectSocket.getSocketAddr());
				
			} catch (IOException ex) {
				System.out.println("[ERROR]: " + objectSocket + "did not recieve message: " + message);
			}
		}
	}	// end broadcastMessage(String message)


	public static void main(String[] args) {
		new Server();
	}	// end main()

}	// end class Server