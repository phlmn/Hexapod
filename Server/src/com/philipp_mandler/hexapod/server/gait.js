importPackage(com.philipp_mandler.hexapod.server);
importPackage(com.philipp_mandler.hexapod.hexapod);

var legs = robot.getLegs();

var triangle1 = new LegGroup(new Array(legs[0], legs[3], legs[4]), new Array(new Vec2(-230, 310), new Vec2(320, 0), new Vec2(-230, -310)));
var triangle2 = new LegGroup(new Array(legs[1], legs[2], legs[5]), new Array(new Vec2(230, 310), new Vec2(-320, 0), new Vec2(230, -310)));

var legPosition = new Vec2();

var z1 = 0;
var z2 = 0;

var bodyHeight = -30;
var defaultHeight = 100;

var stepHeight = 30;

var range = 60;

var direction = true;

var switchHeight = false;

var init = false;

var Module = function() {
	triangle1.setTranslation(new Vec3());
	triangle2.setTranslation(new Vec3());
};

Module.prototype.walk = function(time, speed) {
	

	if(!init) {
		if(bodyHeight < defaultHeight) {
			bodyHeight += time / 100;
		}
		else if(z1 < stepHeight) {
			z1 += time / 100;
		}
		else {
			z1 = stepHeight;
			bodyHeight = defaultHeight;
			init = true;
		}
	}
	else {
	
		if(legPosition.getLength() > range) {
			direction = !direction;
			legPosition.normalize();
			legPosition.multiply(range);
			switchHeight = true;
		}

		var walkingSign = -1;
		if(direction)
			walkingSign = 1;	

		if(switchHeight) {
			if(direction) {
				if(z1 < 30)
					z1 += time / 10;
				else if(z2 > 0)
					z2 -= time / 10;
				else
					switchHeight = false;
			}
			else {
				if(z1 > 0)
					z1 -= time / 10;
				else if(z2 < 30)
					z2 += time / 10;
				else
					switchHeight = false;
			}
		}
		else {

			legPosition.setY(legPosition.getY() + walkingSign * (time * speed.getY() / 100));
			legPosition.setX(legPosition.getX() + walkingSign * (time * speed.getX() / 100));
		}
	}

	triangle1.setTranslation(new Vec3(legPosition.getX(), legPosition.getY(), -z1 - bodyHeight));
	triangle1.moveLegs();

	triangle2.setTranslation(new Vec3(-legPosition.getX(), -triangle1.getTranslation().getY(), -z2 - bodyHeight));
	triangle2.moveLegs();

}

var module = new Module();