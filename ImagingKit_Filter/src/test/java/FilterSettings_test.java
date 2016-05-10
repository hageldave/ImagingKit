import org.junit.Test;
import static org.junit.Assert.*;

import hageldave.imagingkit.filter.settings.FilterSettings;

public class FilterSettings_test {

	@Test
	public void testSetGet(){
		String id = "testSetting";
		FilterSettings settings = new FilterSettings();
		assertFalse(settings.containsSetting(id));
		assertEquals(0, settings.getSettingIds().size());
		assertEquals(null, settings.get(id));
		assertEquals(4, settings.get(id, 4));
		
		settings.set(id, 5);
		assertTrue(settings.containsSetting(id));
		assertEquals(1, settings.getSettingIds().size());
		assertEquals(5, settings.get(id));
		assertEquals(5, (int)settings.getAs(id, Integer.class));
		assertEquals(null, settings.getAs(id, String.class));
		assertEquals("hello", settings.getAs(id, String.class, "hello"));
		settings.clear(id);
		assertFalse(settings.containsSetting(id));
		assertEquals(0, settings.getSettingIds().size());
		assertEquals(null, settings.get(id));
		testException(()->{settings.set(null,null);}, NullPointerException.class);
		testException(()->{settings.set(id,null);}, NullPointerException.class);
		testException(()->{settings.set(null,5);}, NullPointerException.class);
	}
	
	@Test
	public void testTypeConstraint(){
//		String id1 = "testSetting1";
//		String id2 = "testSetting2";
		
		// TODO
	}
	
	public static void testException(Runnable codeThatThrows, Class<? extends Throwable> exClass){
		boolean wasThrown = true;
		try{
			codeThatThrows.run();
			wasThrown = false;
		} catch(Throwable t){
			if(!exClass.isInstance(t)){
				fail(String.format("Expected Exception %s but got %s", exClass, t.getClass()));
			}
		}
		if(!wasThrown){
			fail(String.format("Expected Exception %s but none was thrown",exClass));
		}
	}
	
}
