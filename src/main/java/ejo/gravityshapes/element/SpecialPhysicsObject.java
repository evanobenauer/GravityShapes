package ejo.gravityshapes.element;

import com.ejo.ui.Scene;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.element.base.Interactable;
import com.ejo.ui.element.polygon.RegularPolygon;
import com.ejo.util.math.Vector;
import ejo.gravityshapes.util.VectorUtil;
import org.lwjgl.glfw.GLFW;

//Just a physics object that can be locked, dragged, and has heat?
public class SpecialPhysicsObject extends PhysicsObject implements Interactable {

    private boolean dragging;
    private Vector mouseOffset;

    private boolean locked;
    private Vector lockedPos;

    public int heat;

    public SpecialPhysicsObject(Scene scene, Vector pos, ObjectsPolygon element) {
        super(scene, pos, element);
        this.dragging = false;
        this.mouseOffset = Vector.NULL();
        this.heat = 0;
        this.locked = false;
        this.lockedPos = Vector.NULL();
    }


    @Override
    public void draw(Vector mousePos) {
        //drawWithHeat(mousePos);
        super.draw(mousePos);
        if (locked) ((ObjectsPolygon)getPolygon()).drawBlueOutline();
    }

    @Override
    public void tick(Vector mousePos) {
        if (locked) {
            setPos(lockedPos);
            setVelocity(Vector.NULL());
        }
        if (dragging) {
            Vector pos = mousePos.getAdded(mouseOffset);
            this.lockedPos = pos;
            setPos(pos);
            setVelocity(Vector.NULL());
        }
        //updateHeat();
        super.tick(mousePos);
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (isMouseHovered() && action == GLFW.GLFW_PRESS) {
                mouseOffset = VectorUtil.calculateVectorBetween(this, mousePos);
                dragging = true;
            }

            if (action == GLFW.GLFW_RELEASE) {
                mouseOffset = Vector.NULL();
                dragging = false;
            }
        }
        if (isMouseHovered() && button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && action == GLFW.GLFW_RELEASE) {
            lockedPos = getPos().clone();
            locked = !locked;
        }
    }

    @Override
    public void onMouseScroll(double scroll, Vector mousePos) {
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setLockedPos(Vector lockedPos) {
        this.lockedPos = lockedPos;
    }

    public boolean isDragging() {
        return dragging;
    }

    public boolean isLocked() {
        return locked;
    }

    public Vector getLockedPos() {
        return lockedPos;
    }

    public RegularPolygon getPolygon() {
        return (RegularPolygon) getElement();
    }


    @Deprecated
    private void drawWithHeat(Vector mousePos) {
        //Color oC = getPolygon().getColor();
        //Color c = clampedColor(oC.getRed() + heat,oC.getGreen() - heat / 100f, oC.getBlue() - heat / 100f,oC.getAlpha());
        //getPolygon().setColor(c);
        super.draw(mousePos);
        //getPolygon().setColor(oC);
    }

    private void updateHeat() {
        this.heat = Math.clamp(heat,0,5120);
        float speed = (int) (Math.exp(heat / 1000f));
        if (heat > 0) heat -= speed;
    }
}