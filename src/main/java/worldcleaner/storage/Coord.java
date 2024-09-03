package worldcleaner.storage;

public class Coord {

	private final int x;
	private final int z;
	public Coord(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Coord)) {
			return false;
		}
		Coord othercoord = (Coord) other;
		return x == othercoord.x && z == othercoord.z;
	}

	@Override
	public int hashCode() {
		return x * 31 + z;
	}

	@Override
	public String toString() {
		return getX()+"."+getZ();
	}

	public static int getRegionCoord(int worldCoord) {
		return worldCoord >> 5;
	}

	public static int getLocalCoord(int worldCoord) {
		return worldCoord & 31;
	}

}