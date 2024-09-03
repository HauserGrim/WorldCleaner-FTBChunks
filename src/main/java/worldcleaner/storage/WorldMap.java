package worldcleaner.storage;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class WorldMap {

	private final HashMap<Coord, Set<Coord>> map = new HashMap<>(200);

	public WorldMap() {
	}

	public void addChunk(Coord chunkCoord) {
		addChunk0(chunkCoord);
	}

	private void addChunk0(Coord chunkCoord) {
		getChunks0(Coord.getRegionCoord(chunkCoord.getX()), Coord.getRegionCoord(chunkCoord.getZ()))
		.add(new Coord(Coord.getLocalCoord(chunkCoord.getX()), Coord.getLocalCoord(chunkCoord.getZ())));
	}

	public boolean hasChunks(int regionX, int regionZ) {
		return map.containsKey(new Coord(regionX, regionZ));
	}

	public Set<Coord> getChunks(int regionX, int regionZ) {
		return Collections.unmodifiableSet(getChunks0(regionX, regionZ));
	}

	private Set<Coord> getChunks0(int regionX, int regionZ) {
		Coord regionCoord = new Coord(regionX, regionZ);
		Set<Coord> chunks = map.get(regionCoord);
		if (chunks == null) {
			chunks = new HashSet<Coord>(40);
			map.put(regionCoord, chunks);
		}
		return chunks;
	}

}
