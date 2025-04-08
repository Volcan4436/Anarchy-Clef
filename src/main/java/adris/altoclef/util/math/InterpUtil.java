package adris.altoclef.util.math;

public class InterpUtil {
    private float currentValue;

    public InterpUtil() {
        this.currentValue = 0;
    }

    /**
     * Interpolates the current value towards the target value.
     *
     * @param amount        the target value
     * @param smoothAmount  the smoothing factor (0 = no change, 1 = instant change)
     * @return the interpolated value
     */
    public float interp(float amount, float smoothAmount) {
        currentValue = currentValue + (amount - currentValue) * smoothAmount;
        return currentValue;
    }

    /**
     * Resets the current value to 0.
     */
    public void reset() {
        currentValue = 0;
    }

    /**
     * Sets the current value to a specific value.
     *
     * @param value the new current value
     */
    public void setCurrentValue(float value) {
        currentValue = value;
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public float getCurrentValue() {
        return currentValue;
    }
}