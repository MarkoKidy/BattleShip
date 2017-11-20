import javax.swing.JButton;

public class MyButton extends JButton {

	private static final long serialVersionUID = 1L;
	
	public int x;
	public int y;
	public Ship ship;
	public boolean state = true;
	public int respond = 0;
	
	public MyButton(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public void setShip(Ship ship)
	{
		this.ship = ship;
	}
	
}
