package net.smok.drifter.widgets;

public class AnimationHandler {

    private final float transitionTime;
    private final float coyoteTime;
    private float time;
    private boolean started;
    private boolean finished;

    public AnimationHandler() {
        transitionTime = 10;
        coyoteTime = 0;
    }

    public AnimationHandler(float transitionTime) {
        this.transitionTime = transitionTime;
        coyoteTime = 0;
    }

    public AnimationHandler(float transitionTime, float coyoteTime) {
        this.transitionTime = transitionTime;
        this.coyoteTime = coyoteTime;
    }

    public void start() {
        started = true;
        finished = false;
        time = 0;
    }

    public void tick(float deltaTime) {
        time += deltaTime;
        if (time > allTime()) finished = true;
    }

    public void tickLoop(float deltaTime) {
        time += deltaTime;
        if (time > allTime()) time = 0;
    }

    public float relativeTime() {
        float v = (time - coyoteTime) / transitionTime;
        if (v < 0) return 0;
        if (v > 1) return 1;
        return v;
    }

    public float allTime() {
        return coyoteTime + transitionTime;
    }

    public boolean work() {
        return isStarted() & isNotFinished();
    }

    public boolean isNotFinished() {
        return !finished;
    }

    public boolean isStarted() {
        return started;
    }

    public float getTime() {
        return time;
    }

}
