package worldcleaner.providers;


import worldcleaner.storage.Coord;

import java.util.LinkedList;
import java.util.List;

public abstract class DataProvider {

	private final LinkedList<Coord> chunks = new LinkedList<>();
	protected String worldName, dimensionName;

	public DataProvider(String worldName, String dimensionName) throws Throwable {
		this.worldName = worldName;
		this.dimensionName = dimensionName;
		init();
	}

	public final List<Coord> getChunks() {
		return chunks;
	}

	protected abstract void init() throws Throwable;

	protected final void addChunkAtCoord(int chunkX, int chunkZ) {
		chunks.add(new Coord(chunkX, chunkZ));
	}

	protected final void addChunksInBounds(int worldXMin, int worldZMin, int worldXMax, int worldZMax) {
		for (int x = (worldXMin >> 4); x <= (worldXMax >> 4); x++) {
			for (int z = (worldZMin >> 4); z <= (worldZMax >> 4); z++) {
				chunks.add(new Coord(x, z));
			}
		}
	}

}
