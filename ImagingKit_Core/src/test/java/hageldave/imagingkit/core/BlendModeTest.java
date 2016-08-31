//package hageldave.imagingkit.core;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotEquals;
//import static org.junit.Assert.assertTrue;
//import org.junit.Test;
//
//public class BlendModeTest {
//
//	@Test
//	public void testFunctions(){
//		/* input values range from 0 - 255, output values also have to satisfy that property */
//		for(BlendMode mode: BlendMode.values()){
//			for(int a = 0; a < 0xff; a++){
//				for(int b = 0; b < 0xff; b++){
//					assertEquals(String.format("not 8bit: a=%d b=%d mode:%s", a,b,mode.name()), 0xff, mode.blendFunction.blend(a, b) | 0xff);
//					float[] visibilities = {0.999f, 0.8f, 2/3.0f, 0.5f, (float)(1/Math.PI), 0.2f, 0.001f};
//					int alpha = 0xff000000;
//					for(float vis: visibilities){
//						assertEquals(String.format("not 8bit: a=%d b=%d v:%f mode:%s", a,b,vis,mode.name()), 0xff, BlendMode.blend(a, b, vis, mode) | 0xff);
//					}
//					assertEquals(String.format("visibility 1 not equal to raw blendfunction. a=%d b=%d mode:%s", a,b,mode.name()), mode.blendFunction.blend(a, b), BlendMode.blend(alpha|a, alpha|b, 1, mode));
//					assertEquals(String.format("visibility 0 not equal to bottom color. a=%d b=%d mode:%s", a,b,mode.name()), a, BlendMode.blend(alpha|a, alpha|b, 0, mode));
//				}
//			}
//		}
//	}
//	
//}
