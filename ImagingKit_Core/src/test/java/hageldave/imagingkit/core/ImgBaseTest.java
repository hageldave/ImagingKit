package hageldave.imagingkit.core;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.Test;

import hageldave.imagingkit.core.img.AWT_Displayable;
import hageldave.imagingkit.core.img.ImgBase;
import hageldave.imagingkit.core.pixel.Pixel3;
import hageldave.imagingkit.core.pixel.Pixel4;

public class ImgBaseTest {
	
	@Test
	public void test() throws Exception {
		TestImg img = new TestImg();
		assertEquals(img.getWidth()*img.getHeight(), img.numValues());
		assertEquals(1024, img.getSpliteratorMinimumSplitSize());
		img.forEach(px->px.setValues(px.getIndex()%220,px.getIndex()%221,px.getIndex()%222).setValueCh3(255));
		img.forEach(px->assertEquals((double)px.getIndex()%220, px.getValueCh0(), 0));
	}

	
	private static class TestImg implements ImgBase<TestPixel>, AWT_Displayable {
		
		final double[][][] values;
		
		public TestImg() {
			values = new double[1024][512][4];
		}
		

		@Override
		public int getWidth() {
			return 512;
		}

		@Override
		public int getHeight() {
			return 1024;
		}
		
		@Override
		public int numChannels() {
			return 4;
		}

		@Override
		public double getValueAt(int ch, int x, int y) {
			return values[y][x][ch];
		}
		
		@Override
		public double getValueAtIndex(int ch, int idx) {
			return getValueAt(ch, idx%getWidth(), idx/getWidth());
		}
		
		@Override
		public TestImg setValueAt(int ch, int x, int y, double v) {
			values[y][x][ch]=v;
			return this;
		}
		
		@Override
		public TestImg setValueAtIndex(int ch, int idx, double v) {
			return setValueAt(ch, idx%getWidth(), idx/getWidth(), v);
		}
		
		@Override
		public TestPixel getPixel() {
			return new TestPixel(this);
		}

		@Override
		public TestPixel getPixel(int x, int y) {
			return getPixel().setPosition(x, y);
		}

		@Override
		public BufferedImage toBufferedImage(BufferedImage bimg) {
			this.forEach(px->bimg.setRGB(px.x, px.y, Pixel.argb_bounded((int)px.getValueCh3(),(int)px.getValueCh0(),(int)px.getValueCh1(),(int)px.getValueCh2())));
			return bimg;
		}

		@Override
		public ImgBase<TestPixel> copy() {
			TestImg testImg = new TestImg();
			for(int r = 0; r < getHeight(); r++){
				for(int c = 0; c < getWidth(); c++)
					System.arraycopy(values[r][c], 0, testImg.values[r][c], 0, 4);
			}
			return testImg;
		}
		
	}
	
	private static class TestPixel implements Pixel3<TestPixel>, Pixel4<TestPixel> {

		int x,y;
		TestImg source;
		
		public TestPixel(TestImg source) {
			this.source = source;
		}
		
		@Override
		public int numChannels() {
			return 4;
		}
		
		@Override
		public double getValue(int ch) {
			return channelAsDouble(ch);
		}
		
		@Override
		public TestPixel setValue(int ch, double v) {
			return setChannel(ch, v);
		}
		
		private double channelAsDouble(int c){
			return getSource().values[y][x][c];
		}
		
		private TestPixel setChannel(int c, double v){
			getSource().values[y][x][c]=v;
			return this;
		}
		
		@Override
		public double getValueCh3() {
			return channelAsDouble(0);
		}

		@Override
		public double getValueCh0() {
			return channelAsDouble(1);
		}

		@Override
		public double getValueCh1() {
			return channelAsDouble(2);
		}

		@Override
		public double getValueCh2() {
			return channelAsDouble(3);
		}

		@Override
		public TestPixel setValueCh3(double a) {
			return setChannel(0, a);
		}

		@Override
		public TestPixel setValueCh0(double r) {
			return setChannel(1, r);
		}

		@Override
		public TestPixel setValueCh1(double g) {
			return setChannel(2, g); 
		}

		@Override
		public TestPixel setValueCh2(double b) {
			return setChannel(3, b);
		}

		@Override
		public int getX() {
			return x;
		}

		@Override
		public int getY() {
			return y;
		}

		@Override
		public int getIndex() {
			return y * getSource().getWidth()+ x;
		}

		@Override
		public TestPixel setIndex(int index) {
			return setPosition(index % getSource().getWidth(), index/getSource().getWidth());
		}

		@Override
		public TestPixel setPosition(int x, int y) {
			this.x = x;
			this.y = y;
			return this;
		}

		@Override
		public TestImg getSource() {
			return source;
		}
		
	}
	
}
