import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class GUISinkingShips extends JFrame {

	//private GenericFactoryUsingReflection<MyButton> factory;
	private static final long serialVersionUID = 1L;
	static final int PORT = 1978;
	private JTextField textFieldForName = new JTextField();
	private String address = "localhost";
	public Ship refShip;
	public Navy navy;
	private int size = 10;
	private JLabel spacer = new JLabel("");
	private MyButton[][] mb = new MyButton[size][size];
	private MyButton[][] mb2 = new MyButton[size][size];
	private JButton findMatchButton = new JButton("Find match!");
	private JLabel msgBox = new JLabel("Message box");
	private HashMap<String, Color> colors = new HashMap<>();
	private Socket clientSocket = null;
	private DataOutputStream outToServer;
	private DataInputStream inFromServer;
	private boolean reset = false;

	public GUISinkingShips() throws HeadlessException {
		super();
		this.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		//factory = new GenericFactoryUsingReflection<>(MyButton.class);
		
		gbc.weightx = 0.5;
		gbc.weighty = 0.5;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 13;

		this.add(msgBox, gbc);
		this.setVisible(true);

		gbc.gridwidth = 5;
		gbc.fill = GridBagConstraints.NONE;
		textFieldForName.setPreferredSize(new Dimension(70, 30));

		this.add(textFieldForName, gbc);

		gbc.gridwidth = 3;
		findMatchButton.setPreferredSize(new Dimension(100, 50));
		findMatchButton.setEnabled(false);
		findMatchButton.addActionListener(findButtonListener);
		this.add(findMatchButton, gbc);
		this.setSize(1000, 500);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 1;
		setColors();

		drawOcean(mb, 0, 1, settingShipsListener, gbc);

		gbc.gridx = size;
		gbc.gridy = 1;
		gbc.gridheight = 10;
		add(spacer, gbc);

		drawOcean(mb2, 11, 1, sendingAttacksListener, gbc);

		setEnable(false, mb2);
	}

	void resetGui() {
		navy.resetNavy();
		setNextShip();
		try {
			outToServer.close();
			inFromServer.close();
			clientSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				mb[i][j].ship = null;
				mb[i][j].state = true;
				mb[i][j].respond = 0;
				mb[i][j].setBackground(colors.get(Colors.OCEAN.color()));
				mb[i][j].setEnabled(true);

				mb2[i][j].ship = null;
				mb2[i][j].state = true;
				mb2[i][j].respond = 0;
				mb2[i][j].setBackground(colors.get(Colors.OCEAN.color()));
				mb2[i][j].setEnabled(false);
			}
		}
	}

	ActionListener settingShipsListener = (e) -> {
		MyButton mbt = (MyButton) e.getSource();
		bindShipToTheButton(mbt);
		refShip.buildUp();
		if (refShip.built()) {
			shipWrapAround(mbt.ship);
			setNextShip();
		} else {
			posibleButtonsToSelect(mbt.ship);
			msgBox.setText("Choose field for ship (" + refShip.partsLeft + "/" + refShip.size + ")");
		}
	};

	ActionListener findButtonListener = (e) -> {
		if (reset) {
			resetGui();
			findMatchButton.setText("Find match!");
			findMatchButton.setEnabled(false);
			reset = false;
			textFieldForName.setEditable(true);
		} else
			try {
				textFieldForName.setEditable(false);
				byte[] name = new byte[8];
				JButton jb = (JButton) e.getSource();
				clientSocket = new Socket(address, PORT);
				outToServer = new DataOutputStream(clientSocket.getOutputStream());
				inFromServer = new DataInputStream(clientSocket.getInputStream());
				int res = inFromServer.readInt();

				name = textFieldForName.getText().trim().getBytes();
				outToServer.write(name);

				jb.setEnabled(false);

				if (res == 1)
				{
					setEnable(true, mb2);
					msgBox.setText("Your turn!");
				}
				else
					new Thread() {
						public void run() {
							underTheFire();
						}
					}.start();
			} catch (IOException e1) {
				System.err.println("Connection with server failed!");
			}

	};

	ActionListener sendingAttacksListener = (e) -> {
		MyButton mbt = (MyButton) e.getSource();

		if (!performAttack(mbt))
			new Thread() {
				public void run() {
					underTheFire();
				}
			}.start();
	};

	boolean performAttack(MyButton mbt) {
		int replay = 0;
		try {
			outToServer.writeInt(mbt.x);
			outToServer.writeInt(mbt.y);
			replay = inFromServer.readInt();
			System.out.println("client prima: " + replay);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (replay == Responds.MISS.code) {
			unsuccessfulAttack(mbt);
			setEnable(false, mb2);
			return false;
		} else {
			successfulAttack(mbt, mb2);
			mbt.respond = Responds.PARTOFTHESHIPDOWN.code;
			if (replay == Responds.SHIPDOWN.code)
				successfulAttackShipDown(mbt, mb2);
			if (replay == Responds.NAVYDOWN.code) {
				successfulAttackShipDown(mbt, mb2);
				try {
					gameOver(true);
				} catch (InterruptedException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	void underTheFire() {
		int x = 0;
		int y = 0;
		int replay = 0;
		MyButton mbt;

		msgBox.setText("Enamy turn!!");
		
		do {

			try {
				x = inFromServer.readInt();
				y = inFromServer.readInt();
				replay = didWeGotHit(x, y);
				outToServer.writeInt(replay);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			mbt = mb[x][y];

			if (replay == Responds.MISS.code) {
				unsuccessfulAttack(mbt);
			} else {
				disableButton(mbt, colors.get(Colors.ENEMYFIRE.color()));
				mbt.respond = Responds.PARTOFTHESHIPDOWN.code;
				if (replay == Responds.SHIPDOWN.code)
					disableButton(mbt, colors.get(Colors.ENEMYFIRE.color()));
				if (replay == Responds.NAVYDOWN.code) {
					disableButton(mbt, colors.get(Colors.ENEMYFIRE.color()));
					try {
						gameOver(false);
					} catch (InterruptedException | IOException e) {
						e.printStackTrace();
					}
					return;
				}
			}

		} while (replay > 0);
		setEnable(true, mb2);

		msgBox.setText("Your turn!");
	}

	void drawOcean(MyButton[][] mbt, int offsetX, int offsetY, ActionListener al, GridBagConstraints gbct) {
		gbct.gridheight = 1;
		gbct.gridwidth = 1;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				//using a generic factory class
				//mbt[i][j] = factory.createInstance(i, j);
				mbt[i][j] = new MyButton(i, j);
				mbt[i][j].setBackground(colors.get("ocean"));
				mbt[i][j].addActionListener(al);

				gbct.gridx = j + offsetX;
				gbct.gridy = i + offsetY;
				this.add(mbt[i][j], gbct);
			}
		}
	}

	public enum Responds {
		NAVYDOWN(-1), MISS(0), PARTOFTHESHIPDOWN(1), SHIPDOWN(2);

		int code;

		Responds(int c) {
			code = c;
		}

	}

	public enum Colors {
		SHIP("ship"), OCEANAROUNDSHIP("oceanAroundShip"), OCEAN("ocean"), ENEMYFIRE("enemyfire"), UNSUCCESSFULATTACK(
				"unsuccessfulAttack");

		private String color;

		Colors(String str) {
			color = str;
		}

		public String color() {
			return color;
		}
	}

	void gameOver(boolean weWon) throws InterruptedException, IOException {
		if (weWon)
			msgBox.setText("You won!");
		else
			msgBox.setText("You lost!");
		reset = true;
		findMatchButton.setEnabled(true);
		findMatchButton.setText("Reset");

	}

	void unsuccessfulAttack(MyButton mbt) {
		disableButton(mbt, colors.get(Colors.UNSUCCESSFULATTACK.color()));
	}

	void successfulAttackShipDown(MyButton mbt, MyButton[][] mbtt) {
		int x = mbt.x;
		int y = mbt.y;

		boolean xGreaterThenZero = x > 0;
		boolean yGreaterThenZero = y > 0;
		boolean xLowerThenLength = x < size - 1;
		boolean yLowerThenLength = y < size - 1;

		mbt.respond = -2;

		if (xGreaterThenZero)
			if (mbtt[x - 1][y].respond == Responds.PARTOFTHESHIPDOWN.code) {
				successfulAttackShipDown(mbtt[x - 1][y], mbtt);
				disableButton(mbtt[x - 1][y], colors.get(Colors.SHIP.color()));
			} else if (mbtt[x - 1][y].respond == Responds.MISS.code) {
				mbtt[x - 1][y].state = false;
				disableButton(mbtt[x - 1][y], colors.get(Colors.OCEANAROUNDSHIP.color()));
			}
		if (yGreaterThenZero)
			if (mbtt[x][y - 1].respond == Responds.PARTOFTHESHIPDOWN.code) {
				successfulAttackShipDown(mbtt[x][y - 1], mbtt);
				disableButton(mbtt[x][y - 1], colors.get(Colors.SHIP.color()));
			} else if (mbtt[x][y - 1].respond == Responds.MISS.code) {
				mbtt[x][y - 1].state = false;
				disableButton(mbtt[x][y - 1], colors.get(Colors.OCEANAROUNDSHIP.color()));
			}
		if (xLowerThenLength)
			if (mbtt[x + 1][y].respond == Responds.PARTOFTHESHIPDOWN.code) {
				successfulAttackShipDown(mbtt[x + 1][y], mbtt);
				disableButton(mbtt[x + 1][y], colors.get(Colors.SHIP.color()));
			} else if (mbtt[x + 1][y].respond == Responds.MISS.code) {
				mbtt[x + 1][y].state = false;
				disableButton(mbtt[x + 1][y], colors.get(Colors.OCEANAROUNDSHIP.color()));
			}
		if (yLowerThenLength)
			if (mbtt[x][y + 1].respond == Responds.PARTOFTHESHIPDOWN.code) {
				successfulAttackShipDown(mbtt[x][y + 1], mbtt);
				disableButton(mbtt[x][y + 1], colors.get(Colors.SHIP.color()));
			} else if (mbtt[x][y + 1].respond == Responds.MISS.code) {
				mbtt[x][y + 1].state = false;
				disableButton(mbtt[x][y + 1], colors.get(Colors.OCEANAROUNDSHIP.color()));
			}
	}

	public void successfulAttack(MyButton mbt, MyButton[][] mbtt) {
		int x = mbt.x;
		int y = mbt.y;

		boolean xGreaterThenZero = x > 0;
		boolean yGreaterThenZero = y > 0;
		boolean xLowerThenLength = x < size - 1;
		boolean yLowerThenLength = y < size - 1;

		disableButton(mbt, colors.get(Colors.SHIP.color()));

		if (xGreaterThenZero && yGreaterThenZero)
			disableButton(mbtt[x - 1][y - 1], colors.get("oceanAroundShip"));

		if (xLowerThenLength && yLowerThenLength)
			disableButton(mbtt[x + 1][y + 1], colors.get("oceanAroundShip"));

		if (xGreaterThenZero && yLowerThenLength)
			disableButton(mbtt[x - 1][y + 1], colors.get("oceanAroundShip"));

		if (xLowerThenLength && yGreaterThenZero)
			disableButton(mbtt[x + 1][y - 1], colors.get("oceanAroundShip"));
	}

	public int didWeGotHit(int x, int y) {
		mb[x][y].setBackground(colors.get(Colors.ENEMYFIRE.color()));
		if (mb[x][y].ship != null) {
			mb[x][y].ship.gotHit();
			if (navy.isNavyDown())
				return Responds.NAVYDOWN.code;
			if (mb[x][y].ship.isShipDown())
				return Responds.SHIPDOWN.code;
			return Responds.PARTOFTHESHIPDOWN.code;
		}
		return Responds.MISS.code;
	}

	void setColors() {
		colors.put("ocean", new Color(30, 144, 255));
		colors.put("ship", new Color(112, 128, 144));
		colors.put("oceanAroundShip", new Color(0, 0, 255));
		colors.put("enemyfire", new Color(0, 0, 0));
		colors.put("unsuccessfulAttack", new Color(0, 0, 128));
	}

	void setNextShip() {
		refShip = navy.nextShipToBuild();
		if (refShip == null) {
			findMatchButton.setEnabled(true);
			msgBox.setText("You can find match ->");
		} else {
			enableAllPossibleSelection();
			msgBox.setText("Choose field for ship (" + refShip.partsLeft + "/" + refShip.size + ")");
		}
	}

	public void bindShipToTheButton(MyButton mbt) {
		int x = mbt.x;
		int y = mbt.y;

		boolean xGreaterThenZero = x > 0;
		boolean yGreaterThenZero = y > 0;
		boolean xLowerThenLength = x < size - 1;
		boolean yLowerThenLength = y < size - 1;

		mb[x][y].ship = refShip;
		mb[x][y].state = false;
		mb[x][y].setEnabled(false);
		disableButton(mb[x][y], colors.get(Colors.SHIP.color()));

		if (!isPartOfTheShipNextToMe(mbt.ship, mbt)) {
			if (!canFitVertical(mb[x][y])) {
				if (xGreaterThenZero)
					disableButton(mb[x - 1][y], colors.get("oceanAroundShip"));
				if (xLowerThenLength)
					disableButton(mb[x + 1][y], colors.get("oceanAroundShip"));
			}
			if (!canFitHorizontal(mb[x][y])) {
				if (yGreaterThenZero)
					disableButton(mb[x][y - 1], colors.get("oceanAroundShip"));
				if (yLowerThenLength)
					disableButton(mb[x][y + 1], colors.get("oceanAroundShip"));
			}
		}

		if (xGreaterThenZero && yGreaterThenZero)
			disableButton(mb[x - 1][y - 1], colors.get("oceanAroundShip"));

		if (xLowerThenLength && yLowerThenLength)
			disableButton(mb[x + 1][y + 1], colors.get("oceanAroundShip"));

		if (xGreaterThenZero && yLowerThenLength)
			disableButton(mb[x - 1][y + 1], colors.get("oceanAroundShip"));

		if (xLowerThenLength && yGreaterThenZero)
			disableButton(mb[x + 1][y - 1], colors.get("oceanAroundShip"));
	}

	public void disableButton(MyButton mbt, Color c) {
		mbt.state = false;
		mbt.setBackground(c);
		mbt.setEnabled(false);
	}

	public boolean canFitHorizontal(MyButton mbt) {
		int space = 1;
		int x = mbt.x;
		int y = mbt.y;
		while (--y >= 0 && mb[x][y].state == true)
			space++;
		if (space >= this.refShip.size)
			return true;
		y = mbt.y;
		while (++y < size && mb[x][y].state == true)
			space++;
		if (space >= this.refShip.size)
			return true;
		return false;
	}

	public boolean canFitVertical(MyButton mbt) {
		int space = 1;
		int x = mbt.x;
		int y = mbt.y;
		while (--x >= 0 && mb[x][y].state == true)
			space++;
		if (space >= this.refShip.size)
			return true;
		x = mbt.x;
		while (++x < size && mb[x][y].state == true)
			space++;
		if (space >= this.refShip.size)
			return true;
		return false;
	}

	boolean isPartOfTheShipNextToMe(Ship ship, MyButton mbt) {
		int x = mbt.x;
		int y = mbt.y;

		boolean xGreaterThenZero = x > 0;
		boolean yGreaterThenZero = y > 0;
		boolean xLowerThenLength = x < size - 1;
		boolean yLowerThenLength = y < size - 1;

		if (xGreaterThenZero && mb[x - 1][y].ship != null && mb[x - 1][y].ship.equals(ship))
			return true;
		if (yGreaterThenZero && mb[x][y - 1].ship != null && mb[x][y - 1].ship.equals(ship))
			return true;
		if (xLowerThenLength && mb[x + 1][y].ship != null && mb[x + 1][y].ship.equals(ship))
			return true;
		if (yLowerThenLength && mb[x][y + 1].ship != null && mb[x][y + 1].ship.equals(ship))
			return true;
		return false;
	}

	public void setEnable(boolean enable, MyButton[][] mbt) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				mbt[i][j].setEnabled(enable && mbt[i][j].state);
			}
		}
	}

	public void enableAllPossibleSelection() {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (mb[i][j].state && (canFitHorizontal(mb[i][j]) || canFitVertical(mb[i][j])))
					mb[i][j].setEnabled(true);
			}
		}
	}

	public void posibleButtonsToSelect(Ship ship) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (mb[i][j].state == false || !isPartOfTheShipNextToMe(ship, mb[i][j])) {
					mb[i][j].setEnabled(false);
				} else
					mb[i][j].setEnabled(true);
			}
		}
	}

	public void shipWrapAround(Ship ship) {
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (mb[i][j].state == true && isPartOfTheShipNextToMe(ship, mb[i][j]) && mb[i][j].ship == null) {
					mb[i][j].setEnabled(false);
					disableButton(mb[i][j], colors.get("oceanAroundShip"));
				}
			}
		}
	}
}
