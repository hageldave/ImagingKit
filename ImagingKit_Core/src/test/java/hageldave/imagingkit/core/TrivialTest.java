package hageldave.imagingkit.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import hageldave.imagingkit.core.io.ImageLoader;
import hageldave.imagingkit.core.io.ImageSaver;
import hageldave.imagingkit.core.util.BufferedImageFactory;
import hageldave.imagingkit.core.util.ImagingKitUtils;

/*
 * These tests are only for raising coverage on trivial things like static initializers
 * 
 */
public class TrivialTest {

	
	@Test
	public void test(){
		callPrivateConstructorIfPresent(Iterators.class);
		callPrivateConstructorIfPresent(ImageLoader.class);
		callPrivateConstructorIfPresent(ImageSaver.class);
		callPrivateConstructorIfPresent(BufferedImageFactory.class);
		callPrivateConstructorIfPresent(ImagingKitUtils.class);
	}

	static <T> void callPrivateConstructorIfPresent(Class<T> clazz){
		try{
			Constructor<T> noArgsConstructor = clazz.getDeclaredConstructor();
			if(!noArgsConstructor.isAccessible()){
				noArgsConstructor.setAccessible(true);
				try {
					noArgsConstructor.newInstance();
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
				{
					e.printStackTrace();
				}
				noArgsConstructor.setAccessible(false);
			}
		} catch(NoSuchMethodException e){}
	}
	
}
