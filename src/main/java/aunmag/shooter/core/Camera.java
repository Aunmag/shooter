package aunmag.shooter.core;

import aunmag.shooter.core.basics.BaseObject;
import aunmag.shooter.core.utilities.Mount;
import aunmag.shooter.core.utilities.UtilsMath;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Camera extends BaseObject {

    private Matrix4f viewMatrix = new Matrix4f();
    public float scale = 15;
    public final Mount mount;

    public Camera() {
        super(new Vector2f(0, 0), 0);
        mount = new Mount(getPosition(), null);
    }

    @Override
    public void update() {
        var angle = UtilsMath.correctRadians(getRadians() - UtilsMath.PIx0_5);
        var position = getPosition();

        viewMatrix = new Matrix4f(Context.main.getWindow().projection)
                .rotateZ(-angle)
                .scale(getPixelsScale())
                .translate(-position.x, -position.y, 0);

        Context.main.getListener().setPosition(position.x, position.y, angle);
    }

    public Vector2f project(float x, float y) {
        var projected = new Vector3f(x, y, 0).mulPosition(viewMatrix);
        return new Vector2f(projected.x, projected.y);
    }

    public Matrix4f toViewProjection(float x, float y, float angle) {
        return new Matrix4f(viewMatrix).translate(x, y, 0).rotateZ(angle);
    }

    public float toMeters(float distance) {
        return distance / getPixelsScale();
    }

    public float toPixels(float distance) {
        return distance * getPixelsScale();
    }

    public float getPixelsScale() {
        return Context.main.getWindow().getDiagonal() / scale;
    }

}
