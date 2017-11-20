import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

//trying out a reflection
public class GenericFactoryUsingReflection<T> {

	private Class<T> theClass = null;

	public GenericFactoryUsingReflection(Class<T> theClass) {
		this.theClass = theClass;
	}
	
	//for passed max 5 integers it returns new instance of given class
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public T createInstance(int...varargs)
	{
		T returnValue = null;
		int numberOfParameter = varargs.length;
		Constructor<T>[] allDeclaredConstructors =  (Constructor<T>[]) theClass.getDeclaredConstructors();
		Constructor<T> construstor;
		boolean parameterFound = false;
		
		for (int i = 0; i < allDeclaredConstructors.length; i++) {
			Class[] paremeters = allDeclaredConstructors[i].getParameterTypes();
			if(paremeters.length == numberOfParameter)
			{
				parameterFound = true;
				for (int j = 0; j < paremeters.length; j++) {
					if(!paremeters[j].equals(int.class))
					{
						parameterFound = false;
						break;
					}
				}
				if(parameterFound)
				{
					//this is not good
					System.out.println("dads");
					construstor = allDeclaredConstructors[i];
					try {
						switch (numberOfParameter) {
						case 0:
							returnValue = construstor.newInstance();
							break;
						case 1:
							returnValue = construstor.newInstance(varargs[0]);
							break;
						case 2:
							returnValue = construstor.newInstance(varargs[0], varargs[1]);
							break;
						case 3:
							returnValue = construstor.newInstance(varargs[0], varargs[1], varargs[2]);
							break;
						case 4:
							returnValue = construstor.newInstance(varargs[0], varargs[1], varargs[2], varargs[3]);
							break;
						case 5:
							returnValue = construstor.newInstance(varargs[0], varargs[1], varargs[2], varargs[3], varargs[4]);
							break;
						default:
						{
							System.out.println("To many arg");
						}
							break;
						}
						
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
		return returnValue;
	}
	
}
