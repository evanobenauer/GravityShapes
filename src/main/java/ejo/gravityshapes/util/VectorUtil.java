package ejo.gravityshapes.util;

import com.ejo.ui.element.PhysicsObject;
import com.ejo.util.math.Vector;

public class VectorUtil {

    public static Vector calculateVectorBetween(PhysicsObject object1, PhysicsObject object2) {
        return object1.getPos().getSubtracted(object2.getPos());
    }

    public static Vector calculateVectorBetween(Vector point1, Vector point2) {
        return point1.getSubtracted(point2);
    }

    public static Vector calculateVectorBetween(PhysicsObject object, Vector point) {
        return object.getPos().getSubtracted(point);
    }
}
