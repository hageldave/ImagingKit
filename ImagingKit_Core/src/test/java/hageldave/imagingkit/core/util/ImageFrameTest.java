//package hageldave.imagingkit.core.util;
//
//import org.junit.Test;
//
//import hageldave.imagingkit.core.Img;
//import hageldave.imagingkit.core.util.ImageFrame.ImagePanel;
//
//import static org.junit.Assert.*;
//
//import java.awt.Color;
//import java.util.LinkedList;
//
//public class ImageFrameTest {
//
//	@Test
//	public void imagePanelTest() {
//		Img img = new Img(100, 100);
//		img.fill(0xff223344);
//		
//		ImagePanel panel = new ImagePanel();
//		panel.setSize(100, 100);
//		panel.setBackground(new Color(0xff445544));
//		
//		Img imgGraphics = new Img(panel.getSize());
//		imgGraphics.paint(g2d->panel.paint(g2d));
//		for(int i = 0; i < imgGraphics.numValues(); i++){
//			assertEquals(0xff445544, imgGraphics.getData()[i]);
//		}
//		
//		panel.enableCheckerboardBackground(true);
//		imgGraphics.paint(g2d->panel.paint(g2d));
//		LinkedList<Integer> colors = new LinkedList<>();
//		imgGraphics.forEach(px->{if(!colors.contains(px.getLuminance())){colors.add(px.getLuminance());}});
//		assertEquals(2, colors.size());
//		
//		panel.setImg(img);
//		imgGraphics.paint(g2d->panel.paint(g2d));
//		for(int i = 0; i < img.numValues(); i++){
//			assertEquals(img.getData()[i], imgGraphics.getData()[i]);
//		}
//		
//		panel.setSize(90, 100);
//		imgGraphics.paint(g2d->panel.paint(g2d));
//		
//		
//		panel.setSize(150, 150);
//		imgGraphics = new Img(panel.getSize());
//		imgGraphics.paint(g2d->panel.paint(g2d));
//		
//		
//		
//	}
//	
//}
