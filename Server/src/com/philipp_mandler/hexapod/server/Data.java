package com.philipp_mandler.hexapod.server;

public class Data {
	static float upperLeg = 140;
	static float lowerLeg = 215;

	static double servoAngleOffsets[] = {
		0, -0.314, -0.559,
		0, 0.314, 0.506,
		0, 0.07, -0.593,
		0, 0.314, 0.489,
		0, -0.384, -0.559,
		0, 0.314, 0.576
	};

	static double servoPosOffsetZ = -50;

}
