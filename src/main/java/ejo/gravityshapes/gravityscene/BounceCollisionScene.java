package ejo.gravityshapes.gravityscene;

import com.ejo.ui.element.base.Tickable;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.util.math.Vector;

//TODO: This is unimplemented. Implement this
public class BounceCollisionScene extends GravityScene {

    public BounceCollisionScene(boolean applyGravity, boolean wallBounce, int objectCount, String spawnMode, int minM, int maxM) {
        super("Collisions",applyGravity,wallBounce,objectCount,spawnMode, minM,maxM);
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
                doCollisionWeird(obj1,obj2);
            }
        }
        super.tick();
    }

    @Deprecated
    private void doCollisionWeird(PhysicsObject obj1, PhysicsObject obj2) {
        Vector r = obj2.getPos().getSubtracted(obj1.getPos());
        double objectDistance = r.getMagnitude();
        boolean colliding =  objectDistance <= ((RegularPolygon)obj1.getElement()).getRadius() + ((RegularPolygon)obj2.getElement()).getRadius();
        if (colliding) {
            //Old but still cool
            Vector avgV = obj1.getVelocity().getAdded(obj2.getVelocity()).getMultiplied(.5);
            obj2.setVelocity(avgV);
            obj1.setVelocity(avgV);
        }
    }
}
