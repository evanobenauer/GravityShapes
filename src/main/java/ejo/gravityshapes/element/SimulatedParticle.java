package ejo.gravityshapes.element;

import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.util.math.Vector;

import java.util.ArrayList;

public class SimulatedParticle {

    private final PhysicsObject physicsObject;
    private int steps;

    public SimulatedParticle(PhysicsObject physicsObject, int steps) {
        this.physicsObject = physicsObject;
        this.steps = steps;
    }

    public Vector[] getFuturePositions(SimulatedForceCalculation forceCalculation, ManualLinearFutureAttributeModifier attributeModifier) {
        ArrayList<Vector> positions = new ArrayList<>();

        Vector simulatedForce;
        Vector futureVelocity = physicsObject.getVelocity();
        Vector futurePos = physicsObject.getPos();

        positions.add(futurePos); //Add initial position to the list? Potentially remove this?

        for (int i = 0; i < steps; i++) {
            simulatedForce = forceCalculation.run(physicsObject,futurePos,i);
            Vector fakeAcceleration = simulatedForce.getMultiplied(1/ physicsObject.getMass());
            futureVelocity = futureVelocity.getAdded(fakeAcceleration.getMultiplied(physicsObject.getDeltaT()));
            futurePos = futurePos.getAdded(futureVelocity.getMultiplied(physicsObject.getDeltaT()));
            attributeModifier.run(physicsObject,futurePos,futureVelocity,simulatedForce,i);
            positions.add(futurePos);
        }
        return positions.toArray(new Vector[0]);
    }

    public Vector[] getFuturePositions(SimulatedForceCalculation forceCalculation) {
        return getFuturePositions(forceCalculation,(a,b,c,d,e) -> {});
    }

    public Double[] getFutureRotations(SimulatedTorqueCalculation torqueCalculation, ManualRotationalAttributeModifier attributeModifier) {
        ArrayList<Double> rotations = new ArrayList<>();

        double simulatedTorque;
        double futureOmega = physicsObject.getOmega();
        double futureTheta = physicsObject.getTheta();

        rotations.add(futureTheta); //Add original rotation to the list.. potentially remove

        for (int i = 0; i < steps; i++) {
            simulatedTorque = torqueCalculation.run(physicsObject,futureTheta,i);
            double fakeAlpha = simulatedTorque / physicsObject.getRotationalInertia();
            futureOmega += fakeAlpha * physicsObject.getDeltaT();
            futureTheta += futureOmega * physicsObject.getDeltaT();
            attributeModifier.run(physicsObject,futureTheta,futureOmega,simulatedTorque,i);
            rotations.add(futureTheta);
        }
        return rotations.toArray(new Double[0]);
    }

    public Double[] getFutureRotations(SimulatedTorqueCalculation torqueCalculation) {
        return getFutureRotations(torqueCalculation,(a,b,c,d,e) ->{});
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }


    public PhysicsObject getAtPos(Vector pos) {
        PhysicsObject obj = this.physicsObject.clone();
        obj.setPos(pos);
        return obj;
    }

    public PhysicsObject getPhysicsObject() {
        return physicsObject;
    }

    @FunctionalInterface
    public interface SimulatedForceCalculation {
        Vector run(PhysicsObject obj, Vector stepPos, int step);
    }

    @FunctionalInterface
    public interface ManualLinearFutureAttributeModifier {
        void run(PhysicsObject obj, Vector futurePos, Vector futureVelocity, Vector futureForce, int step);
    }
    

    @FunctionalInterface
    public interface SimulatedTorqueCalculation {
        double run(PhysicsObject obj, double stepTheta, int step);
    }

    @FunctionalInterface
    public interface ManualRotationalAttributeModifier {
        void run(PhysicsObject obj, double futureTheta, double futureOmega, double futureTorque, int step);
    }
}
