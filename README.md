# ImagingKit
#### [Master Branch](https://github.com/hageldave/ImagingKit/tree/master)
[![Build Status](https://travis-ci.org/hageldave/ImagingKit.svg?branch=master)](https://travis-ci.org/hageldave/ImagingKit/branches)
[![Coverage Status](https://coveralls.io/repos/github/hageldave/ImagingKit/badge.svg?branch=master)](https://coveralls.io/github/hageldave/ImagingKit?branch=master)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.hageldave.imagingkit/imagingkit-core.svg)](http://search.maven.org/#artifactdetails|com.github.hageldave.imagingkit|imagingkit-core|1.4|jar)
---
#### [Development Branch](https://github.com/hageldave/ImagingKit/tree/devel2.0)
[![Build Status](https://travis-ci.org/hageldave/ImagingKit.svg?branch=devel2.0)](https://travis-ci.org/hageldave/ImagingKit/branches)
[![Coverage Status](https://coveralls.io/repos/github/hageldave/ImagingKit/badge.svg?branch=devel2.0)](https://coveralls.io/github/hageldave/ImagingKit?branch=devel2.0)
---

A Java library for imaging tasks that integrates well with the commonly used java.awt.image environment (especially well with TYPE_INT BufferedImages). Its goal is to make image processing more convenient and to ease performance optimization. The library is intended for images using integer typed values like 24bit RGB or 32bit ARGB. 

So far the *ImagingKit-Core* artifact of the library is available through the maven central repository:
```xml
<dependency>
  <groupId>com.github.hageldave.imagingkit</groupId>
  <artifactId>imagingkit-core</artifactId>
  <version>2.0</version>
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
img.stream().parallel().forEach( px -> {
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
img.forEach(true, (pixel) -> {
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
ImageFrame.display(img);
ImageSaver.saveImage(img.getRemoteBufferedImage(), "hello_world.png");
```
Fancy polar color thing:
```java
Img img = new Img(1024, 1024);
img.forEach(px -> {
	double x = px.getXnormalized()*2-1;
	double y = px.getYnormalized()*2-1;
	double len = Math.max(Math.abs(x),Math.abs(y));
	double angle = (Math.atan2(x,y)+Math.PI)*(180/Math.PI);
	
	double r = Math.max(0,1-Math.abs((angle-120)/120.0));
	double g = Math.max(0, 1-Math.abs((angle-240)/120.0));
	double b = Math.max(0, angle <= 120 ? 
			1-Math.abs((angle)/120.0):1-Math.abs((angle-360)/120.0));
	
	px.setRGB_fromDouble(r*(1-len), g*(1-len), b*(1-len));
});
ImageFrame.display(img);
```
Shifting hue (using color space transformation):
```java
Img img = ImageLoader.loadImgFromURL("http://sipi.usc.edu/database/preview/misc/4.2.03.png");

img.forEach(ColorSpaceTransformation.RGB_2_HSV);
double hueShift = (360-90)/360.0;
img.forEach(pixel -> {
	// R channel corresponds to hue (modulo 1.0 for cyclic behaviour)
	pixel.setR_fromDouble((pixel.r_asDouble()+hueShift) % 1.0);
});
img.forEach(ColorSpaceTransformation.HSV_2_RGB);

ImageFrame.display(img);
```
Normal Map from Height Map (using pixel to vector mapping):
```java
PixelConverter<Pixel, Map.Entry<java.awt.Point, javax.vecmath.Vector3f>> converter
	= new PixelConverter<Pixel, Map.Entry<Point,Vector3f>>()
{
	Vector3f offset = new Vector3f(.5f, .5f, .5f);
	@Override
	public Map.Entry<Point,Vector3f> allocateElement() {
		return new AbstractMap.SimpleEntry<Point,Vector3f>(new Point(), new Vector3f());
	}
	@Override
	public void convertPixelToElement(Pixel px, Map.Entry<Point,Vector3f> el) {
		el.getKey().setLocation(px.getX(), px.getY());
		el.getValue().set(
				(float)px.r_asDouble(), 
				(float)px.g_asDouble(), 
				(float)px.b_asDouble());
	}
	@Override
	public void convertElementToPixel(Map.Entry<Point,Vector3f> el, Pixel px) {
		Vector3f vec = el.getValue();
		vec.scaleAdd(.5f, offset);
		px.setRGB_fromDouble(vec.x, vec.y, vec.z);
	}
};

Img heightmap = ImageLoader.loadImgFromURL("https://upload.wikimedia.org/wikipedia/commons/5/57/Heightmap.png");
Img normalmap = new Img(heightmap.getDimension());
final boolean parallel = true;
normalmap.forEach(converter, parallel, pair -> {
	Point pos = pair.getKey();
	Vector3f vec = pair.getValue();
	// get heights of surrounding poiunts
	float hx0 = heightmap.getValue(pos.x-1, pos.y, Img.boundary_mode_repeat_edge);
	float hx1 = heightmap.getValue(pos.x+1, pos.y, Img.boundary_mode_repeat_edge);
	float hy0 = heightmap.getValue(pos.x, pos.y-1, Img.boundary_mode_repeat_edge);
	float hy1 = heightmap.getValue(pos.x, pos.y+1, Img.boundary_mode_repeat_edge);
	// cross product of central difference vectors is normal
	float zH = (hx1-hx0)/2; // yH=0  xH=1
	float zV = (hy1-hy0)/2; // yV=1  xV=0
	vec.set(
		zH*1-zV*0, 
		zH*0-zV*1, 
		1*0- 0*1);
	if(vec.lengthSquared() > 0) vec.normalize();
});
ImageFrame.display(normalmap);
ImageFrame.display(heightmap);
```
