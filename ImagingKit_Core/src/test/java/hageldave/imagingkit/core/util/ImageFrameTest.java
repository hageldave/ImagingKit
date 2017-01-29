package hageldave.imagingkit.core.util;

import org.junit.Test;

import hageldave.imagingkit.core.Img;

import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.LinkedList;

public class ImageFrameTest {

	@Test
	public void imagePanelTest() {
		Img img = new Img(100, 100);
		img.fill(0xff223344);
		
		ImagePanel panel = new ImagePanel();
		panel.setSize(100, 100);
		panel.setBackground(new Color(0xff445544));
		
		Img imgGraphics = new Img(panel.getSize());
		imgGraphics.paint(g2d->panel.paint(g2d));
		for(int i = 0; i < imgGraphics.numValues(); i++){
			assertEquals(0xff445544, imgGraphics.getData()[i]);
		}
		
		panel.enableCheckerboardBackground(!panel.isCheckerboardBackgroundEnabled());
		imgGraphics.paint(g2d->panel.paint(g2d));
		LinkedList<Integer> colors = new LinkedList<>();
		imgGraphics.forEach(px->{if(!colors.contains(px.getLuminance())){colors.add(px.getLuminance());}});
		assertEquals(2, colors.size());
		
		panel.setImg(img);
		imgGraphics.paint(g2d->panel.paint(g2d));
		for(int i = 0; i < img.numValues(); i++){
			assertEquals(img.getData()[i], imgGraphics.getData()[i]);
		}
		
		panel.setSize(90, 100);
		imgGraphics.paint(g2d->panel.paint(g2d));
		
		
		panel.setSize(150, 150);
		imgGraphics = new Img(panel.getSize());
		imgGraphics.paint(g2d->panel.paint(g2d));
		
		// Simulate Mouse Events
		panel.setSize(160, 160);
		
		MouseListener[] mouseListeners = panel.getMouseListeners();
		MouseMotionListener[] motionListeners = panel.getMouseMotionListeners();
		
		assertTrue(mouseListeners.length > 0 && motionListeners.length > 0);
		
		for(MouseMotionListener m: motionListeners){
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON1));
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON2));
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON3));
		}
		
		int x = -20;
		int y = -20;
		for(MouseListener m: mouseListeners){
			m.mousePressed(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON1));
			m.mousePressed(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON2));
			m.mousePressed(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON3));
		}
		imgGraphics.paint(g2d->panel.paint(g2d));
		
		x = 100;
		y = 100;
		for(MouseMotionListener m: motionListeners){
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON1));
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON2));
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON3));
		}
		imgGraphics.paint(g2d->panel.paint(g2d));
		
		x = 200;
		y = 200;
		for(MouseMotionListener m: motionListeners){
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON1));
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON2));
			m.mouseDragged(new MouseEvent(panel, 0, 0, 0, x, y, 1, false, MouseEvent.BUTTON3));
		}
		imgGraphics.paint(g2d->panel.paint(g2d));
		
		for(MouseListener m: mouseListeners){
			m.mouseReleased(new MouseEvent(panel, 0, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON1));
			m.mouseReleased(new MouseEvent(panel, 0, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON2));
			m.mouseReleased(new MouseEvent(panel, 0, 0, 0, 0, 0, 1, false, MouseEvent.BUTTON3));
		}
		
	}
	
	@Test
	public void imageFrameTest() {
		Img img1 = new Img(1, 1);
		Img img2 = new Img(2, 2);
		
		ImageFrame frame = ImageFrame.display(img1);
		
		Thread.yield();
		
		frame.setImg(img2);
		frame.getPanel().setBackground(Color.cyan);
		frame.dispose();
	}
	
}
