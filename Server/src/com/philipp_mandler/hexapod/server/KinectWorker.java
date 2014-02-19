package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.Vec2i;
import com.philipp_mandler.hexapod.hexapod.Vec3i;
import com.philipp_mandler.hexapod.hexapod.net.BooleanMapPackage;
import com.philipp_mandler.hexapod.hexapod.orientation.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class KinectWorker extends Thread {

	private ByteBuffer m_kinectData;
	private final BooleanMapManager m_obstacleMap = new BooleanMapManager();
	private boolean m_running = false;

	@Override
	public void run() {
		m_running = true;

		while(m_running) {
			if(m_kinectData != null) {
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
						if(tmpChunk.getOrigin().getX() == chunk.getOrigin().getX() && tmpChunk.getOrigin().getZ() == chunk.getOrigin().getZ()) {
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

				HeightMapManager ground = new HeightMapManager();


				double heightSum = 0;
				double voxelCount = 0;

				ChunkManager cleanedGroundChunks = new ChunkManager();

				for(Chunk chunk : groundChunks.getChunks()) {

					// check if there is a chunk below
					boolean skipChunk = false;
					for(Chunk tmpChunk : chunks.getChunks()) {
						if(tmpChunk.getOrigin().getX() == chunk.getOrigin().getX() && tmpChunk.getOrigin().getZ() == chunk.getOrigin().getZ()) {
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
											ground.setHeight(new Vec2i(globalPos.getX(), globalPos.getZ()), globalPos.getY());
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

				 /*

				// second cleaning

				for(Chunk chunk : cleanedGroundChunks.getChunks()) {

					// check if there is a chunk below
					boolean skipChunk = false;
					for(Chunk tmpChunk : chunks.getChunks()) {
						if(tmpChunk.getOrigin().getX() == chunk.getOrigin().getX() && tmpChunk.getOrigin().getZ() == chunk.getOrigin().getZ()) {
							if(tmpChunk.getOrigin().getY() < chunk.getOrigin().getY())
								skipChunk = true;
						}
					}
					// skip chunk if there is at least one below
					if(skipChunk) continue;


					Chunk currentChunk = chunk;

					int maxHeight = (int)Math.floor(heightSum / voxelCount) + 5;

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


				   */


				ChunkManager obstacles = new ChunkManager();

				for(Chunk chunk : chunks.getChunks()) {
					Chunk groundChunk = cleanedGroundChunks.getChunkAt(chunk.getOrigin());
					for(int x = 0; x < 64; x++) {
						for(int y = 0; y < 64; y++) {
							for(int z = 0; z < 64; z++) {
								if(chunk.getBlock(new Vec3i(x, y, z))) {

									Vec3i globalPos = new Vec3i(x + 64 * chunk.getOrigin().getX(), y + 64 * chunk.getOrigin().getY(), z + 64 * chunk.getOrigin().getZ());
									boolean hide = false;

									Integer groundHeight = ground.getHeight(new Vec2i(globalPos.getX(), globalPos.getZ()));
									if(groundHeight != null) {
										if(globalPos.getY() <= groundHeight) {
											hide = true;
										}
									}
									if(!hide) obstacles.setBlock(globalPos, true);
								}
							}
						}
					}
				}





				HeightMapManager obstacleHeightMap = new HeightMapManager();

				synchronized (m_obstacleMap) {

					m_obstacleMap.clear();

					for(Chunk chunk : obstacles.getChunks()) {

						// check if there is a chunk below
						boolean skipChunk = false;
						for(Chunk tmpChunk : chunks.getChunks()) {
							if(tmpChunk.getOrigin().getX() == chunk.getOrigin().getX() && tmpChunk.getOrigin().getZ() == chunk.getOrigin().getZ()) {
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

											obstacleHeightMap.setHeight(new Vec2i(globalPos.getX(), globalPos.getZ()), globalPos.getY());
											m_obstacleMap.setValue(new Vec2i(globalPos.getX(), globalPos.getZ()), true);
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
				}

			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public BooleanMapManager getObstacleMap() {
		return m_obstacleMap;
	}

	public void setKinectData(ByteBuffer buffer) {
		// receive Kinect data
		m_kinectData = buffer;
		m_kinectData.order(ByteOrder.LITTLE_ENDIAN);
	}

	public void end() {
		m_running = false;
	}
}
