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
		assertEquals(5, settings.getAs(id, Integer.class));
		assertEquals(null, settings.getAs(id, String.class));
		assertEquals("hello", settings.getAs(id, String.class, "hello"));
		settings.clear(id);
		assertFalse(settings.containsSetting(id));
		assertEquals(0, settings.getSettingIds().size());
		assertEquals(null, settings.get(id));
	}
	
	@Test
	public void testTypeConstraint(){
		String id1 = "testSetting1";
		String id2 = "testSetting2";
		{
			FilterSettings settings = new FilterSettings(true);
			settings.set(id1, 5);
			settings.set(id2, 8);
			assertEquals(0, settings.getSettingIds().size());
			assertEquals(null, settings.get(id1));
			assertEquals(null, settings.get(id1));
		}
		{
			testException(()->
			{
				FilterSettings settings = new FilterSettings(new Object[]{id1});
			}, IllegalArgumentException.class);
			testException(()->
			{
				FilterSettings settings = new FilterSettings(new Object[]{id1,id2});
			}, IllegalArgumentException.class);
			testException(()->
			{
				FilterSettings settings = new FilterSettings(new Object[]{id1,5});
			}, IllegalArgumentException.class);
			testException(()->
			{
				FilterSettings settings = new FilterSettings(new Object[]{id1,Object.class, id1, Integer.class});
			}, IllegalArgumentException.class);
			//---------------------------------
			FilterSettings settings = new FilterSettings(new Object[]{id1, Integer.class, id2, null});
			assertEquals(true, settings.isTypeConstrained(id1));
			assertEquals(true, settings.isTypeConstrained(id2));
			assertEquals(Integer.class, settings.getTypeConstraint(id1));
			assertEquals(Object.class, settings.getTypeConstraint(id2));
			testException(()->
			{
				settings.set(id1, "stringvalue");
			}, IllegalArgumentException.class);
			settings.set(id2, "stringvalue");
			assertEquals(String.class, settings.get(id2).getClass());
			settings.set(id1, 5);
			assertEquals(5, settings.get(id1));
		}
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
