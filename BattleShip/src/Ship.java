

public class Ship {

	int size;
	int partsLeft;
	
	public Ship(int size){
		this.size = size;
		this.partsLeft = 0;
	}
	
	public Ship(){}
	
	void gotHit()
	{
		partsLeft--;
	}
	
	void buildUp()
	{
		partsLeft++;
	}
	
	boolean built()
	{
		return size == partsLeft;
	}
	
	boolean isShipDown()
	{
		return partsLeft == 0;
	}
	
	void resetShip()
	{
		partsLeft = 0;
	}
	
}
