package com.philipp_mandler.hexapod.hexapod.orientation;

import com.philipp_mandler.hexapod.hexapod.Vec2i;

import java.util.ArrayList;

public class HeightMapManager {

	private ArrayList<HeightMap> m_heightMaps = new ArrayList<>();

	public void setHeight(Vec2i pos, Integer height) {

		Vec2i mapOrigin = new Vec2i(pos.getX() / 64, pos.getY() / 64);

		if(pos.getX() < 0)
			mapOrigin.setX(mapOrigin.getX() - 1);

		if(pos.getY() < 0)
			mapOrigin.setY(mapOrigin.getY() - 1);

		HeightMap destinationMap = null;
		for(HeightMap map : m_heightMaps) {
			if(map.getOrigin().equals(mapOrigin)) {
				destinationMap = map;
				break;
			}
		}

		if(destinationMap == null) {
			destinationMap = new HeightMap(mapOrigin);
			m_heightMaps.add(destinationMap);
		}

		destinationMap.setHeight(new Vec2i(superModulo(pos.getX(), 64), superModulo(pos.getY(), 64)), height);
	}

	private int superModulo(int a, int b) {
		return (a % b + b) % b;
	}

	public ArrayList<HeightMap> getHeightMaps() {
		return m_heightMaps;
	}

	public HeightMap getHeightMapAt(Vec2i pos) {
		for(HeightMap map : m_heightMaps) {
			if(map.getOrigin().equals(pos))
				return map;
		}

		return null;
	}

	public Integer getHeight(Vec2i pos) {

		Vec2i mapOrigin = new Vec2i(pos.getX() / 64, pos.getY() / 64);

		if(pos.getX() < 0)
			mapOrigin.setX(mapOrigin.getX() - 1);

		if(pos.getY() < 0)
			mapOrigin.setY(mapOrigin.getY() - 1);

		HeightMap sourceMap = null;
		for(HeightMap map : m_heightMaps) {
			if(map.getOrigin().equals(mapOrigin)) {
				sourceMap = map;
				break;
			}
		}

		if(sourceMap == null) {
			return 0;
		}

		return sourceMap.getHeight(new Vec2i(superModulo(pos.getX(), 64), superModulo(pos.getY(), 64)));
	}

	public void replaceHeightMap(HeightMap heightMap) {
		if(heightMap.getOrigin() == null) return;

		for(HeightMap map : m_heightMaps) {
			if(map.getOrigin().equals(heightMap.getOrigin())) {
				m_heightMaps.remove(map);
				break;
			}
		}

		m_heightMaps.add(heightMap);
	}

	public void clear() {
		m_heightMaps.clear();
	}

}
