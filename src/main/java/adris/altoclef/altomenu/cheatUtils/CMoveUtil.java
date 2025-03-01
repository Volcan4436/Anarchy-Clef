package adris.altoclef.altomenu.cheatUtils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CMoveUtil {

    protected static MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isMoving() {
        return mc.player.forwardSpeed > 0 || mc.player.sidewaysSpeed > 0 || mc.player.forwardSpeed < 0 || mc.player.sidewaysSpeed < 0;
    }

    public static boolean isSprinting() {
        return mc.player.isSprinting();
    }

    public static boolean isOnGround() {
        return mc.player.isOnGround();
    }



    static double yaw = mc.player.getYaw();
    private static double speed = 1.0; // Replace with your desired speed value

    static double  motionX = -Math.sin(yaw) * speed;
    static double motionY = mc.player.getVelocity().y; // Keep the current Y velocity
    static double motionZ = Math.cos(yaw) * speed;

    public static void setSpeed(double newSpeed) {
        speed = newSpeed;
    }

    public static void strafe() {
        strafe(speed());
    }

    public static double speed() {
        return Math.sqrt(Math.pow(mc.player.getVelocity().getX(), 2) + Math.pow(mc.player.getVelocity().getZ(), 2));
    }


    public static void strafe(double speed) {
        if (!isMoving()) {
            return;
        }

        double yaw = getDirection();
        double motionX = -MathHelper.sin((float) yaw) * speed; // Inverted direction
        double motionZ = MathHelper.cos((float) yaw) * speed; // Inverted direction
        mc.player.setVelocity(new Vec3d(motionX, mc.player.getVelocity().getY(), motionZ));
    }


    public void strafe(double speed, float yaw) {
        if (!isMoving()) {
            return;
        }

        yaw = (float) getDirection();
        double motionX = MathHelper.sin(yaw) * speed; // Forward movement
        double motionZ = -MathHelper.cos(yaw) * speed; // Right movement
        mc.player.setVelocity(new Vec3d(motionX, mc.player.getVelocity().getY(), motionZ));
    }

    public static double getDirection() {
        double rotationYaw = MinecraftClient.getInstance().player.getYaw();
        if (MinecraftClient.getInstance().player.input.movementForward < 0) {
            rotationYaw += 180;
        }
        double forward = 1;
        if (MinecraftClient.getInstance().player.input.movementForward < 0) {
            forward = -0.5;
        } else if (MinecraftClient.getInstance().player.input.movementForward > 0) {
            forward = 0.5;
        }
        if (MinecraftClient.getInstance().player.input.movementSideways > 0) {
            rotationYaw -= 90 * forward;
        }
        if (MinecraftClient.getInstance().player.input.movementSideways < 0) {
            rotationYaw += 90 * forward;
        }
        return Math.toRadians(rotationYaw);
    }

    public static double predictedMotion(double motion) {
        return (motion - 0.08) * 0.98;
    }


    public static double predictedMotion(double motion, int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98;
        }

        return predicted;
    }
}
