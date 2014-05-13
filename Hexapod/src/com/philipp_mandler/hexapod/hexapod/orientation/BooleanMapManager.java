package com.philipp_mandler.hexapod.hexapod.orientation;

import com.philipp_mandler.hexapod.hexapod.Vec2i;

import java.util.ArrayList;

public class BooleanMapManager {

	private ArrayList<BooleanMap> m_booleanMaps = new ArrayList<>();

	public void setValue(Vec2i pos, boolean value) {

		Vec2i mapOrigin = new Vec2i(pos.getX() / 64, pos.getY() / 64);

		if(pos.getX() < 0)
			mapOrigin.setX(mapOrigin.getX() - 1);

		if(pos.getY() < 0)
			mapOrigin.setY(mapOrigin.getY() - 1);

		BooleanMap destinationMap = null;
		for(BooleanMap map : m_booleanMaps) {
			if(mapOrigin.equals(map.getOrigin())) {
				destinationMap = map;
				break;
			}
		}

		if(destinationMap == null) {
			destinationMap = new BooleanMap(mapOrigin);
			m_booleanMaps.add(destinationMap);
		}

		destinationMap.setValue(new Vec2i(superModulo(pos.getX(), 64), superModulo(pos.getY(), 64)), value);
	}

	private int superModulo(int a, int b) {
		return (a % b + b) % b;
	}

	public ArrayList<BooleanMap> getBooleanMaps() {
		return m_booleanMaps;
	}

	public BooleanMap getBooleanMapAt(Vec2i pos) {
		for(BooleanMap map : m_booleanMaps) {
			if(map.getOrigin().equals(pos))
				return map;
		}

		return null;
	}

	public boolean getValue(Vec2i pos) {

		Vec2i mapOrigin = new Vec2i(pos.getX() / 64, pos.getY() / 64);

		if(pos.getX() < 0)
			mapOrigin.setX(mapOrigin.getX() - 1);

		if(pos.getY() < 0)
			mapOrigin.setY(mapOrigin.getY() - 1);

		BooleanMap sourceMap = null;
		for(BooleanMap map : m_booleanMaps) {
			if(mapOrigin.equals(map.getOrigin())) {
				sourceMap = map;
				break;
			}
		}

		if(sourceMap == null) {
			return false;
		}

		return sourceMap.getValue(new Vec2i(superModulo(pos.getX(), 64), superModulo(pos.getY(), 64)));
	}

	public void replaceBooleanMap(BooleanMap booleanMap) {
		if(booleanMap.getOrigin() == null) return;

		for(BooleanMap map : m_booleanMaps) {
			if(map.getOrigin().equals(booleanMap.getOrigin())) {
				m_booleanMaps.remove(map);
				break;
			}
		}

		m_booleanMaps.add(booleanMap);
	}

	public void clear() {
		m_booleanMaps.clear();
	}

}
