package com.philipp_mandler.hexapod.server;

public class Data {
	// leg lengths
	static float upperLeg = 140;
	static float lowerLeg = 218;

	// servo rotation calibration
	static double servoAngleOffsets[] = {
			0, 0, -0.559,
			0, 0, 0.506,
			0, 0, -0.5,
			0, 0, 0.489,
			0, 0, -0.559,
			0, 0, 0.576
	};

	// height offset from second leg servo
	static double servoPosOffsetZ = -50;

}
