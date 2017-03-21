package hageldave.imagingkit.filter.implementations;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Arrays;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.util.ImageFrame;

public class CurveTransformation extends GenericColorChannelTransformation {
	
	public Point2D.Double[] curve;

	@Override
	protected int transformAlpha(int a) {
		return (int) (255*interpolate(a/255.0));
	}
	
	@Override
	protected int transformRed(int r) {
		return (int) (255*interpolate(r/255.0));
	}

	@Override
	protected int transformGreen(int g) {
		return (int) (255*interpolate(g/255.0));
	}

	@Override
	protected int transformBlue(int b) {
		return (int) (255*interpolate(b/255.0));
	}

	
	private double interpolate(double v){
		return interpolateCurve(v, curve);
	}
	
	
	private static double catmullRom1D(double x, double x1, double x2, double y1, double y2, double m1, double m2) {
		double d=(x2-x1), t=(x-x1)/d, t2=t*t, t3=t*t*t;
		m1*=d; m2*=d;
		return 	(2*t3-3*t2+1)*y1 + (-2*t3+3*t2)*y2 +
				(t3-2*t2+t)*m1   + (t3-t2)*m2;
	}
	
	private static double interpolateCurve(double v, Point2D[] points){
		Arrays.sort(points, (p1,p2)->(int)Math.signum(p1.getX()-p2.getX()));
		// find index
		int idx; 
		for(idx=0; idx < points.length; idx++)
			if(points[idx].getX() > v)
				break;
		
		Point2D p0,p1,p2,p3;
		p0 = points[Math.max(0, idx-2)];
		p1 = points[Math.max(0, idx-1)];
		// v lies between p1 and p2
		p2 = points[Math.min(points.length-1, idx)];
		p3 = points[Math.min(points.length-1, idx+1)];
		
		color = colors[Math.max(0,idx-1)%4];
		
		double m1,m2;
		if(p0==p1){
			double x0=p1.getX(), x1=p2.getX(), x2=p3.getX();
			double y0=p1.getY(), y1=p2.getY(), y2=p3.getY();
			m1 = ((y1-y0)+(y2-y1)*0.5)/((x1-x0)+(x2-x1)*0.5);
		} else 
			m1 = midSlope(p0,p1,p2);
		
		if(p2==p3){
			double x0=p1.getX(), x1=p2.getX(), x2=p3.getX();
			double y0=p1.getY(), y1=p2.getY(), y2=p3.getY();
			m2 = ((y1-y0)+(y2-y1)*0.5)/((x1-x0)+(x2-x1)*0.5);
		} else 
			m2 = midSlope(p1,p2,p3);
		System.out.println(String.format(" %03f %03f", m1,m2));
		return catmullRom1D(v, p1.getX(), p2.getX(), p1.getY(), p2.getY(), m1, m2);
	}

	private static double midSlope(Point2D p0, Point2D p1, Point2D p2) {
		double s1 = (p1.getY()-p0.getY())/(p1.getX()-p0.getX());
		double s2 = (p2.getY()-p1.getY())/(p2.getX()-p1.getX());
		
		if(p0.equals(p1)) return s2;
		if(p1.equals(p2)) return s1;
		
		double d1 = p0.distance(p1);
		double d2 = p1.distance(p2);
		double dist = d1+d2;
		// proximity weighted slope
		return s1*(d2/dist)+s2*(d1/dist);
	}

	public static void drawCurve(Point2D[] curve2) {
		Img img = new Img(1600, 1600);
		img.fill(0xff000000);
		for(int x = 0; x < img.getWidth(); x++){
			double v = x*1.0/img.getWidth();
			int y;
			if(v<0.2){
				y = (int)(img.getHeight()*catmullRom1D(v, 0,0.2, 0,0.6, 1, 1.75));
			} else {
				y = (int)(img.getHeight()*catmullRom1D(v, 0.2,1, 0.6,1, 1.75,1));
			}
			y = (int)(img.getHeight()*interpolateCurve(v, curve2));
//			y = (int)(img.getHeight()*catmullRom1D(v, 0.6, 1, 1.75, 1));
			img.setValue(x, y, color);
		}
		ImageFrame.display(img);
	}
	
	static int[] colors = {0xffff0000,0xff00ff00,0xffffffff,0xff0000ff};
	static int color = colors[0];
	
}
