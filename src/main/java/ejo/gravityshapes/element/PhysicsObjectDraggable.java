package ejo.gravityshapes.element;

import com.ejo.ui.Scene;
import com.ejo.ui.element.DrawableElement;
import com.ejo.ui.element.PhysicsObject;
import com.ejo.ui.element.base.Interactable;
import com.ejo.ui.element.shape.RegularPolygon;
import com.ejo.util.math.Vector;
import ejo.gravityshapes.util.VectorUtil;
import org.lwjgl.glfw.GLFW;

public class PhysicsObjectDraggable extends PhysicsObject implements Interactable {

    private boolean dragging;
    private Vector mouseOffset;

    public PhysicsObjectDraggable(Scene scene, Vector pos, ObjectsPolygon element) {
        super(scene, pos, element);
        this.dragging = false;
        this.mouseOffset = Vector.NULL();
    }

    @Override
    public void tick(Vector mousePos) {
        if (dragging) {
            setPos(mousePos.getAdded(mouseOffset));
            setVelocity(Vector.NULL());
        }
        super.tick(mousePos);
    }

    @Override
    public void onKeyPress(int key, int scancode, int action, int mods) {
    }

    @Override
    public void onMouseClick(int button, int action, int mods, Vector mousePos) {
        if (isMouseHovered() && button == GLFW.GLFW_MOUSE_BUTTON_LEFT && action == GLFW.GLFW_PRESS) {
            mouseOffset = VectorUtil.calculateVectorBetween(this,mousePos);
            dragging = true;
        }

        if (action == GLFW.GLFW_RELEASE) {
            mouseOffset = Vector.NULL();
            dragging = false;
        }
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isDragging() {
        return dragging;
    }

    @Override
    public void onMouseScroll(double scroll, Vector mousePos) {
    }

    public RegularPolygon getPolygon() {
        return (RegularPolygon) getElement();
    }
}