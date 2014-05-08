package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec2i;
import com.philipp_mandler.hexapod.hexapod.net.NetPackage;
import com.philipp_mandler.hexapod.hexapod.orientation.BooleanMapManager;

import java.util.ArrayList;

public class AutoModule extends Module {

	private VisionModule m_visionModule;
	private MobilityModule m_mobilityModule;

	private Thread m_thread;

	private boolean m_running = false;

	private ArrayList<BooleanMapManager> m_obstacleMaps = new ArrayList<>();

	private final Vec2 m_walkingSpeed = new Vec2();


	public AutoModule() {
		super.setName("auto");
	}

	@Override
	protected void onStart() {

		m_visionModule = (VisionModule)Main.getModuleManager().getModule("vision");
		m_mobilityModule = (MobilityModule)Main.getModuleManager().getModule("mobility");

		if(!m_visionModule.isRunning()) m_visionModule.start();
		if(!m_mobilityModule.isRunning()) m_mobilityModule.start();

		m_mobilityModule.lift();

		m_thread = new Thread(new Runnable() {
			@Override
			public void run() {
				m_running = true;

				while(m_running) {
					BooleanMapManager obstacleMap =  m_visionModule.getObstacleMap();

					if(obstacleMap != null) {

						m_obstacleMaps.add(obstacleMap);

						while(m_obstacleMaps.size() > 4) {
							m_obstacleMaps.remove(0);
						}

						int obstacles = 0;

						for(int y = 0; y < 30; y++) {
							for(int x = -10; x < 10; x++) {
								int tmpObstacles = 0;
								for(BooleanMapManager map : m_obstacleMaps) {
									if(map.getValue(new Vec2i(x, y))) {
										tmpObstacles++;
									}
								}
								if(tmpObstacles == m_obstacleMaps.size())
									obstacles++;
							}
						}

						/*for(BooleanMap map : obstacleMap.getBooleanMaps()) {
							Main.getNetworking().broadcast(new BooleanMapPackage(map));
						} */

						if(obstacles == 0) {
							if(m_mobilityModule.lifted()) {
								synchronized (m_walkingSpeed) {
									m_walkingSpeed.setX(0);
									m_walkingSpeed.setY(0.6);
									m_mobilityModule.setRotationSpeed(0);
								}
							}
						}
						else {
							synchronized (m_walkingSpeed) {
								m_walkingSpeed.setX(0);
								m_walkingSpeed.setY(0);
								m_mobilityModule.setRotationSpeed(0.4);
							}
						}

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		});

		m_thread.start();

	}

	@Override
	protected void onStop() {
		m_running = false;
		m_thread = null;
	}

	@Override
	public void tick(long tick, Time elapsedTime) {
		m_mobilityModule.setWalkingSpeed(new Vec2(m_walkingSpeed));
	}

	@Override
	public void onDataReceived(ClientWorker client, NetPackage pack) {

	}

	@Override
	public void onCmdReceived(ClientWorker client, String[] cmd) {

	}

	@Override
	public void onClientDisconnected(ClientWorker client) {

	}

	@Override
	public void onClientConnected(ClientWorker client) {

	}
}
