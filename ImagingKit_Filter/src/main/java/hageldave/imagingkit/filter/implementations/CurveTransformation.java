package hageldave.imagingkit.filter.implementations;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.Arrays;

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
	
	
	private static double catmullRom1D(double v, double x1, double x2, double m1, double m2) {
		double v2 = v*v, v3 = v*v*v;
		return 	(2*v3-3*v2+1)*x1 + (-2*v3+3*v2)*x2 +
				(v3-2*v2+v)*m1   + (v3-v2)*m2;
	}
	
	private static double interpolateCurve(double v, Point2D.Double[] points){
		Arrays.sort(points, (p1,p2)->(int)Math.signum(p1.x-p2.x));
		// find index
		int idx; 
		for(idx=0; idx < points.length; idx++)
			if(points[idx].x >= v)
				break;
		
		Point2D.Double p0,p1,p2,p3;
		p0 = points[Math.max(0, idx-2)];
		p1 = points[Math.max(0, idx-1)];
		// v lies between p1 and p2
		p2 = points[Math.min(points.length-1, idx)];
		p3 = points[Math.min(points.length-1, idx+1)];
		
		System.out.println(Arrays.toString(new Object[]{p0,p1,p2,p3}));
		
		double m1,m2;
		if(p0==p1)
			m1 = 1;
		else
			m1 = midSlope(p0,p1,p2);
		
		if(p2==p3)
			m2 = 1;
		else
			m2 = midSlope(p1, p2, p3);
		
		// normalize v to range of surrounding control points
		v = (v-p1.x) / (p2.x-p1.x);
		return catmullRom1D(v, p1.y, p2.y, m1, m2);
	}

	private static double midSlope(Point2D.Double p0, Point2D.Double p1, Point2D.Double p2) {
		double s1 = (p1.y-p0.y)/(p1.x-p0.x);
		double s2 = (p2.y-p1.y)/(p2.x-p1.x);
		double d1 = p0.distance(p1);
		double d2 = p1.distance(p2);
		double dist = d1+d2;
		// proximity weighted slope
		return s1*(d2/dist)+s2*(d1/dist);
	}
	
}
