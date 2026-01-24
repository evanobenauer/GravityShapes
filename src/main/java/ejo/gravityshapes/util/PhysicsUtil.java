package ejo.gravityshapes.util;

import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.util.math.Vector;

public class PhysicsUtil {

    public static Vector getGravityForce(PhysicsObject obj1, PhysicsObject obj2) {
        return getGravityField(obj2,obj1.getPos()).getMultiplied(obj1.getMass());
    }

    public static Vector getGravityField(PhysicsObject obj, Vector pos) {
        double G = 1;
        Vector r = obj.getPos().getSubtracted(pos);
        if (r.getMagnitude() == 0) return new Vector(0,0);
        Vector rHat = r.getUnitVector();
        double rMag = r.getMagnitude();

        double magnitude = G * obj.getMass() / Math.pow(rMag, 2);
        return rHat.multiply(magnitude);
    }

    //For Collisionless things. Add this to getGravityField
    //         if (r.getMagnitude() <= (((RegularPolygon)obj1.getPolygon()).getRadius() + ((RegularPolygon)obj2.getPolygon()).getRadius()) / 2)
    //            return new Vector(0, 0);

}
