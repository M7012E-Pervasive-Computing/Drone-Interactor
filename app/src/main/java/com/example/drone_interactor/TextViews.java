package com.example.drone_interactor;

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
            TextView upwardDistance) {
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
    }
}
