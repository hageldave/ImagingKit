# ImagingKit
[![Maven Central](https://img.shields.io/maven-central/v/com.github.hageldave.imagingkit/imagingkit-core.svg)](http://search.maven.org/#artifactdetails|com.github.hageldave.imagingkit|imagingkit-core|1.3|jar)
[![Build Status](https://travis-ci.org/hageldave/ImagingKit.svg?branch=master)](https://travis-ci.org/hageldave/ImagingKit)
[![Coverage Status](https://coveralls.io/repos/github/hageldave/ImagingKit/badge.svg?branch=master)](https://coveralls.io/github/hageldave/ImagingKit?branch=master)

A Java library for imaging tasks that integrates well with the commonly used java.awt.image environment (especially well with TYPE_INT BufferedImages). Its goal is to make image processing more convenient and to ease performance optimization. The library is intended for images using integer typed values like 24bit RGB or 32bit ARGB. 

So far the *ImagingKit-Core* artifact of the library is available through the maven central repository:
```xml
<dependency>
    <groupId>com.github.hageldave.imagingkit</groupId>
    <artifactId>imagingkit-core</artifactId>
    <version>1.4</version>
</dependency>
```

As this library aims at convenience and ease of use, look at this code snippet for grayscale conversion as a teaser:
```java
Img img = ImageLoader.loadImgFromURL("file:///home/pictures/rainbow.jpg");
for(Pixel px: img){
    int grey = (px.r() + px.g() + px.b()) / 3;
    px.setRGB(grey, grey, grey);
}
```
And now for the parallel processing part:
```java
img.parallelStream().forEach( px -> {
    int grey = px.getLuminance();
    px.setRGB(grey, grey, grey);
});
```


--
### Code Examples
Convert an image to grayscale:
```java
BufferedImage buffimg = ImageLoader.loadImage("myimage_colored.png", BufferedImage.TYPE_INT_ARGB);
Img img = Img.createRemoteImg(buffimg);
img.forEachParallel((pixel) -> {
	int gray = (pixel.r() + pixel.g() + pixel.b())/3;
	pixel.setARGB(pixel.a(), gray, gray, gray);
});
ImageSaver.saveImage(buffimg,"myimage_grayscale.png");
```
Draw into image (using java.awt.Graphics2D):
```java
Img img = new Img(400, 300);
img.fill(0xff000000);
img.paint(g2d -> {
	g2d.setColor(Color.white);
	String helloWorld = "Hello World";
	int textWidth = g2d.getFontMetrics().stringWidth(helloWorld);
	g2d.drawString(helloWorld, img.getWidth()/2-textWidth/2, img.getHeight()/2);
	g2d.drawLine(0, img.getHeight()/2+4, img.getWidth(), img.getHeight()/2+4);
});
ImageSaver.saveImage(img.getRemoteBufferedImage(), "hello_world.png");
```
Fancy polar color thing:
```java
Img img = new Img(1024, 1024);
img.forEach(px -> {
	double x = (px.getX()-512)/512.0;
	double y = (px.getY()-512)/512.0;
	double len = Math.max(Math.abs(x),Math.abs(y));
	double angle = (Math.atan2(x,y)+Math.PI)*(180/Math.PI);
	
	double r = 255*Math.max(0,1-Math.abs((angle-120)/120.0));
	double g = 255*Math.max(0, 1-Math.abs((angle-240)/120.0));
	double b = 255*Math.max(0, angle <= 120 ? 
			1-Math.abs((angle)/120.0):1-Math.abs((angle-360)/120.0));
	
	px.setRGB((int)(r*(1-len)), (int)(g*(1-len)), (int)(b*(1-len)));
});
ImageSaver.saveImage(img.getRemoteBufferedImage(), "polar_colors.png");
```
Shifting hue (using color space transformation):
```java
Img img = ImageLoader.loadImgFromURL("http://sipi.usc.edu/database/preview/misc/4.2.04.png");

img.forEach(ColorSpaceTransformation.RGB_2_HSV.get());
int hueShift = (int)((360-30) * (256.0f/360.0f));
img.forEach(pixel -> {
	// R channel corresponds to hue
	pixel.setR((pixel.r()+hueShift));
});
img.forEach(ColorSpaceTransformation.HSV_2_RGB.get());

ImageSaver.saveImage(img.getRemoteBufferedImage(), "lena_hue_shift.png");
```
Swing framebuffer rendering:
```java
BiConsumer<Pixel, Long> shader = (px, time)->{
	px.setRGB(
		(int)(px.getXnormalized()*255)+(int)((time/10)%255), 
		(int)(px.getYnormalized()*255), 
		(int)(px.getYnormalized()*255));		
	ColorSpaceTransformation.HSV_2_RGB.get().accept(px);
};

Img img = new Img(160, 90); 
BufferedImage bimg = img.getRemoteBufferedImage();
JPanel canvas = new JPanel(){ public void paint(Graphics g) { 
	long now = System.currentTimeMillis();
	img.forEachParallel(px->{shader.accept(px, now);});
	g.drawImage(bimg, 0,0,getWidth(),getHeight(), 0,0,img.getWidth(),img.getHeight(), null);

	String shaderTime = String.format("%02dms",System.currentTimeMillis()-now);
	g.drawString(shaderTime, 2, getHeight()); g.setColor(Color.white);
	g.drawString(shaderTime, 1, getHeight()-1);
}};
canvas.setPreferredSize(img.getDimension());
JFrame f = new JFrame("IMG"); f.setContentPane(canvas); 
f.pack(); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
SwingUtilities.invokeLater(()->{f.setVisible(true);});

final long fpsLimitTime = 1000/25; // 25fps
long target = System.currentTimeMillis()+fpsLimitTime;
while(true){
	long now = System.currentTimeMillis();
	canvas.repaint();
	Thread.sleep(Math.max(0, target-now));
	target = now+fpsLimitTime;
}
```
