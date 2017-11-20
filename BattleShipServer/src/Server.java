import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	static final int PORT = 1978;

    @SuppressWarnings("resource")
	public static void main(String args[]) {
        ServerSocket serverSocket = null;
        Socket socket1 = null;
        Socket socket2 = null;
        
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Server erorr!");
        }
        while (true) {
        	System.out.println("Server ceka igrace!");
            try {
                socket1 = serverSocket.accept();
                socket2 = serverSocket.accept();
            } catch (IOException e) {
                System.out.println("I/O error: " + e);
            }
            new GameThread(socket1, socket2).start();
        }
    }

}
