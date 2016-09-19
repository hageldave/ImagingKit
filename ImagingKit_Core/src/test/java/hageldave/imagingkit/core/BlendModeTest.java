//package hageldave.imagingkit.core;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertTrue;
//import org.junit.Test;
//
//public class BlendModeTest {
//
//	@Test
//	public void testFunctions(){
//		for(BlendMode mode: BlendMode.values()){
//			/* input values range from 0 to 255, output values also have to satisfy that property */
//			for(int a = 0; a < 0xff; a++){
//				for(int b = 0; b < 0xff; b++){
//					assertEquals(String.format("not 8bit: a=%d b=%d mode:%s", a,b,mode.name()), 
//							0xff, 
//							mode.blendFunction.blend(a, b) | 0xff);
//				}
//			}			
//		}
//	}
//	
//	@Test
//	public void testOpacityThings(){
//		int opaque = 0xff000000;
//		int semi = 0x88000000;
//		assertEquals("not top color", 
//				opaque|0xaa, 
//				BlendMode.blend(opaque|0xff, opaque|0xaa, BlendMode.NORMAL));
//		assertEquals("not same result with visibility 1", 
//				BlendMode.blend(opaque|0xff, opaque|0xaa, BlendMode.NORMAL), 
//				BlendMode.blend(opaque|0xff, opaque|0xaa, 1,BlendMode.NORMAL));
//		assertEquals("not bottom color with visibility 0",
//				opaque|0xff, 
//				BlendMode.blend(opaque|0xff, opaque|0xaa, 0,BlendMode.NORMAL));
//		assertEquals("not bottom color with transparent top color at visibility 1",
//				opaque|0xff,
//				BlendMode.blend(opaque|0xff, 0|0xaa, 1, BlendMode.NORMAL));
//		assertTrue("not brighter than darker color at visibility 0.5", 
//				Pixel.getLuminance(opaque|0x44) < 
//				Pixel.getLuminance(BlendMode.blend(opaque|0x44, opaque|0xff, 0.5f, BlendMode.NORMAL)));
//		assertTrue("not darker than brighter color at visibility 0.5", 
//				Pixel.getLuminance(opaque|0xff) > 
//				Pixel.getLuminance(BlendMode.blend(opaque|0x44, opaque|0xff, 0.5f, BlendMode.NORMAL)));
//		assertTrue("not brighter than darker color with 88 opacity", 
//				Pixel.getLuminance(opaque|0x44) < 
//				Pixel.getLuminance(BlendMode.blend(opaque|0x44, semi|0xff, 1, BlendMode.NORMAL)));
//		assertTrue("not darker than brighter color with 88 opacity", 
//				Pixel.getLuminance(opaque|0xff) > 
//				Pixel.getLuminance(BlendMode.blend(opaque|0x44, semi|0xff, 1, BlendMode.NORMAL)));
//		
//	}
//	
//	@Test
//	public void testConsumers() {
//		
//	}
//	
//}
