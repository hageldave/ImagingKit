package hageldave.imagingkit.core.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import static hageldave.imagingkit.core.JunitUtils.*;
import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.Pixel;
import hageldave.imagingkit.core.operations.Blending;

public class BlendModeTest {
	
	static final int A=3;
	static final int[] RGB = {0,1,2};

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
			img2.forEach(mode.getBlendingWith(img1, RGB));
			img2.forEach(px->{
				testWithMsg(()->assertEquals(0, px.getValueCh0(), 0),()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.getValueCh1() <= 1.0), ()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.getValueCh2() <= 1.0), ()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.getValueCh1() >= 0.0), ()->px.toString()+mode.name());
				testWithMsg(()->assertTrue(px.getValueCh2() >= 0.0), ()->px.toString()+mode.name());
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
				Blending.blend(bot.setPackedARGB(opaque|0xff), top.setPackedARGB(opaque|0xaa), Blending.NORMAL,RGB).getPackedARGB());
		assertEquals("not same result with visibility 1",
				Blending.blend(     bot.setPackedARGB(opaque|0xff), top.setPackedARGB(opaque|0xaa),   Blending.NORMAL,RGB).getPackedARGB(),
				Blending.alphaBlend(bot.setPackedARGB(opaque|0xff), top.setPackedARGB(opaque|0xaa), 1,Blending.NORMAL,A,RGB).getPackedARGB());
		assertEquals("not bottom color with visibility 0",
				opaque|0xff,
				Blending.alphaBlend(bot.setPackedARGB(opaque|0xff), top.setPackedARGB(opaque|0xaa), 0,Blending.NORMAL,A,RGB).getPackedARGB());
		assertEquals("not bottom color with transparent top color at visibility 1",
				opaque|0xff,
				Blending.alphaBlend(bot.setPackedARGB(opaque|0xff), top.setPackedARGB(0|0xaa),      1,Blending.NORMAL,A,RGB).getPackedARGB());
		assertTrue("not brighter than darker color at visibility 0.5",
				Pixel.getLuminance(opaque|0x44) <
				Pixel.getLuminance(Blending.alphaBlend(bot.setPackedARGB(opaque|0x44), top.setPackedARGB(opaque|0xff), 0.5, Blending.NORMAL,A,RGB).getPackedARGB()));
		assertTrue("not darker than brighter color at visibility 0.5",
				Pixel.getLuminance(opaque|0xff) >
				Pixel.getLuminance(Blending.alphaBlend(bot.setPackedARGB(opaque|0x44), top.setPackedARGB(opaque|0xff), 0.5, Blending.NORMAL,A,RGB).getPackedARGB()));
		assertTrue("not brighter than darker color with 88 opacity",
				Pixel.getLuminance(opaque|0x44) <
				Pixel.getLuminance(Blending.alphaBlend(bot.setPackedARGB(opaque|0x44), top.setPackedARGB(semi|0xff),   1.0, Blending.NORMAL,A,RGB).getPackedARGB()));
		assertTrue("not darker than brighter color with 88 opacity",
				Pixel.getLuminance(opaque|0xff) >
				Pixel.getLuminance(Blending.alphaBlend(bot.setPackedARGB(opaque|0x44), top.setPackedARGB(semi|0xff),   1.0, Blending.NORMAL,A,RGB).getPackedARGB()));
		assertEquals("alpha not preserved",
				semi|0x67,
				Blending.blend(bot.setPackedARGB(semi|0x34), top.setPackedARGB(opaque|0x67), Blending.NORMAL, RGB).getPackedARGB());

	}

	@Test
	public void testConsumers() {
		int opaqueBlack = 0xff000000;
		int opaqueWhite = 0xffffffff;

		Img imgB = new Img(10, 10);
		imgB.fillARGB(opaqueBlack);
		Img imgT = imgB.copy();
		imgT.fillARGB(opaqueWhite);

		// bottom is black top is white
		imgB.forEach(Blending.NORMAL.getBlendingWith(imgT,RGB));
		// bottom should be white now
		for(int c: imgB.getData()){
			assertEquals(opaqueWhite, c);
		}
		imgB.fillARGB(opaqueBlack);
		// test offset
		imgB.forEach(Blending.NORMAL.getBlendingWithOffset(imgT, 5, 5, RGB));
		imgB.forEach(px->{
			if(px.getX() >= 5 && px.getY() >= 5){
				assertEquals(opaqueWhite, px.getPackedARGB());
			} else {
				assertEquals(opaqueBlack, px.getPackedARGB());
			}
		});
		imgB.fillARGB(opaqueBlack);
		// test negative offset
		imgB.forEach(Blending.NORMAL.getBlendingWithOffset(imgT, -5, -5, RGB));
		imgB.forEach(px->{
			if(px.getX() < 5 && px.getY() < 5){
				assertEquals(opaqueWhite, px.getPackedARGB());
			} else {
				assertEquals(opaqueBlack, px.getPackedARGB());
			}
		});
		imgB.fillARGB(opaqueBlack);
		// test visibility (transparency)
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWith(imgT, 0.5, A,RGB));
		for(int c: imgB.getData()){
			assertTrue(c != opaqueBlack && c != opaqueWhite);
		}
		imgB.fillARGB(opaqueBlack);
		// test visibility and positive and negative offset
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWithOffset(imgT, -5, -5, 0.5, A,RGB));
		imgB.forEach(Blending.NORMAL.getAlphaBlendingWithOffset(imgT,  5,  5, 0.5, A,RGB));
		imgB.forEach(px->{
			if(px.getX() < 5 && px.getY() < 5){
				assertTrue(px.getPackedARGB() != opaqueBlack && px.getPackedARGB() != opaqueWhite);
			} else if(px.getX() >= 5 && px.getY() >= 5){
				assertTrue(px.getPackedARGB() != opaqueBlack && px.getPackedARGB() != opaqueWhite);
			} else {
				assertEquals(opaqueBlack, px.getPackedARGB());
			}
		});

	}

}
