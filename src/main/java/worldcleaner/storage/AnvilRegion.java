package worldcleaner.storage;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class AnvilRegion {

	private final File regionfolder;
	private final int columnX;
	private final int columnZ;
	public AnvilRegion(File regionfolder, String filename) {
		this.regionfolder = regionfolder;
		String[] split = filename.split("[.]");
		if (split.length != 4) {
			throw new IllegalArgumentException("Not a region file");
		}
		this.columnX = Integer.parseInt(split[1]);
		this.columnZ = Integer.parseInt(split[2]);
	}

	public static void init() {}

	private static final int dataBlockSize = 4096;

	private final int[] timestamps = new int[1024];
	private final HashMap<Coord, byte[]> chunks = new HashMap<>();

	public int getX() {
		return columnX;
	}

	public int getZ() {
		return columnZ;
	}

	public List<Coord> getChunks() {
		return new ArrayList<>(chunks.keySet());
	}

	public void removeChunk(Coord chunkcoord) {
		chunks.remove(chunkcoord);
	}

	private File getFile() {
		return new File(regionfolder, "r." + columnX + "." + columnZ + ".mca");
	}

	public void loadFromDisk() throws IOException {
		File regionfile = getFile();
		if (!regionfile.exists()) {
			return;
		}
		RandomAccessFile raf = new RandomAccessFile(regionfile, "rw");
		if (raf.length() < dataBlockSize * 2) {
			raf.close();
			return;
		}
		int[] locations = new int[1024];
		for (int i = 0; i < locations.length; i++) {
			locations[i] = raf.readInt();
		}
		for (int i = 0; i < timestamps.length; i++) {
			timestamps[i] = raf.readInt();
		}
		for (int z = 0; z < 32; z++) {
			for (int x = 0; x < 32; x++) {
				int location = locations[x + z * 32];
				if (location != 0) {
					raf.seek((long) (location >> 8) * dataBlockSize);
					chunks.put(new Coord(x, z), readFully(raf, raf.readInt()));
				}
			}
		}
		raf.close();
	}

	private static byte[] readFully(RandomAccessFile file, int len) throws IOException {
		byte[] data = new byte[len];
		int totalRead = 0;
		while (totalRead < data.length) {
			int read = file.read(data, totalRead, data.length - totalRead);
			if (read == -1) {
				throw new IOException("Unexpected end of stream");
			}
			totalRead += read;
		}
		return data;
	}

	public void saveToDisk() throws IOException {
		File regionfile = getFile();
		if (chunks.isEmpty()) {
			delete();
			return;
		}
		if (!regionfile.exists()) {
			regionfile.createNewFile();
		}
		RandomAccessFile raf = new RandomAccessFile(regionfile, "rw");
		ArrayList<byte[]> chunkbuffers = new ArrayList<>();
		int[] locations = new int[1024];
		int currentoffset = 2;
		for (Entry<Coord, byte[]> entry : chunks.entrySet()) {
			chunkbuffers.add(entry.getValue());
			int newsize = calcPaddedChunkDataDataBlocks(entry.getValue().length);
			locations[entry.getKey().getX() + entry.getKey().getZ() * 32] = ((currentoffset << 8) | newsize);
			currentoffset += newsize;
		}
		for (int location : locations) {
			raf.writeInt(location);
		}
		for (int timestamp : timestamps) {
			raf.writeInt(timestamp);
		}
		for (byte[] data : chunkbuffers) {
			raf.writeInt(data.length);
			raf.write(data);
			byte[] pad = new byte[calcPadBytesLength(data.length)];
			raf.write(pad);
		}
		raf.close();
	}

	public void delete() {
		getFile().delete();
	}

	private static final int chunkDataLenFieldSize = Integer.SIZE / Byte.SIZE;

	private int calcPadBytesLength(int chunkdatasize) {
		return calcPaddedChunkDataDataBlocks(chunkdatasize) * dataBlockSize - chunkDataLenFieldSize - chunkdatasize;
	}

	private int calcPaddedChunkDataDataBlocks(int chunkdatasize) {
		int chunkdatawithsize = chunkdatasize + chunkDataLenFieldSize;
		int datablocks = chunkdatawithsize / dataBlockSize;
		if (chunkdatawithsize % dataBlockSize == 0) {
			return datablocks;
		} else {
			return datablocks + 1;
		}
	}

}
