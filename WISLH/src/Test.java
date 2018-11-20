import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import weka.classifiers.AbstractClassifier;
import weka.classifiers.lazy.IBk;
import weka.core.SelectedTag;
import weka.core.Tag;

public class Test {

	public static void main(String[] args) {
		
		try {
			
			//Class<?> c = Class.forName();
			
			Constructor<?> cstr = c.getConstructor();
			
			AbstractClassifier ac = (AbstractClassifier)cstr.newInstance();
			System.out.println(ac);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
