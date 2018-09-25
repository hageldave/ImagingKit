package hageldave.imagingkit.fourier;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.Dimension;

import org.junit.Test;

import hageldave.imagingkit.core.scientific.ColorImg;

public class ComplexImgTest {

	@Test
	public void test() {
		ComplexImg img = new ComplexImg(new Dimension(100, 100));
		img.enableSynchronizePowerSpectrum(true);
		assertEquals(true, img.isSynchronizePowerSpectrum());
		img.setComplex(1, 1, 2, 4);
		assertEquals(2, img.getValueR(1, 1), 0);
		assertEquals(4, img.getValueI(1, 1), 0);
		assertEquals(2*2+4*4, img.getValueP(1, 1), 0);
		img.setValueI(1, 1, 0);
		assertEquals(0, img.getValueI(1, 1, ColorImg.boundary_mode_zero), 0);
		assertEquals(2*2, img.getValueP(1, 1, ColorImg.boundary_mode_zero), 0);
		img.setValueR(1, 1, -2);
		assertEquals(2*2, img.getValueP(1, 1, ColorImg.boundary_mode_zero), 0);
		assertEquals(-2, img.getValueR(1, 1, ColorImg.boundary_mode_zero), 0);
		assertEquals(2*2, img.computePower(1, 1), 0);
		assertEquals(Math.PI, img.computePhase(1, 1), 0);
		// call spectrum images for coverage (was visually assessed for correctnes)
		img.getPowerSpectrumImg();
		img.getPhaseSpectrumImg();
		img.getPowerPhaseSpectrumImg();
		img.toBufferedImage();
		if(img.supportsRemoteBufferedImage())
			img.getRemoteBufferedImage();
		// test shift stuff
		img.setComplex(0, 0, 3, 4);
		assertEquals(3, img.getDCreal(),0);
		img.shiftCornerToCenter();
		assertEquals(3, img.getDCreal(),0);
		assertEquals(50, img.getCurrentXshift());
		assertEquals(50, img.getCurrentYshift());
		assertEquals(3, img.getValueR(50, 50),0);
		assertEquals(4, img.getValueI(50, 50),0);
		img.setComplex(0, 0, 1, 1);
		img.resetShift();
		assertEquals(2, img.getValueP(50, 50), 0);
		// test filling
		img.fill(ComplexImg.CHANNEL_REAL, -3);
		img.fill(ComplexImg.CHANNEL_IMAG, -4);
		for(double v:img.getDataReal())
			assertEquals(-3, v,0);
		for(double v:img.getDataImag())
			assertEquals(-4, v,0);
		for(double v:img.getDataPower())
			assertEquals(3*3+4*4, v, 0);
		// test min max
		img.setComplex(4, 4, 10, -10);
		img.setComplex(9, 9, -10, 10);
		assertEquals(-10, img.getMinValue(ComplexImg.CHANNEL_REAL),0);
		assertEquals(10, img.getMaxValue(ComplexImg.CHANNEL_IMAG),0);
		assertEquals(4, img.getIndexOfMaxValue(ComplexImg.CHANNEL_REAL)%img.getWidth());
		assertEquals(9, img.getIndexOfMinValue(ComplexImg.CHANNEL_REAL)%img.getWidth());
		// test copying
		ComplexImg copy = img.copy();
		for(int y=0;y<img.getHeight();y++)
			for(int x=0;x<img.getWidth();x++)
				assertEquals(
						img.getValue(ComplexImg.CHANNEL_POWER, x, y), 
						copy.getValue(ComplexImg.CHANNEL_POWER, -(x+1), -(y+1), ColorImg.boundary_mode_mirror),
						0
				);
		// reset copy's real and imaginary to zero
		copy.fill(0, 0);
		copy.fill(1, 0);
		// enable synchronization
		copy.enableSynchronizePowerSpectrum(true);
		// set img power to zero
		img.enableSynchronizePowerSpectrum(false);
		img.fill(ComplexImg.CHANNEL_POWER, 0);
		img.copyArea(0, 0, 10, 10, copy, 0, 0);
		for(int y=0;y<img.getHeight();y++){
			for(int x=0;x<img.getWidth();x++){
				if(x<10&&y<10){
					assertEquals(
						img.computePower(x, y), 
						copy.getValue(ComplexImg.CHANNEL_POWER, x, y),
						0
					);
				} else {
					assertEquals(0, copy.getValue(ComplexImg.CHANNEL_POWER, x, y),0);
				}
			}
		}
	}
	
}
