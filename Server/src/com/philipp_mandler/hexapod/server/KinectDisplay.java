package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.Vec3i;
import com.philipp_mandler.hexapod.hexapod.com.philipp_mandler.hexapod.hexapod.orientation.Chunk;
import com.philipp_mandler.hexapod.hexapod.com.philipp_mandler.hexapod.hexapod.orientation.ChunkManager;
import processing.core.PApplet;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class KinectDisplay extends PApplet {

	private static final long serialVersionUID = 8976369687969539326L;
	private ByteBuffer m_kinectData;

	@Override
	public void setup() {
		// initialize the processing scene
		size(640, 480, P3D);
	}

	@Override
	public void draw() {
		if(m_kinectData != null) {

			background(255);

			g.color(255, 255, 255);

			// create a new ChunkManager
			ChunkManager chunks = new ChunkManager();

			// convert depth map to voxels and add to ChunkManager
			int w = 640;
			int h = 480;
			for(int y = 0; y < h; y++) {
				for(int x = 0; x < w; x++) {
					int pos = (y * w + x);
					int value = m_kinectData.getShort(pos * 2);
					if(value != 2047) {
						double distance = 0.1236 * Math.tan(value / 2842.5 + 1.1863);

						double minDistance = -10.0;
						double scaleFactor = 0.00021;
						double world_x = (x - w / 2) * (distance + minDistance) * scaleFactor;
						double world_y = (y - h / 2) * (distance + minDistance) * scaleFactor;

						// world z = distance

						chunks.setBlock(new Vec3i((int) (world_x / 0.025), (int) (world_y / 0.025), (int) (distance / 0.025)), true);
					}
				}
			}



			// find Ground

			ChunkManager groundChunks = new ChunkManager();


			for(Chunk chunk : chunks.getChunks()) {

				// check if there is a chunk below
				boolean skipChunk = false;
				for(Chunk tmpChunk : chunks.getChunks()) {
					 if(tmpChunk.getOrigin().getX() == chunk.getOrigin().getX() && tmpChunk.getOrigin().getX() == chunk.getOrigin().getZ()) {
						 if(tmpChunk.getOrigin().getY() < chunk.getOrigin().getY())
							 skipChunk = true;
					 }
				}
				 // skip chunk if there is at least one below
				if(skipChunk) continue;


				Chunk currentChunk = chunk;

				for(int x = 0; x < 64; x++) {
					for(int z = 0; z < 64; z++) {
						int topHeight = -1;
						boolean foundSolid = false;
						boolean foundGround = false;
						Chunk foundChunk = chunk;
						while(!foundGround) {
							for(int y = 0; y < 64; y++) {
								if(currentChunk.getBlock(new Vec3i(x, y, z))) {
									foundSolid = true;
									topHeight = y;
									foundChunk = currentChunk;
								}
								else {
									if(foundSolid) {
										foundGround = true;
										break;
									}
								}
							}

							// continue at chunk above
							if(!foundGround) {
								Chunk topChunk = chunks.getChunkAt(new Vec3i(currentChunk.getOrigin().getX(), currentChunk.getOrigin().getY() + 1, currentChunk.getOrigin().getZ()));
								if(topChunk != null) {
									currentChunk = topChunk;
								}
								else {
									break;
								}
							}
						}
						if(topHeight != -1) {
							groundChunks.setBlock(new Vec3i(x + (foundChunk.getOrigin().getX() * 64), topHeight + (foundChunk.getOrigin().getY() * 64), z + (foundChunk.getOrigin().getZ() * 64)), true);
						}
						currentChunk = chunk;
					}
				}
			}


			// clean ground voxels


			double heightSum = 0;
			double voxelCount = 0;

			ChunkManager cleanedGroundChunks = new ChunkManager();

			for(Chunk chunk : groundChunks.getChunks()) {

				// check if there is a chunk below
				boolean skipChunk = false;
				for(Chunk tmpChunk : chunks.getChunks()) {
					if(tmpChunk.getOrigin().getX() == chunk.getOrigin().getX() && tmpChunk.getOrigin().getX() == chunk.getOrigin().getZ()) {
						if(tmpChunk.getOrigin().getY() < chunk.getOrigin().getY())
							skipChunk = true;
					}
				}
				// skip chunk if there is at least one below
				if(skipChunk) continue;


				Chunk currentChunk = chunk;

				for(int x = 0; x < 64; x++) {
					for(int z = 0; z < 64; z++) {
						boolean foundGround = false;
						while(!foundGround) {
							for(int y = 0; y < 64; y++) {
								if(currentChunk.getBlock(new Vec3i(x, y, z))) {
									foundGround = true;
									Vec3i globalPos = new Vec3i(x + currentChunk.getOrigin().getX() * 64, y + currentChunk.getOrigin().getY() * 64, z + currentChunk.getOrigin().getZ() * 64);
									int blockAround = 0;
									for(int i = -1; i <= 1; i++) {
										for(int j = -1; j <= 1; j++) {
											for(int k = -1; k <= 1; k++) {
												if(i == 0 && j == 0 && k == 0) continue;
												if(groundChunks.getBlock(globalPos.sum(new Vec3i(i, j, k)))) blockAround++;
											}
										}
									}
									if(blockAround > 2) {
										cleanedGroundChunks.setBlock(globalPos, true);
										heightSum += globalPos.getY();
										voxelCount++;
									}
								}
							}

							// continue at chunk above
							if(!foundGround) {
								Chunk topChunk = chunks.getChunkAt(new Vec3i(currentChunk.getOrigin().getX(), currentChunk.getOrigin().getY() + 1, currentChunk.getOrigin().getZ()));
								if(topChunk != null) {
									currentChunk = topChunk;
								}
								else {
									break;
								}
							}
						}
						currentChunk = chunk;
					}
				}
			}



			// second cleaning

			for(Chunk chunk : cleanedGroundChunks.getChunks()) {

				// check if there is a chunk below
				boolean skipChunk = false;
				for(Chunk tmpChunk : chunks.getChunks()) {
					if(tmpChunk.getOrigin().getX() == chunk.getOrigin().getX() && tmpChunk.getOrigin().getX() == chunk.getOrigin().getZ()) {
						if(tmpChunk.getOrigin().getY() < chunk.getOrigin().getY())
							skipChunk = true;
					}
				}
				// skip chunk if there is at least one below
				if(skipChunk) continue;


				Chunk currentChunk = chunk;

				int maxHeight = (int)Math.floor(heightSum / voxelCount);

				for(int x = 0; x < 64; x++) {
					for(int z = 0; z < 64; z++) {
						boolean foundGround = false;
						while(!foundGround) {
							for(int y = 0; y < 64; y++) {
								if(currentChunk.getBlock(new Vec3i(x, y, z))) {
									foundGround = true;
									Vec3i globalPos = new Vec3i(x + currentChunk.getOrigin().getX() * 64, y + currentChunk.getOrigin().getY() * 64, z + currentChunk.getOrigin().getZ() * 64);
									if(globalPos.getY() > maxHeight) {
										currentChunk.setBlock(new Vec3i(x, y, z), false);
									}
									else {
										cleanedGroundChunks.setBlock(globalPos.sum(new Vec3i(0, -1, 0)), true);
										cleanedGroundChunks.setBlock(globalPos.sum(new Vec3i(0, -2, 0)), true);
										cleanedGroundChunks.setBlock(globalPos.sum(new Vec3i(0, -3, 0)), true);
									}
								}
							}

							// continue at chunk above
							if(!foundGround) {
								Chunk topChunk = chunks.getChunkAt(new Vec3i(currentChunk.getOrigin().getX(), currentChunk.getOrigin().getY() + 1, currentChunk.getOrigin().getZ()));
								if(topChunk != null) {
									currentChunk = topChunk;
								}
								else {
									break;
								}
							}
						}
						currentChunk = chunk;
					}
				}
			}









			// graphics output

			background(0);

			g.stroke(0x33, 0x33, 0x33);
			g.fill(0xFF, 0xFF, 0xFF);

			float scale = 10;

			g.pushMatrix();
			g.translate(200, 300, -200);
			g.rotate(-0.25f, 1, 0, 0);
			g.rotate(-0.25f, 0, 1, 0);
			for(Chunk chunk : chunks.getChunks()) {
				Chunk groundChunk = cleanedGroundChunks.getChunkAt(chunk.getOrigin());
				for(int x = 0; x < 64; x++) {
					for(int y = 0; y < 64; y++) {
						for(int z = 0; z < 64; z++) {
							if(chunk.getBlock(new Vec3i(x, y, z))) {
								g.pushMatrix();
								g.translate((x + (chunk.getOrigin().getX() * 64)) * scale, -(y + (chunk.getOrigin().getY() * 64)) * scale, -(z + (chunk.getOrigin().getZ() * 64)) * scale);
								g.fill(0xFF, 0xFF, 0xFF);
								boolean hide = false;
								if(groundChunk != null) {
									if(groundChunk.getBlock(new Vec3i(x, y, z))) {
										hide = true;
										g.fill(0xFF, 0x00, 0x00);
									}
								}
								if(!hide) g.box(scale);
								g.popMatrix();
							}
						}
					}
				}
			}

			g.stroke(0xFF, 0x00, 0x00, 0x66);
			for(int x = -2; x < 4; x++) {
				for(int y = -2; y < 2; y++) {
					for(int z = 0; z < 4; z++) {
						g.line(64 * scale * x, 64 * scale * y, -64 * scale * z, (64 * x + 64) * scale, 64 * scale * y, -64 * scale * z);
						g.line(64 * scale * x, 64 * scale * y, -64 * scale * z, 64 * scale * x, (64 * y + 64) * scale, -64 * scale * z);
						g.line(64 * scale * x, 64 * scale * y, -64 * scale * z, 64 * scale * x, 64 * scale * y, -(64 * z + 64) * scale);
					}

				}
			}

			g.popMatrix();

		}

	}

	public void setKinectData(ByteBuffer buffer) {
		// receive Kinect data
		m_kinectData = buffer;
		m_kinectData.order(ByteOrder.LITTLE_ENDIAN);
	}
}
