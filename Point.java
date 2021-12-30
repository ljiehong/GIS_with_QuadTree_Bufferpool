import cs3114.GIS.DS.Compare2D;
import cs3114.GIS.DS.Direction;

import java.util.Set;
import java.util.TreeSet;

public class Point implements Compare2D<Point> {

	private long xcoord;
	private long ycoord;
	private Set<Long> offsets;


	//Constructor
	public Point(long x, long y) {
		xcoord = x;
		ycoord = y;
		offsets=new TreeSet<>();
	}


	//Add file offset
	public void addOffset(long offset){
		offsets.add(offset);
	}

	/**
	 * Gets a set of file offsets
	 * @return set of file offsets
	 * */
	public Set<Long> getOffsets() {
		return offsets;
	}

	// For the following methods, let P designate the Point object on which
	// the method is invoked (e.g., P.getX()).
	// Reporter methods for the coordinates of P.
	public long getX() {
		return xcoord;
	}

	public long getY() {
		return ycoord;
	}

	// Determines which quadrant of the region centered at P the point (X, Y),
	// consistent with the relevent diagram in the project specification;
	// returns NODQUADRANT if P and (X, Y) are the same point.
	public Direction directionFrom(long X, long Y) {

		long xDiff = xcoord - X;
		long yDiff = ycoord - Y;
		if (xDiff == 0 && yDiff == 0)
			return Direction.NE;
		if (xDiff > 0 && yDiff > 0)
			return Direction.NE;
		if (xDiff < 0 && yDiff > 0)
			return Direction.NW;
		if (xDiff < 0 && yDiff < 0)
			return Direction.SW;
		if (xDiff > 0 && yDiff < 0)
			return Direction.SE;

		if (xDiff > 0 && yDiff == 0)
			return Direction.SE;
		if (xDiff == 0 && yDiff > 0)
			return Direction.NW;
		if (xDiff < 0 && yDiff == 0)
			return Direction.SW;
		if (xDiff == 0 && yDiff < 0)
			return Direction.SW;

		return Direction.NOQUADRANT; // actually not reachable
	}

	// Determines which quadrant of the specified region P lies in,
	// consistent with the relevent diagram in the project specification;
	// returns NOQUADRANT if P does not lie in the region. 
	public Direction inQuadrant(double xLo, double xHi, double yLo, double yHi) {

		if (!inBox(xLo, xHi, yLo, yHi))
			return Direction.NOQUADRANT;

		long xCenter = (long) ((xLo + xHi) / 2);
		long yCenter = (long) ((yLo + yHi) / 2);

		return directionFrom(xCenter, yCenter);
	}

	// Returns true iff P lies in the specified region.
	public boolean inBox(double xLo, double xHi, double yLo, double yHi) {
		return (long)(xLo) <= xcoord && xcoord <= (long)(xHi) && (long)(yLo) <= ycoord && ycoord <= (long)(yHi);
	}

	// Returns a String representation of P.
	public String toString() {
		StringBuilder offsetsStr= new StringBuilder();
		for(long offset:offsets){
			offsetsStr.append(String.format(", %d", offset));
		}
		return String.format("[(%d, %d)%s]", xcoord,ycoord,offsetsStr);
	}

	// Returns true iff P and o specify the same point.
	public boolean equals(Object o) {

		Point other = (Point) o;
		return xcoord == other.xcoord && ycoord == other.ycoord;
	}
}
