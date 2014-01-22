package com.philipp_mandler.hexapod.hexapod.com.philipp_mandler.hexapod.hexapod.orientation;

import com.philipp_mandler.hexapod.hexapod.Vec3i;

import java.io.Serializable;

public class Chunk implements Serializable {
	private static final long serialVersionUID = -1847003701665879783L;

	private boolean m_blocks[][][] = new boolean[64][64][64];
	private Vec3i m_origin = new Vec3i();

	public Chunk() {
		for(int x = 0; x < 64; x++) {
			for(int y = 0; y < 64; y++) {
				for(int z = 0; z < 64; z++) {
					m_blocks[x][y][z] = false;
				}
			}
		}
	}

	public Chunk(Vec3i origin) {
		m_origin = origin;
		for(int x = 0; x < 64; x++) {
			for(int y = 0; y < 64; y++) {
				for(int z = 0; z < 64; z++) {
					m_blocks[x][y][z] = false;
				}
			}
		}
	}

	public boolean[][][] getBlocks() {
		return m_blocks;
	}

	public void clear() {
		m_blocks = new boolean[64][64][64];
	}

	public void setBlock(Vec3i pos, boolean solid) {
		m_blocks[pos.getX()][pos.getY()][pos.getZ()] = solid;
	}

	public boolean getBlock(Vec3i pos) {
		return m_blocks[pos.getX()][pos.getY()][pos.getZ()];
	}

	public void setOrigin(Vec3i origin) {
		m_origin = origin;
	}

	public Vec3i getOrigin() {
		return m_origin;
	}
}
