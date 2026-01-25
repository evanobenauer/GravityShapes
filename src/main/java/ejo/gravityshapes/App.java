package ejo.gravityshapes;

import com.ejo.ui.Window;
import com.ejo.util.math.Vector;

public class App {

    public static Window WINDOW = new Window(
            "Gravity Shapes",
            new Vector(1600,1000),
            new TitleScene()).initAntiAliasingLevel(4);


    public static void main(String[] args) {
        //WINDOW.setDebugMode(Window.DebugMode.DEBUG_SIMPLE);
        WINDOW.init();
        WINDOW.startThreadTickLoop();
        WINDOW.runMainRenderLoop();
    }

}
