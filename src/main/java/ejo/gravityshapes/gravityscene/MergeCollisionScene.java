package ejo.gravityshapes.gravityscene;

import com.ejo.ui.element.base.Tickable;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.ui.element.simulation.PhysicsObject;
import com.ejo.util.math.Angle;
import com.ejo.util.math.Vector;
import ejo.gravityshapes.util.CollisionUtil;

import java.awt.*;

public class MergeCollisionScene extends GravityScene {

    public MergeCollisionScene(boolean applyGravity, boolean wallBounce, int objectCount, String spawnMode, int minM, int maxM) {
        super("Merge Collisions",applyGravity, wallBounce, objectCount, spawnMode, minM, maxM);
    }

    @Override
    public void draw() {
        //Update visual rotation
        drawableElements.forIQueued((e) -> {
            if (e instanceof PhysicsObject obj)
                ((RegularPolygon) obj.getElement()).setRotation(new Angle(obj.getTheta()));
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
                doCollisionMerge(obj1,obj2);
            }
        }
        super.tick();
    }

    private void doCollisionMerge(PhysicsObject obj1, PhysicsObject obj2) {
        if (CollisionUtil.isColliding(((RegularPolygon)obj1.getElement()),((RegularPolygon)obj2.getElement()))) {
            //Update Velocity
            double addedMass = obj1.getMass() + obj2.getMass();
            Vector newV = obj1.getVelocity().getMultiplied(obj1.getMass()).getAdded(obj2.getVelocity().getMultiplied(obj2.getMass())).getMultiplied(1/addedMass);
            obj1.setVelocity(newV);
            obj1.setMass(addedMass);

            //Set Pos
            double weight = obj1.getMass() / (obj2.getMass() + obj1.getMass());
            obj1.setPos(obj1.getPos().getMultiplied(weight).getAdded(obj2.getPos().getMultiplied(1 - weight)));

            //Update Radius
            double p = 1f;
            double rn = Math.pow(3 * obj1.getMass() / (4 * Math.PI * p),1f/3); //3D
            ((RegularPolygon) obj1.getElement()).setRadius(rn);

            //Update Vertex Count
            int v1 = ((RegularPolygon) obj1.getElement()).getVertexCount();
            int v2 = ((RegularPolygon) obj2.getElement()).getVertexCount();
            ((RegularPolygon) obj1.getElement()).setVertexCount((int) Math.round(v1 * weight + v2 * (1-weight)));

            //Update Color
            Color c1 = ((RegularPolygon)obj1.getElement()).getColor();
            Color c2 = ((RegularPolygon)obj2.getElement()).getColor();
            double cWeight = Math.clamp(weight,0,1);
            int red = (int)(c1.getRed() * cWeight + c2.getRed() * (1-cWeight));
            int green = (int)(c1.getGreen() * cWeight + c2.getGreen() * (1-cWeight));
            int blue = (int)(c1.getBlue() * cWeight + c2.getBlue() * (1-cWeight));
            int alpha = (int)(c1.getAlpha() * cWeight + c2.getAlpha() * (1-cWeight));
            ((RegularPolygon)obj1.getElement()).setColor(new Color(red,green,blue,alpha));

            //TODO: Add Rotation Motion data here too.

            removeElement(obj2,true);
            //drawableElements.remove(obj2);
        }
    }
}
