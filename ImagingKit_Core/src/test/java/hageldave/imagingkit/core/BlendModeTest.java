package hageldave.imagingkit.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class BlendModeTest {

	@Test
	public void testFunctions(){
		for(Blending mode: Blending.values()){
			/* input values range from 0 to 255, output values also have to satisfy that property */
			for(int a = 0; a < 0xff; a++){
				for(int b = 0; b < 0xff; b++){
					assertEquals(String.format("not 8bit: a=%d b=%d mode:%s", a,b,mode.name()), 
							0xff, 
							mode.blendFunction.blend(a, b) | 0xff);
				}
			}			
		}
	}
	
	@Test
	public void testOpacityThings(){
		int opaque = 0xff000000;
		int semi = 0x88000000;
		assertEquals("not top color", 
				opaque|0xaa, 
				Blending.blend(opaque|0xff, opaque|0xaa, Blending.NORMAL));
		assertEquals("not same result with visibility 1", 
				Blending.blend(opaque|0xff, opaque|0xaa, Blending.NORMAL), 
				Blending.alphaBlend(opaque|0xff, opaque|0xaa, 1,Blending.NORMAL));
		assertEquals("not bottom color with visibility 0",
				opaque|0xff, 
				Blending.alphaBlend(opaque|0xff, opaque|0xaa, 0,Blending.NORMAL));
		assertEquals("not bottom color with transparent top color at visibility 1",
				opaque|0xff,
				Blending.alphaBlend(opaque|0xff, 0|0xaa, 1, Blending.NORMAL));
		assertTrue("not brighter than darker color at visibility 0.5", 
				Pixel.getLuminance(opaque|0x44) < 
				Pixel.getLuminance(Blending.alphaBlend(opaque|0x44, opaque|0xff, 0.5f, Blending.NORMAL)));
		assertTrue("not darker than brighter color at visibility 0.5", 
				Pixel.getLuminance(opaque|0xff) > 
				Pixel.getLuminance(Blending.alphaBlend(opaque|0x44, opaque|0xff, 0.5f, Blending.NORMAL)));
		assertTrue("not brighter than darker color with 88 opacity", 
				Pixel.getLuminance(opaque|0x44) < 
				Pixel.getLuminance(Blending.alphaBlend(opaque|0x44, semi|0xff, 1, Blending.NORMAL)));
		assertTrue("not darker than brighter color with 88 opacity", 
				Pixel.getLuminance(opaque|0xff) > 
				Pixel.getLuminance(Blending.alphaBlend(opaque|0x44, semi|0xff, 1, Blending.NORMAL)));
		assertEquals("alpha not preserved",
				semi|0x67,
				Blending.blend(semi|0x34, opaque|0x67, Blending.NORMAL));
		
	}
	
	@Test
	public void testConsumers() {
		int opaqueBlack = 0xff000000;
		int opaqueWhite = 0xffffffff;
		
		Img imgB = new Img(10, 10);
		imgB.fill(opaqueBlack);
		Img imgT = imgB.copy();
		imgT.fill(opaqueWhite);
		
		// bottom is black top is white
		imgB.forEach(Blending.NORMAL.getBlendingWith(imgT));
		// bottom should be white now
		for(int c: imgB.getData()){
			assertEquals(opaqueWhite, c);
		}
		imgB.fill(opaqueBlack);
		// test offset
		imgB.forEach(Blending.NORMAL.getBlendingWith(imgT, 5, 5));
		imgB.forEach(px->{
			if(px.getX() >= 5 && px.getY() >= 5){
				assertEquals(opaqueWhite, px.getValue());
			} else {
				assertEquals(opaqueBlack, px.getValue());
			}
		});
		imgB.fill(opaqueBlack);
		// test negative offset
		imgB.forEach(Blending.NORMAL.getBlendingWith(imgT, -5, -5));
		imgB.forEach(px->{
			if(px.getX() < 5 && px.getY() < 5){
				assertEquals(opaqueWhite, px.getValue());
			} else {
				assertEquals(opaqueBlack, px.getValue());
			}
		});
		imgB.fill(opaqueBlack);
		// test visibility (transparency)
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWith(imgT, 0.5f));
		for(int c: imgB.getData()){
			assertTrue(c != opaqueBlack && c != opaqueWhite);
		}
		imgB.fill(opaqueBlack);
		// test visibility and positive and negative offset
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWith(imgT, -5, -5, 0.5f));
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWith(imgT,  5,  5, 0.5f));
		imgB.forEach(px->{
			if(px.getX() < 5 && px.getY() < 5){
				assertTrue(px.getValue() != opaqueBlack && px.getValue() != opaqueWhite);
			} else if(px.getX() >= 5 && px.getY() >= 5){
				assertTrue(px.getValue() != opaqueBlack && px.getValue() != opaqueWhite);
			} else {
				assertEquals(opaqueBlack, px.getValue());
			}
		});
		
	}
	
}
