
public class Navy {

	Ship[] ships;
	int numberOFShips;
	int counter = 0;
	
	public Navy(int numberOFShips) {
		this.ships = new Ship[numberOFShips];
		this.numberOFShips = numberOFShips;
		//GenericFactoryUsingReflection<Ship> factory = new GenericFactoryUsingReflection<>(Ship.class);
		for (int i = 0; i < this.numberOFShips; i++) {
			//addShip(factory.createInstance(i+1));
			addShip(new Ship(i+1));
		}
	}
	
	boolean isNavyDown()
	{
		for (int i = 0; i < ships.length; i++) {
			if(!ships[i].isShipDown())
				return false;
		}
		return true;
	}
	
	public Ship nextShipToBuild()
	{
		for (Ship ship : ships) {
			if(!ship.built())
				return ship;
		}
		return null;
	}
	
	public void addShip(Ship ship)
	{
		ships[counter++] = ship;
	}
	
	public void resetNavy()
	{
		for (int i = 0; i < numberOFShips; i++) {
			ships[i].resetShip();
		}
	}
}
