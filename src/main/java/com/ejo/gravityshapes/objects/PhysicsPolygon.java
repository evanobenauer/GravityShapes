package com.ejo.gravityshapes.objects;

import com.ejo.glowlib.math.Angle;
import com.ejo.glowlib.math.MathE;
import com.ejo.glowlib.misc.ColorE;
import com.ejo.glowui.scene.Scene;
import com.ejo.glowui.scene.elements.shape.RegularPolygonUI;
import com.ejo.glowlib.math.Vector;
import com.ejo.uiphysics.elements.PhysicsDraggableUI;
import com.ejo.uiphysics.elements.PhysicsObjectUI;

public class PhysicsPolygon extends PhysicsDraggableUI {

    private final RegularPolygonUI rect;

    public PhysicsPolygon(RegularPolygonUI shape, double mass, Vector velocity, Vector netForce) {
        super(shape, mass, velocity, netForce);
        this.rect = shape;
        this.spin = shape.getRotation() == null ? 0 : shape.getRotation().getRadians();
        setDeltaT(.05f);
        setTickNetReset(true);
    }

    @Override
    public void tickElement(Scene scene, Vector mousePos) {
        super.tickElement(scene,mousePos);
        getPolygon().setRotation(new Angle(getSpin()));
    }

    //TODO: Make a "Bop" sound effect from the collision
    public void doCollision(PhysicsPolygon object) {
        double weight = getMass() / (object.getMass() + getMass());

        //Set states
        setCenter(getCenter().getMultiplied(weight).getAdded(object.getCenter().getMultiplied(1 - weight)));
        setVelocity(getVelocity().getMultiplied(weight).getAdded(object.getVelocity().getMultiplied(1 - weight)));

        //Set average color
        getPolygon().setColor(new ColorE((int)(getColor().getRed() * weight + object.getColor().getRed() * (1-weight)),(int)(getColor().getGreen() * weight + object.getColor().getGreen() * (1-weight)),(int)(getColor().getBlue() * weight + object.getColor().getBlue() * (1-weight))));

        //Set average polygon spin
        double spin = simplifyAngle(getSpin());
        double otherSpin = simplifyAngle(object.getSpin());
        while (spin > Math.PI * 2 / getPolygon().getVertexCount()) spin -= Math.PI * 2 / getPolygon().getVertexCount();
        while (otherSpin > Math.PI * 2 / object.getPolygon().getVertexCount()) otherSpin -= Math.PI * 2 / object.getPolygon().getVertexCount();
        setSpin(spin*weight + otherSpin*(1-weight));

        //Set average polygon type
        getPolygon().setVertexCount((int) MathE.roundDouble(getPolygon().getVertexCount() * weight + object.getPolygon().getVertexCount() * (1-weight),0));

        //Calculate conservation of angular momentum
        double angularWeight = getMomentOfInertia() / (getMomentOfInertia() + object.getMomentOfInertia());
        setOmega(getOmega() * angularWeight + object.getOmega() * (1-angularWeight));

        //Set mass and radius
        setMass(getMass() + object.getMass());
        double density = 1;
        double radius = Math.pow(getMass()/density * 3/(4*Math.PI), (double) 1/3); //Volume is calculated as a sphere
        getPolygon().setRadius(radius);

        //Set torque
        applyTorqueFromCollision(object,getDeltaT());

        //Set dragging
        if (object.isDragging()) setDragging(true);

        //Delete old object
        object.setDragging(false);
        object.setPhysicsDisabled(true);
        object.setEnabled(false);
    }

    public void applyTorqueFromCollision(PhysicsPolygon object, double collisionTime) {
        //Main object is reference frame. Other object is moving object

        //Calculate perpendicularity of velocity compared to the position
        Vector otherObjRefPos = object.getCenter().getSubtracted(getCenter());//VectorUtil.calculateVectorBetweenObjects(object,this);
        Vector otherObjRefVelocity = object.getVelocity().getSubtracted(getVelocity());
        double perpendicularity = otherObjRefPos.getUnitVector().getCross(otherObjRefVelocity.getUnitVector()).getMagnitude();

        //Calculate the sign of the velocity compared to the position
        int sign;
        Angle referencePosAngle = new Angle(-Math.atan2(otherObjRefPos.getY(),otherObjRefPos.getX())); //+180 to -180
        Angle referenceVelocityAngle = new Angle(-Math.atan2(otherObjRefVelocity.getY(),otherObjRefVelocity.getX())); //+180 to -180

        if (otherObjRefPos.getY() < 0) { //Top Half
            if (referenceVelocityAngle.getDegrees() + 180 > 240) referenceVelocityAngle = new Angle(referenceVelocityAngle.getRadians() - 2*Math.PI);
            sign = (referenceVelocityAngle.getDegrees() + 180 < referencePosAngle.getDegrees()) ? -1 : 1;
        } else { //Bottom Half
            if (referenceVelocityAngle.getDegrees() - 180 < -240) referenceVelocityAngle = new Angle(referenceVelocityAngle.getRadians() + 2*Math.PI);
            sign = (referenceVelocityAngle.getDegrees() - 180 < referencePosAngle.getDegrees()) ? -1 : 1;
        }

        //Set the net torque using the force between the two, the radius of the NEW object, and perpendicularity
        double rotForce = object.getMass()*Math.abs(0 - otherObjRefVelocity.getMagnitude()) / collisionTime;
        addTorque(rotForce * getPolygon().getRadius() * sign * perpendicularity);
    }

    private double simplifyAngle(double rad) {
        while (rad > Math.PI * 2) rad -= Math.PI * 2;
        while (rad < 0) rad += Math.PI * 2;
        return rad;
    }

    public void doWallBounce(Scene scene) {
        PhysicsObjectUI object = this;
        double mul = .1f;
        if (getPos().getX() + getPolygon().getRadius() > scene.getSize().getX()) {
            object.setVelocity(new Vector(-mul * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(scene.getSize().getX() - getPolygon().getRadius(), getPos().getY()));
        }
        if (getPos().getX() - getPolygon().getRadius() < 0.0) {
            object.setVelocity(new Vector(-mul * object.getVelocity().getX(), object.getVelocity().getY()));
            setPos(new Vector(getPolygon().getRadius(), getPos().getY()));
        }
        if (getPos().getY() + getPolygon().getRadius() > scene.getSize().getY()) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -mul * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), scene.getSize().getY() - getPolygon().getRadius()));
        }
        if (getPos().getY() - getPolygon().getRadius() < 0.0) {
            object.setVelocity(new Vector(object.getVelocity().getX(), -mul * object.getVelocity().getY()));
            setPos(new Vector(getPos().getX(), getPolygon().getRadius()));
        }
    }

    @Override
    public double getMomentOfInertia() {
        return (double) 2 /5 * getMass() * Math.pow(getPolygon().getRadius(),2);
    }

    @Override
    public void resetMovement() {
        setNetForce(Vector.NULL);
        setVelocity(Vector.NULL);
    }

    public RegularPolygonUI getPolygon() {
        return rect;
    }

}
