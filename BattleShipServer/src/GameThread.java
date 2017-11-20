import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class GameThread extends Thread {

	private Socket socket1;
	private Socket socket2;
	private DataInputStream inFromSocket1;
	private DataOutputStream outToSocket1;
	private DataInputStream inFromSocket2;
	private DataOutputStream outToSocket2;

	public GameThread(Socket clientSocket1, Socket clientSocket2) {
		this.socket1 = clientSocket1;
		this.socket2 = clientSocket2;
	}

	public void run() {
		byte[] bufferName = new byte[8];
		String name1;
		String name2;
		DataBaseHandler dataBaseHandler = DataBaseHandler.getInstance();
		System.out.println("Game is up!");
		int replay;
		try {
			inFromSocket1 = new DataInputStream(socket1.getInputStream());
			outToSocket1 = new DataOutputStream(socket1.getOutputStream());

			inFromSocket2 = new DataInputStream(socket2.getInputStream());
			outToSocket2 = new DataOutputStream(socket2.getOutputStream());

			outToSocket1.writeInt(1);
			outToSocket2.writeInt(2);

			inFromSocket1.read(bufferName);
			name1 = new String(bufferName).trim();
			inFromSocket2.read(bufferName);
			name2 = new String(bufferName).trim();

			while (true) {
				replay = 1;

				while (replay > 0) {
					int x = inFromSocket1.readInt();
					int y = inFromSocket1.readInt();
					outToSocket2.writeInt(x);
					outToSocket2.writeInt(y);
					System.out.println("Server salje x: " + x + " y: " + y);
					replay = inFromSocket2.readInt();
					System.out.println("Server prima: " + replay);
					outToSocket1.writeInt(replay);
				}
				if (replay < 0) {
					dataBaseHandler.addWinToAPlayer(name1);
					dataBaseHandler.addGameToAPlayer(name2);
					break;
				}

				replay = 1;

				while (replay > 0) {
					int x = inFromSocket2.readInt();
					int y = inFromSocket2.readInt();
					outToSocket1.writeInt(x);
					outToSocket1.writeInt(y);
					System.out.println("Server salje x: " + x + " y: " + y);
					replay = inFromSocket1.readInt();
					System.out.println("Server prima: " + replay);
					outToSocket2.writeInt(replay);
				}
				if (replay < 0) {
					dataBaseHandler.addWinToAPlayer(name2);
					dataBaseHandler.addGameToAPlayer(name1);
					break;
				}

			}
			System.out.println("GameThread off");

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
