
public class Client {

	public static void main(String[] args) {
		
		GUISinkingShips gui = new GUISinkingShips();
		Navy navy = new Navy(5);
		gui.navy = navy;
		gui.setNextShip();

	}

}
