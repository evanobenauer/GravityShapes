package ejo.gravityshapes.util;

import com.ejo.ui.element.Text;
import com.ejo.util.math.Angle;
import com.ejo.util.math.Vector;

public class TitleBounceHandler {

    private final Text text;
    private Vector pos;

    private final Angle angle;

    public TitleBounceHandler(Text text, Vector pos) {
        this.text = text;
        this.pos = pos;

        this.angle = new Angle(0);
    }

    public void updatePos() {
        text.setPos(pos);
        text.setPos(text.getPos().getAdded(new Vector(0, Math.cos(angle.getRadians()) * 8)));
        angle.add(new Angle(.05));
        angle.simplify();
    }

    public void setPos(Vector pos) {
        this.pos = pos;
    }

    public Vector getPos() {
        return pos;
    }
}
