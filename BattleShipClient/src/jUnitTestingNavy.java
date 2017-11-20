import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class jUnitTestingNavy {

	@Test
	public void testBuildAndDestroyNavys()
	{
		int numberOfNavys = 10;
		
		//making navy's and ships for every navy
		Navy[] navys = new Navy[numberOfNavys];
		for (int i = 0; i < numberOfNavys; i++) {
			navys[i] = new Navy(i+1);
			for (int j = 0; j < navys[i].numberOFShips; j++) {
				while(!navys[i].ships[j].built())
					navys[i].ships[j].buildUp();
			}
		}
		
		//testing 
		for (int i = 0; i < numberOfNavys; i++) {
			assertFalse(navys[i].isNavyDown());
		}
		
		//simulating attacks on the ships
		for (int i = 0; i < numberOfNavys; i++) {
			int counter = 0;
			while(!navys[i].isNavyDown())
			{
				while(!navys[i].ships[counter].isShipDown())
					navys[i].ships[counter].gotHit();
				counter++;
			}
					
		}
		
		//testing
		for (int i = 0; i < numberOfNavys; i++) {
			assertTrue(navys[i].isNavyDown());
		}
	}
	
}
