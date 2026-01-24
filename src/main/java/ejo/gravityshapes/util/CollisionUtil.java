package ejo.gravityshapes.util;

import com.ejo.ui.Scene;
import com.ejo.ui.element.shape.Circle;
import com.ejo.ui.element.shape.ConvexPolygon;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.util.math.Vector;

public class CollisionUtil {


    public static boolean isColliding(ConvexPolygon poly1, ConvexPolygon poly2) {
        return false; //TODO: Do an SAT thing here
    }

    public static boolean isColliding(RegularPolygon poly1, RegularPolygon poly2) {
        //TODO: Swap this over to SAT when you finish it w/ a radius check at the beginning
        Vector r = poly2.getPos().getSubtracted(poly1.getPos());
        double objectDistance = r.getMagnitude();
        return objectDistance <= poly1.getRadius() + poly2.getRadius();
    }

    public static boolean isColliding(Circle c1, Circle c2) {
        Vector r = c2.getPos().getSubtracted(c1.getPos());
        double objectDistance = r.getMagnitude();
        return objectDistance <= c1.getRadius() + c2.getRadius();
    }

    public static void doWallBounce(PhysicsObject obj, double restitution) {
        doWallBounce(obj.getScene(),obj.getPos(),obj.getVelocity(),restitution);
    }

    public static void doWallBounce(Scene scene, Vector pos, Vector vel, double restitution) {
        if (pos.getX() > scene.getWindow().getSize().getX()) {
            vel.scale(new Vector(-restitution, 1));
            pos.setX(scene.getWindow().getSize().getX());
        }
        if (pos.getX() < 0) {
            vel.scale(new Vector(-restitution, 1));
            pos.setX(0);
        }
        if (pos.getY() > scene.getWindow().getSize().getY()) {
            vel.scale(new Vector(1, -restitution));
            pos.setY(scene.getWindow().getSize().getY());
        }
        if (pos.getY() < 0) {
            vel.scale(new Vector(1, -restitution));
            pos.setY(0);
        }
    }

}
