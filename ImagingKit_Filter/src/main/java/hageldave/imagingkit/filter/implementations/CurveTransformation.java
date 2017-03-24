package hageldave.imagingkit.filter.implementations;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import hageldave.imagingkit.core.Img;
import hageldave.imagingkit.core.util.ImageFrame;

public class CurveTransformation extends GenericColorChannelTransformation {
	
	private Curve curve;
	
	
	public CurveTransformation() {
		this(new Curve() {/*default implementation*/});
	}
	
	public CurveTransformation(Curve curve) {
		setCurve(curve);
	}

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
		return curve.interpolate(v);
	}
	
	public void setCurve(Curve curve) {
		Objects.requireNonNull(curve);
		this.curve = curve;
	}
	
	public Curve getCurve() {
		return curve;
	}
	
	public CubicHermiteSpline setSpline(Point2D[] controlPoints){
		return (CubicHermiteSpline) (this.curve = new CubicHermiteSpline(controlPoints));
	}

	public static void drawCurve(Point2D[] curvep) {
		CubicHermiteSpline curve = new CubicHermiteSpline(curvep);
		Img img = new Img(1024, 1024);
		img.fill(0xff000000);
		for(int x = 0; x < img.getWidth(); x++){
			double v = x*1.0/img.getWidth();
			int y = (int)(img.getHeight()*curve.interpolate(v));
			img.setValue(x, y, 0xff00ff00);
		}
		img.paint(g->{
			g.setColor(Color.WHITE);
			for(Point2D p : curvep){
				int x = (int) (p.getX()*img.getWidth());
				int y = (int) (p.getY()*img.getHeight());
				g.drawOval(x-2, y-2, 4, 4);
			}
		});
		ImageFrame.display(img);
	}
	
	
	public static interface Curve {
		public default double interpolate(double v){return v;}
	}
	
	public static class CubicHermiteSpline implements Curve {
		
		private Point2D[] controlPoints;
		
		public CubicHermiteSpline(Point2D[] controlPoints) {
			setControlPoints(controlPoints);
		}

		private static void requireDistinctXCoordinates(Point2D[] controlPoints) {
			TreeMap<Double, List<Integer>> coords2occurence = new TreeMap<>();
			for(int i = 0; i < controlPoints.length; i++){
				Point2D p = controlPoints[i];
				List<Integer> sameXCoords;
				boolean listInitialized = (sameXCoords = coords2occurence.get(p.getX())) != null || (sameXCoords  = new LinkedList<>()) != null;
				assert(listInitialized);
				sameXCoords.add(i);
				coords2occurence.putIfAbsent(p.getX(), sameXCoords);
			}
			if(coords2occurence.size() != controlPoints.length){
				coords2occurence.entrySet().removeIf(e->e.getValue().size() < 2);
				Integer[][] badControlPoints = new Integer[coords2occurence.size()][];
				int[] i = {0};
				coords2occurence.entrySet().forEach(e->badControlPoints[i[0]++] = e.getValue().toArray(new Integer[0]));
				throw new IllegalArgumentException("Provided control points do not have distinct x values. Indices of Points with same x coordinates: \n\t" + Arrays.deepToString(badControlPoints));
			}
		}

		private static void requireAtLeast2Points(Point2D[] controlPoints) {
			if(controlPoints.length < 2)
				throw new IllegalArgumentException("Cannot create Curve from less than 2 points");
		}
		
		public Point2D[] getControlPoints() {
			return controlPoints;
		}
		
		public void setControlPoints(Point2D[] controlPoints) {
			Objects.requireNonNull(controlPoints);
			requireAtLeast2Points(controlPoints);
			requireDistinctXCoordinates(controlPoints);
			this.controlPoints = controlPoints;
		}

		@Override
		public double interpolate(double v) {
			return interpolateCurve(v, controlPoints);
		}
		
		private static double catmullRom1D(double x, double x1, double x2, double y1, double y2, double m1, double m2) {
			double d=(x2-x1), t=(x-x1)/d, t2=t*t, t3=t*t*t;
			m1*=d; m2*=d;
			return 	(2*t3-3*t2+1)*y1 + (-2*t3+3*t2)*y2 +
					(t3-2*t2+t)*m1   + (t3-t2)*m2;
		}
		
		private static double interpolateQuadratic(double x, double x1, double x2, double y1, double y2, double m2) {
			// y = a*x^2 + b*x + c
			// y'= m = 2*a*x + b
			// b = m - 2*a*x
			// y = a*x^2 + (m - 2*a*x)*x + c 
			// y = m*x - a*x^2 + c
			double a,b,c;
			// shift x1 to 0
			x2 -= x1;
			x  -= x1;
			x1 -= x1;
			// if x1 = 0 then y1 = c;
			c = y1;
			// y2 = m*x2 - a*x2^2 + y1
			a = (y2-y1-m2*x2)/-(x2*x2);
			b = m2 - 2*a*x2;
			// function is now complete
			return a*(x*x) + b*x + c;
		}
		
		private static double interpolateCurve(double v, Point2D[] points){
			Arrays.sort(points, (p1,p2)->(int)Math.signum(p1.getX()-p2.getX()));
			// find index
			int idx; 
			for(idx=0; idx < points.length; idx++)
				if(points[idx].getX() > v)
					break;
			
			Point2D p0,p1,p2,p3;
			p0 = points[clamp(0, idx-2, points.length-3)];
			p1 = points[clamp(0, idx-1, points.length-2)];
			// v lies between p1 and p2
			p2 = points[Math.min(clamp(1, idx,   points.length-1),points.length-1)];
			p3 = points[Math.min(clamp(2, idx+1, points.length-1),points.length-1)];
			
			double m1,m2;
			if(p0==p1){
				return interpolateQuadratic(v, p1.getX(), p2.getX(), p1.getY(), p2.getY(), midSlope(p1,p2,p3));
			} else {
				m1 = midSlope(p0,p1,p2);
			}
			
			if(p2==p3){
				return interpolateQuadratic(v, p2.getX(), p1.getX(), p2.getY(), p1.getY(), midSlope(p0,p1,p2));
			} else {
				m2 = midSlope(p1,p2,p3);
			}
			return catmullRom1D(v, p1.getX(), p2.getX(), p1.getY(), p2.getY(), m1, m2);
		}

		private static double midSlope(Point2D p0, Point2D p1, Point2D p2) {
			double s1 = (p1.getY()-p0.getY())/(p1.getX()-p0.getX());
			double s2 = (p2.getY()-p1.getY())/(p2.getX()-p1.getX());
			
			if(p0.equals(p1)) return s2;
			if(p1.equals(p2)) return s1;
			
			double d1 = Math.abs(p0.getX()-p1.getX());//p0.distance(p1);
			double d2 = Math.abs(p1.getX()-p2.getX());//p1.distance(p2);
			double dist = d1+d2;
			// proximity weighted slope
			return s1*(d1/dist)+s2*(d2/dist);
		}
		
		private static int clamp(int lowerBound, int value, int upperBound){
			return Math.max(lowerBound, Math.min(upperBound, value));
		}
		
	}
	
}
