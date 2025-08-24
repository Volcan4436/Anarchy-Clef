package adris.altoclef.helpers;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;

public class ClickHelper {

    private static final Robot robot;

    static {
        Robot temp = null;
        try {
            temp = new Robot(); // force creation on class load
        } catch (AWTException e) {
            e.printStackTrace();
        }
        robot = temp; // guaranteed to be assigned once
    }

    public static void leftClick() {
        if (robot == null) return; // still null if creation failed
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }

    public static void rightClick() {
        if (robot == null) return;
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK);
    }
}