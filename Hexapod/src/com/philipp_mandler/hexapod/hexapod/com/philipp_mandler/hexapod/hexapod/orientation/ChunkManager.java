package com.philipp_mandler.hexapod.hexapod.com.philipp_mandler.hexapod.hexapod.orientation;

import com.philipp_mandler.hexapod.hexapod.Vec3i;

import java.util.ArrayList;

public class ChunkManager {

	private ArrayList<Chunk> m_chunks = new ArrayList<>();

	public void setBlock(Vec3i pos, boolean solid) {

		Vec3i chunkOrigin = new Vec3i(pos.getX() / 64, pos.getY() / 64, pos.getZ() / 64);

		if(pos.getX() < 0)
			chunkOrigin.setX(chunkOrigin.getX() - 1);

		if(pos.getY() < 0)
			chunkOrigin.setY(chunkOrigin.getY() - 1);

		if(pos.getZ() < 0)
			chunkOrigin.setZ(chunkOrigin.getZ() - 1);

		Chunk destinationChunk = null;
		for(Chunk chunk : m_chunks) {
			if(chunk.getOrigin().equals(chunkOrigin)) {
				destinationChunk = chunk;
				break;
			}
		}

		if(destinationChunk == null) {
			destinationChunk = new Chunk(chunkOrigin);
			m_chunks.add(destinationChunk);
		}

		destinationChunk.setBlock(new Vec3i(superModulo(pos.getX(), 64), superModulo(pos.getY(), 64), superModulo(pos.getZ(), 64)), solid);
	}

	private int superModulo(int a, int b) {
		return (a % b + b) % b;
	}

	public ArrayList<Chunk> getChunks() {
		return m_chunks;
	}

}
