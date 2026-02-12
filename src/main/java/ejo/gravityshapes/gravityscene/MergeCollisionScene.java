package ejo.gravityshapes.gravityscene;

import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.element.base.Tickable;
import com.ejo.ui.element.polygon.RegularPolygon;
import com.ejo.util.math.Angle;
import com.ejo.util.math.Vector;
import ejo.gravityshapes.element.SpecialPhysicsObject;
import ejo.gravityshapes.util.CollisionUtil;

import java.awt.*;

public class MergeCollisionScene extends GravityScene {

    public MergeCollisionScene(int G, boolean wallBounce, boolean paths, boolean fieldLines, int objectCount, String spawnMode, int minM, int maxM) {
        super("Merge Collisions", G, wallBounce, paths, fieldLines, objectCount, spawnMode, minM, maxM);
    }

    @Override
    public void draw() {
        drawableElements.forIQueued((e,i) -> {
            if (e instanceof PhysicsObject obj) {
                //Update visual rotation
                ((RegularPolygon) obj.getElement()).setRotation(new Angle(obj.getTheta()));

                //Slow rotation back to stable over time? Like some kind of drag?
                float slowSpeed = 0f; //Currently disabled. I don't think I really like it...
                if (Math.abs(obj.getOmega()) > 0) {
                    if (obj.getOmega() > 0) obj.setOmega(obj.getOmega() - slowSpeed / 360);
                    if (obj.getOmega() < 0) obj.setOmega(obj.getOmega() + slowSpeed / 360);
                }
            }
        });
        super.draw();
    }

    @Override
    public void tick() {
        for (Tickable e1 : tickables) {
            if (!(e1 instanceof PhysicsObject obj1)) continue;
            if (tickables.isRemovalQueued(obj1)) continue;

            //Collisions
            for (Tickable e2 : tickables) {
                if (e1.equals(e2) || !(e2 instanceof PhysicsObject obj2)) continue;
                if (tickables.isRemovalQueued(obj2)) continue;
                doCollisionMerge(obj1, obj2);
            }
        }
        super.tick();
    }

    //TODO: Make a "Bop" sound effect from the collision
    //TODO: Spawn extra mini particles that randomly explode from the main particle upon the collision?
    // Release the particles from the point of collision. Have them fly out depending on the side of both particles.
    // If particles are more similar in size, they will fly out tangentially. If one is much bigger than the other,
    // they will fly out in the direction of the smaller particle
    public void doCollisionMerge(PhysicsObject obj1, PhysicsObject obj2) {
        RegularPolygon poly1 = ((RegularPolygon) obj1.getElement());
        RegularPolygon poly2 = ((RegularPolygon) obj2.getElement());
        if (!CollisionUtil.isColliding(poly1, poly2)) return;

        double massWeight = obj1.getMass() / (obj2.getMass() + obj1.getMass());
        double angularWeight = obj1.getRotationalInertia() / (obj1.getRotationalInertia() + obj2.getRotationalInertia());

        //============= Rotations ==============

        //Set Average Polygon Spin
        Angle t1 = new Angle(obj1.getTheta()).simplify();
        Angle t2 = new Angle(obj2.getTheta()).simplify();
        double spin1 = t1.getRadians();
        double spin2 = t2.getRadians();
        while (spin1 > Math.PI * 2 / poly1.getVertexCount()) spin1 -= Math.PI * 2 / poly1.getVertexCount();
        while (spin2 > Math.PI * 2 / poly2.getVertexCount()) spin2 -= Math.PI * 2 / poly2.getVertexCount();
        obj1.setTheta(spin1*massWeight + spin2*(1-massWeight));

        //Set Conservation of Angular Momentum
        obj1.setOmega(obj1.getOmega() * angularWeight + obj2.getOmega() * (1 - angularWeight));

        //Set Torque
        applyTorqueFromCollision(obj1, obj2, obj1.getDeltaT());

        //============= Linear ==============

        //Set Conservation of Linear Momentum
        obj1.setVelocity(obj1.getVelocity().getMultiplied(massWeight).getAdded(obj2.getVelocity().getMultiplied(1 - massWeight)));

        //Set Mass and Radius
        obj1.setMass(obj1.getMass() + obj2.getMass());
        double p = 1;
        double radius = Math.pow(3 * obj1.getMass() / (4 * Math.PI * p), 1f / 3); //3D
        poly1.setRadius(radius);

        //============= Visuals ==============

        //Set Average Color
        int red = (int) Math.round(poly1.getColor().getRed() * massWeight + poly2.getColor().getRed() * (1 - massWeight));
        int green = (int) Math.round(poly1.getColor().getGreen() * massWeight + poly2.getColor().getGreen() * (1 - massWeight));
        int blue = (int) Math.round(poly1.getColor().getBlue() * massWeight + poly2.getColor().getBlue() * (1 - massWeight));
        int alpha = (int) Math.round(poly1.getColor().getAlpha() * massWeight + poly2.getColor().getAlpha() * (1 - massWeight));
        poly1.setColor(new Color(red, green, blue, alpha));

        //Set Average Polygon Type
        poly1.setVertexCount((int) Math.round(poly1.getVertexCount() * massWeight + poly2.getVertexCount() * (1 - massWeight)));

        //============= Attributes ==============

        //Set Averaged Position
        obj1.setPos(obj1.getPos().getMultiplied(massWeight).getAdded(obj2.getPos().getMultiplied(1 - massWeight)));

        //Update Rotational Inertia
        obj1.setRotationalInertia(2f / 5 * obj1.getMass() * Math.pow(poly1.getRadius(),2));

        //Set collision heat
        //setCollisionHeat(obj1,obj2,massWeight);

        //============= Interactable ==============

        //Set Dragging
        if (((SpecialPhysicsObject) obj2).isDragging())
            ((SpecialPhysicsObject) obj1).setDragging(true);
        ((SpecialPhysicsObject) obj2).setDragging(false);

        //Set Locked
        if (((SpecialPhysicsObject) obj2).isLocked()) {
            ((SpecialPhysicsObject) obj1).setLocked(true);
            ((SpecialPhysicsObject) obj1).setLockedPos(((SpecialPhysicsObject) obj2).getLockedPos());
        }
        ((SpecialPhysicsObject) obj2).setLocked(false);

        //============= Finalize ==============

        //Delete old object
        removeElement(obj2, true);
    }

    @Deprecated
    private void setCollisionHeat(PhysicsObject obj1, PhysicsObject obj2, double massWeight) {
        Vector obj2RefVelocity = obj2.getVelocity().getSubtracted(obj1.getVelocity());
        int heat = (int)(.5f *obj2.getMass()*Math.pow(obj2RefVelocity.getMagnitude(),2)) /1000;
        ((SpecialPhysicsObject)obj1).heat = (int) (((SpecialPhysicsObject)obj1).heat * massWeight + ((SpecialPhysicsObject)obj2).heat * (1-massWeight));
        ((SpecialPhysicsObject) obj1).heat += heat;
    }

    public void applyTorqueFromCollision(PhysicsObject obj1, PhysicsObject obj2, double collisionTime) {
        //Main object is reference frame. Other object is moving object

        //Calculate perpendicularity of velocity compared to the position
        Vector obj2RefPos = obj2.getPos().getSubtracted(obj1.getPos());//VectorUtil.calculateVectorBetweenObjects(object,this);
        Vector obj2RefVelocity = obj2.getVelocity().getSubtracted(obj1.getVelocity());
        double perpendicularity = obj2RefPos.getUnitVector().getCross(obj2RefVelocity.getUnitVector()).getMagnitude();

        //Calculate the sign of the velocity compared to the position
        //This is some weird wizardry
        int sign;
        Angle referencePosAngle = new Angle(-Math.atan2(obj2RefPos.getY(), obj2RefPos.getX())); //+180 to -180
        Angle referenceVelocityAngle = new Angle(-Math.atan2(obj2RefVelocity.getY(), obj2RefVelocity.getX())); //+180 to -180
        if (obj2RefPos.getY() < 0) { //Top Half
            if (referenceVelocityAngle.getDegrees() + 180 > 240)
                referenceVelocityAngle = new Angle(referenceVelocityAngle.getRadians() - 2 * Math.PI);
            sign = (referenceVelocityAngle.getDegrees() + 180 < referencePosAngle.getDegrees()) ? -1 : 1;
        } else { //Bottom Half
            if (referenceVelocityAngle.getDegrees() - 180 < -240)
                referenceVelocityAngle = new Angle(referenceVelocityAngle.getRadians() + 2 * Math.PI);
            sign = (referenceVelocityAngle.getDegrees() - 180 < referencePosAngle.getDegrees()) ? -1 : 1;
        }

        //Set the net torque using the force between the two, the radius of the NEW object, and perpendicularity
        double rotForce = obj2.getMass() * Math.abs(0 - obj2RefVelocity.getMagnitude()) / collisionTime;
        obj1.addTorque(rotForce * ((RegularPolygon) obj1.getElement()).getRadius() * sign * perpendicularity);
    }
}
