package com.philipp_mandler.hexapod.server;

import com.philipp_mandler.hexapod.hexapod.Vec2;
import com.philipp_mandler.hexapod.hexapod.Vec3;

public class LegGroup {
	
	private Leg[] m_legs;
	private Vec2[] m_relativePositions;
	
	private Vec3 m_translation;
	private Vec3 m_rotation;
	
	
	public LegGroup(Leg[] legs, Vec2[] relativePositions) {
		
		m_legs = legs;		
		m_relativePositions = relativePositions;
		
		m_translation = new Vec3();
		m_rotation = new Vec3();
	}
	
	public void moveLegs() {
		
		for(int i = 0; i < m_legs.length; i++) {
			if(m_legs[i] == null || m_relativePositions[i] == null) continue;
			//Vec2 tmpPoint = new Vec2(m_relativePositions[i]);
			//tmpPoint.rotate(new Vec2(), m_rotation.getZ());

			Vec3 tmpPoint = new Vec3(m_relativePositions[i], 0);

			tmpPoint.rotate(m_rotation);


			Vec3 finalPoint = new Vec3(tmpPoint.getX() + m_translation.getX(), tmpPoint.getY() + m_translation.getY(), tmpPoint.getZ() + m_translation.getZ());

			m_legs[i].setGoalPosition(finalPoint);
			if(i == 0) {
				//DebugHelper.log(finalPoint.getX() + "  " + finalPoint.getY() + "  " + finalPoint.getZ());
			}
		}
	}
	
	public Vec3 getTranslation() {
		return m_translation;
	}
	
	public void setTranslation(Vec3 translation) {
		m_translation = translation;
	}
	
	public void translate(Vec3 relativePos) {
		m_translation.add(relativePos);
	}
	
	public Vec3 getRotation() {
		return m_rotation;
	}
	
	public void setRotation(Vec3 rotation) {
		m_rotation = rotation;
	}
	
	public Leg[] getLegs() {
		return m_legs;
	}
	
	public void setLegs(Leg[] legs) {
		m_legs = legs;
	}
	
	public Vec2[] getRelativePositions() {
		return m_relativePositions;
	}
	
	public void setRelativePositions(Vec2[] relativePositions) {
		m_relativePositions = relativePositions;
	}

}
