package hageldave.imagingkit.core.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import static hageldave.imagingkit.core.JunitUtils.*;
import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.core.operations.Blending;

public class BlendModeTest {

	@Test
	public void testFunctions(){
		Img img0 = new Img(256, 256);
		Img img1 = img0.copy();
		img0.forEach(px->px.setRGB(0, px.getY(), px.getX()));
		img1.forEach(px->px.setRGB(0, px.getX(), px.getY()));
		Img img2;
		for(Blending mode: Blending.values()){
			/* input values range from 0 to 255, output values also have to satisfy that property */
			img2 = new Img(img0.getRemoteBufferedImage());
			img2.forEach(mode.getBlendingWith(img1));
			img2.forEach(px->{
				testWithMsg(()->assertEquals(0, px.r_asDouble(), 0),()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.g_asDouble() <= 1.0), ()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.b_asDouble() <= 1.0), ()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.g_asDouble() >= 0.0), ()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.b_asDouble() >= 0.0), ()->px.toString()+mode.name());
			});
		}
	}

	@Test
	public void testOpacityThings(){
		int opaque = 0xff000000;
		int semi = 0x88000000;
		Pixel top = new Img(1, 1).getPixel();
		Pixel bot = new Img(1, 1).getPixel();

		assertEquals("not top color",
				opaque|0xaa,
				Blending.blend(bot.setValue(opaque|0xff), top.setValue(opaque|0xaa), Blending.NORMAL).getValue());
		assertEquals("not same result with visibility 1",
				Blending.blend(     bot.setValue(opaque|0xff), top.setValue(opaque|0xaa),   Blending.NORMAL).getValue(),
				Blending.alphaBlend(bot.setValue(opaque|0xff), top.setValue(opaque|0xaa), 1,Blending.NORMAL).getValue());
		assertEquals("not bottom color with visibility 0",
				opaque|0xff,
				Blending.alphaBlend(bot.setValue(opaque|0xff), top.setValue(opaque|0xaa), 0,Blending.NORMAL).getValue());
		assertEquals("not bottom color with transparent top color at visibility 1",
				opaque|0xff,
				Blending.alphaBlend(bot.setValue(opaque|0xff), top.setValue(0|0xaa),      1,Blending.NORMAL).getValue());
		assertTrue("not brighter than darker color at visibility 0.5",
				Pixel.getLuminance(opaque|0x44) <
				Pixel.getLuminance(Blending.alphaBlend(bot.setValue(opaque|0x44), top.setValue(opaque|0xff), 0.5, Blending.NORMAL).getValue()));
		assertTrue("not darker than brighter color at visibility 0.5",
				Pixel.getLuminance(opaque|0xff) >
				Pixel.getLuminance(Blending.alphaBlend(bot.setValue(opaque|0x44), top.setValue(opaque|0xff), 0.5, Blending.NORMAL).getValue()));
		assertTrue("not brighter than darker color with 88 opacity",
				Pixel.getLuminance(opaque|0x44) <
				Pixel.getLuminance(Blending.alphaBlend(bot.setValue(opaque|0x44), top.setValue(semi|0xff),   1.0, Blending.NORMAL).getValue()));
		assertTrue("not darker than brighter color with 88 opacity",
				Pixel.getLuminance(opaque|0xff) >
				Pixel.getLuminance(Blending.alphaBlend(bot.setValue(opaque|0x44), top.setValue(semi|0xff),   1.0, Blending.NORMAL).getValue()));
		assertEquals("alpha not preserved",
				semi|0x67,
				Blending.blend(bot.setValue(semi|0x34), top.setValue(opaque|0x67), Blending.NORMAL).getValue());

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
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWith(imgT, 0.5));
		for(int c: imgB.getData()){
			assertTrue(c != opaqueBlack && c != opaqueWhite);
		}
		imgB.fill(opaqueBlack);
		// test visibility and positive and negative offset
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWith(imgT, -5, -5, 0.5));
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWith(imgT,  5,  5, 0.5));
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
