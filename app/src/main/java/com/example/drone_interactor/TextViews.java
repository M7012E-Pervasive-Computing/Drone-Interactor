package com.example.drone_interactor;

import android.widget.Switch;
import android.widget.TextView;

public class TextViews {
    public TextView debugText;
    public TextView motors;
    public TextView distanceX;
    public TextView distanceY;
    public TextView distanceZ;
    public TextView downwardDistance;
    public TextView currentAngle;
    public TextView forwardDistance;
    public TextView backwardDistance;
    public TextView upwardDistance;

    public Switch forwardOption;
    public Switch backwardOption;
    public Switch upwardOption;
    public Switch downwardOption;
    public Switch obstacleAvoidanceOption;

    /**
     * Constructor for a TextViews object, setting all the objects to the given parameters.
     * @param debugText debug text
     * @param motors motors text
     * @param distanceX distance x text
     * @param distanceY distance y text
     * @param distanceZ distance z text
     * @param downwardDistance downward distance text
     * @param currentAngle current angle text
     * @param forwardDistance forward distance text
     * @param backwardDistance backward distance text
     * @param upwardDistance upward distance text
     * @param forwardOption forward option switch for sensors
     * @param backwardOption backward option switch for sensors
     * @param upwardOptions upward option switch for sensors
     * @param downwardOption downward option switch for sensors
     * @param obstacleAvoidanceOption obstacle avoidance option switch
     */
    public TextViews(
            TextView debugText,
            TextView motors,
            TextView distanceX,
            TextView distanceY,
            TextView distanceZ,
            TextView downwardDistance,
            TextView currentAngle,
            TextView forwardDistance,
            TextView backwardDistance,
            TextView upwardDistance,
            Switch forwardOption,
            Switch backwardOption,
            Switch upwardOptions,
            Switch downwardOption,
            Switch obstacleAvoidanceOption) {
        this.debugText = debugText;
        this.motors = motors;
        this.distanceX = distanceX;
        this.distanceY = distanceY;
        this.distanceZ = distanceZ;
        this.downwardDistance = downwardDistance;
        this.currentAngle = currentAngle;
        this.forwardDistance = forwardDistance;
        this.backwardDistance = backwardDistance;
        this.upwardDistance = upwardDistance;
        this.forwardOption = forwardOption;
        this.backwardOption = backwardOption;
        this.upwardOption = upwardOptions;
        this.downwardOption = downwardOption;
        this.obstacleAvoidanceOption = obstacleAvoidanceOption;
    }
}
