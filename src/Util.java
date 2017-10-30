import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * This class contains random functions that I found useful. They are all completely static and 
 * require no member variables. 
 */
abstract public class Util {
	/**	
	 * Uses system time an independent variable to create recurring, linear functions.
	 * 
	 * The xy graph will look like this:
	 * |   /   /   /   /   /   /   /
	 * |  /   /   /   /   /   /   /
	 * | /   /   /   /   /   /   /
	 * |/   /   /   /   /   /   /
	 * +------------------------------------
	 * 
	 * @param dMaximum The maximum value given by this function right before it resets to 0.
	 * @param nPeriod The amount of time in milliseconds to complete one cycle
	 * @return A periodic value...
	 */
	public static double modulus(double dMaximum, int nPeriod){
		return System.nanoTime() / 1000000.0 % (double)nPeriod / nPeriod * dMaximum;
	}
	
	
//	/** Wrapper method for segmentPolyIntersect(Line2D.Double, Polygon).
//	 *	Takes two points instead of a Line2D */
	/* static boolean segmentPolyIntersect(Point p1, Point p2, Polygon poly) {
		return segmentPolyIntersect(new Line2D.Double(p1, p2), poly);
	}*/
	
	static boolean shapeIntersect(Shape shape1, Shape shape2){
		if (shape1 == null || shape2 == null ){
			return false;
		/**
		 * Checks whether a polygon and a given line intersect by breaking the polygon into
		 * lines and checking for intersection between the line fragments and the other main line.
		 * 
		 * Also checks whether the line is completely inside the polygon by checking if both points
		 * are contained.
		 */
		} else if((shape1 instanceof Line2D && shape2 instanceof Polygon) 
		 ||(shape1 instanceof Polygon && shape2 instanceof Line2D)){
			Line2D line1;
			Polygon poly;
			if(shape1 instanceof Line2D){
				line1 = (Line2D) shape1;
				poly = (Polygon) shape2;
			} else {
				line1 = (Line2D) shape2;
				poly = (Polygon) shape1;
			}
			/* Line breakup */
			for(int nI=0; nI<poly.npoints; nI++) {
				Point p3 = new Point(poly.xpoints[nI], poly.ypoints[nI]);
				Point p4 = new Point(poly.xpoints[(nI+1)%poly.npoints], poly.ypoints[(nI+1)%poly.npoints]);
				if(line1.intersectsLine(new Line2D.Double(p3, p4)))
					return true;
			}
			/* Test for a completely contained line */
			if(poly.contains(line1.getP1()) && poly.contains(line1.getP2())){
				return true;
			}
			return false;
		}
		/**
		 * Checks whether the given two polygons intersect by splitting them into lines and checking
		 * for any intersections. Note that this will not return true if one polygon is entirely
		 * inside the other.
		 */
		else if(shape1 instanceof Polygon && shape2 instanceof Polygon){
			Polygon poly1 = (Polygon) shape1;
			Polygon poly2 = (Polygon) shape2;
			
			for(int nI1 = 0; nI1 < poly1.npoints; nI1++) {
				for(int nI2 = 0; nI2 < poly2.npoints; nI2++) {
					Line2D.Double line1 = new Line2D.Double((double)poly1.xpoints[nI1], (double)poly1.ypoints[nI1], (double)poly1.xpoints[(nI1+1)%poly1.npoints], (double)poly1.ypoints[(nI1+1)%poly1.npoints]);
					Line2D.Double line2 = new Line2D.Double((double)poly2.xpoints[nI2], (double)poly2.ypoints[nI2], (double)poly2.xpoints[(nI2+1)%poly2.npoints], (double)poly2.ypoints[(nI2+1)%poly2.npoints]);
					if(line1.intersectsLine(line2))
						return true;
				}
			}
			return false;
			
			/* Alternate way of checking polygon intersection. Checks points instead of lines. */
			/*for(int nI1 = 0; nI1 < poly1.npoints; nI1++) {
				Point point1 = new Point(poly1.xpoints[nI1], poly1.ypoints[nI1]);
				if(poly2.contains(point1))
					return true;
			}
			return false;*/
		/**
		 * Checks whether a given polygon and shape intersect by breaking the polygon up into lines and using
		 * this method to check whether the line and shape intersect. 
		 */
		} else if (shape1 instanceof Polygon || shape2 instanceof Polygon){
			Polygon poly;
			Shape shPath;
			if(shape1 instanceof Polygon){
				poly = (Polygon) shape1;
				shPath = shape2;
			} else {
				poly = (Polygon) shape2;
				shPath = shape1;
			}
			/* Line breakup */
			for(int nI=0; nI<poly.npoints; nI++) {
				Line2D.Double line = new Line2D.Double(poly.xpoints[nI], poly.ypoints[nI], poly.xpoints[(nI+1)%poly.npoints], poly.ypoints[(nI+1)%poly.npoints]);
				if(shapeIntersect(line, shPath))
					return true;
				
				/* Test for a completely contained line */
				if(shPath.contains(line.getP1()) && shPath.contains(line.getP2())){
					return true;
				}
			}
			
			return false;
		
		/**
		 * Checks whether a given line and generic shape intersect by rotating the shape and line using the 
		 * first point on the line as a rotation anchor, using an angle where the line will become flat. 
		 * When the line is flat, it can be represented with a long, thin rectangle instead of a line. 
		 */
		} else if (shape1 instanceof Line2D || shape2 instanceof Line2D){
			Line2D line;
			Shape shPath;
			if(shape1 instanceof Line2D){
				line = (Line2D) shape1;
				shPath = shape2;
			} else {
				line = (Line2D) shape2;
				shPath = shape1;
			}
			/* Get the distance represented by the line for the length of the rectangle later, 
			 * the angle of rotation to rotate the line and path by, 
			 * and the coordinates for the center of rotation. */
			final double dDISTANCE = Point.distance(line.getX1(), line.getY1(), line.getX2(), line.getY2());
			final double dROTATION_ANGLE = -Math.atan2(line.getY2() - line.getY1(), line.getX2() - line.getX1());
			final double dROTATION_X = line.getX1();
			final double dROTATION_Y = line.getY1();
			
			/* Rotate the path about the rotation anchor (the first point in the line) */
			AffineTransform afTransform = AffineTransform.getRotateInstance(dROTATION_ANGLE, dROTATION_X, dROTATION_Y);
			Shape shPathRotated = afTransform.createTransformedShape(shPath);
			
			/* Now compare the rotated shape to a rectangle with height of 1, width of distance. */
			return shPathRotated.intersects(line.getX1(), line.getY1(), dDISTANCE, 1);
		} else {
			throw new IllegalArgumentException("Unknown types to check intersection for.");
		}
	}
	public static void elasticCollision(Floater floater1, Floater floater2){
		/** Conservation of momentum: elastic collision 
		 *  
		 *  Theta: angle from 0° to floater velocity vector.
		 *  Alpha: angle from 0° to axis of collision
		 *  Beta: the angle from the opposite side of the axis of collision to the velocity vector 
		 */
		final double ALPHA = Math.atan2(floater1.getY() - floater2.getY(), floater1.getX() - floater2.getX()) + Math.PI;
		final double THETA_FLT1 = Math.atan2(floater2.getDirectionY(), floater2.getDirectionX());
		final double BETA_FLT1 = Math.PI / 2 + THETA_FLT1 - ALPHA;
		/* Break the velocity of the floater to a component of the collision axis. */
		final double V_FLT2_COL_AXIS = Math.hypot(floater2.getDirectionX(), floater2.getDirectionY()) * Math.cos(BETA_FLT1);
		final double V_FLT2_TANGENT = Math.hypot(floater2.getDirectionX(), floater2.getDirectionY()) * Math.sin(BETA_FLT1);
		
		final double THETA_FLT2 = Math.atan2(floater1.getDirectionY(), floater1.getDirectionX());
		final double BETA_FLT2 = Math.PI / 2 + THETA_FLT2 - ALPHA;
		final double V_FLT1_COL_AXIS = Math.hypot(floater1.getDirectionX(), floater1.getDirectionY()) * Math.cos(BETA_FLT2);
		final double V_FLT1_TANGENT = Math.hypot(floater1.getDirectionX(), floater1.getDirectionY()) * Math.sin(BETA_FLT2);
		
		/* Calculate the new magnitude of the velocity components */
		final double M1 = floater1.getSize();
		final double M2 = floater2.getSize();
		final double NEW_V_FLT1 = (V_FLT1_COL_AXIS * (M1 - M2) + 2 * M2 * V_FLT2_COL_AXIS) / (M1 + M2);
		final double NEW_V_FLT2 = (V_FLT2_COL_AXIS * (M2 - M1) + 2 * M1 * V_FLT1_COL_AXIS) / (M1 + M2);
		
		
		/* Convert the new components back into the old angle's components */
		final double V_FLT1 = Math.hypot(NEW_V_FLT1, V_FLT1_TANGENT);
		final double V_FLT2 = Math.hypot(NEW_V_FLT2, V_FLT2_TANGENT);
		floater1.setDirectionX(V_FLT1 * Math.cos(THETA_FLT1));
		floater1.setDirectionY(V_FLT1 * Math.sin(THETA_FLT1));
		floater2.setDirectionX(V_FLT2 * Math.cos(THETA_FLT2));
		floater2.setDirectionY(V_FLT2 * Math.sin(THETA_FLT2));
		
		//final Point2D.Double COLLISION_POINT = new Point2D.Double(floater2.getX() - floater2.getSize()/2 * Math.cos(ALPHA), floater2.getY() - floater2.getSize()/2 * Math.sin(ALPHA));
		//offscreen.setColor(Color.cyan);
		//offscreen.drawLine(floater1.getX(), floater1.getY(), floater2.getX(), floater2.getY());
		//offscreen.drawLine((int)(COLLISION_POINT.x + 100 * Math.cos(ALPHA + Math.PI/2)), (int)(COLLISION_POINT.y + 100 * Math.sin(ALPHA + Math.PI/2)), (int)(COLLISION_POINT.x + 100 * Math.cos(ALPHA - Math.PI/2)), (int)(COLLISION_POINT.y + 100 * Math.sin(ALPHA - Math.PI/2)));
		//offscreen.drawArc((int)COLLISION_POINT.x -50, (int)COLLISION_POINT.y -50, 100, 100, 0, (int) Math.toDegrees(ALPHA));
		
		
		/* Move the smaller floater away from the other until they are not colliding anymore */
		do{
			if(floater1.getSize() < floater2.getSize()){
				floater1.move(Asteroids_v2.APPLET_X, Asteroids_v2.APPLET_Y);
			} else {
				floater2.move(Asteroids_v2.APPLET_X, Asteroids_v2.APPLET_Y);	
			}
		} while (Util.shapeIntersect(floater1.getShape(), floater2.getShape()));
	}
	/**
	 * There really isn't a built-in way to check whether a string is a number huh?
	 * 
	 * @param s String to check.
	 * @return Whether it is a number or not.
	 */
	public static boolean isNumber(String s) {
		try {
			Double.parseDouble(s);
		}
		catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	/**
	 * I find it annoying that the Polygon class has a translate function and no rotate function.
	 * 
	 * @param unrotated An unrotated polygon to rotate about the origin.
	 * @param dDirection The direction to rotate to in degrees.
	 */
	public static Polygon rotate(Polygon unrotated, double dDirection){
		Polygon rotatedPoly = new Polygon(unrotated.xpoints, unrotated.ypoints, unrotated.npoints);
		/* Rotate and translate polygon*/
		double dRadians = Math.toRadians(dDirection);
		for(int nI = 0; nI < unrotated.npoints; nI++) {
			rotatedPoly.xpoints[nI] = (int)((unrotated.xpoints[nI] * Math.cos(dRadians)) - (unrotated.ypoints[nI] * Math.sin(dRadians)));
			rotatedPoly.ypoints[nI] = (int)((unrotated.xpoints[nI] * Math.sin(dRadians)) + (unrotated.ypoints[nI] * Math.cos(dRadians)));         
		}
		return rotatedPoly;
	}
	/** 
	 * Calculate the intersection of a ray originating from the ship and the rectangle of the screen. 
	 * 
	 * Special cases where the angle is a multiple of 90° are not handled because because the code is cleaner
	 * and because they would never have any bad effect anyway. Math.tan(PI/2) returns 1.63e16 !
	 *
	 * @param rayLoc The location of the ray.
	 * @param rayAngleRadian The ray angle in radians.
	 * @param rectangleX The X coordinate defining the right side of the rectangle.
	 * @param rectangleY The Y coordinate defining the top (actually bottom) of the rectangle.
	 * @return The distance from the ray origin to the boundary of the rectangle. 
	 */
	public static double rayDistanceToRectangle(Point2D.Double rayLoc, double rayAngleRadian, double rectangleX, double rectangleY){
		/* Return exception if ray origin is not in rectangle */
		if(new Rectangle2D.Double(0, 0, rectangleX, rectangleY).contains(rayLoc) == false){
			throw new IllegalArgumentException("Ray origin is not in rectangle");
		}
		
		/* Intersection 1 will reason out Y value */
		Point2D.Double intersection1 = new Point2D.Double();
		if(Math.cos(rayAngleRadian) > 0){
			intersection1.x = rectangleX;
//		} else if(Math.cos(rayAngleRadian) == 0){
//			
		} else {
			intersection1.x = 0;
		}
		intersection1.y = Math.tan(rayAngleRadian) * (intersection1.x - rayLoc.getX()) + rayLoc.getY();
		
		/* Intersection 2 will reason out X value */
		Point2D.Double intersection2 = new Point2D.Double();
		if(Math.sin(rayAngleRadian) > 0){
			intersection2.y = rectangleY;
//		} else if(Math.sin(rayAngleRadian) == 0){
//			
		} else {
			intersection2.y = 0;
		}
		intersection2.x = (intersection2.y - rayLoc.getY())/ Math.tan(rayAngleRadian) + rayLoc.getX();
		
		/* The distance to the point of intersection is the smaller of the distances between the ray origin (ship) 
		 * and the two points. */
		return Math.min(intersection1.distance(rayLoc), intersection2.distance(rayLoc));
	}
//	
//	final static Map<Double, Double> lookup_sin = generateSinTable();
//	final static int nPRECISION = 10000;
//	
//	/**
//	 * Generates a sin lookup table of nPRECISION values from -pi to pi. 
//	 * 
//	 * EDIT: I found this to be much, much slower than a plain old Math.sin().
//	 * 
//	 * @return the sin lookup table.
//	 */
//	private static Map<Double, Double> generateSinTable(){
//		Map<Double, Double> sines = new HashMap<Double, Double>();
//		
//		for(int nI = -nPRECISION / 2; nI <= nPRECISION / 2 ; nI++){
//			sines.put(new Double(nI), new Double(Math.sin(Math.PI * nI / (nPRECISION / 2))));
//		}
//		
//		return sines;
//	}
//	public static double sin(double dAngle){
//		/* Produce a coterminal angle */
//		dAngle %= 2 * Math.PI;
//		if(dAngle < -Math.PI){
//			dAngle += 2 * Math.PI;
//		} else if(dAngle > Math.PI){
//			dAngle -= 2 * Math.PI;
//		}
//		System.out.println(dAngle);
//		/* Round angle to the nearest entry we have */
//		double dEntry = Math.round(dAngle * (nPRECISION / 2) / Math.PI);
//		
//		return lookup_sin.get(new Double(dEntry));
//	}
}
