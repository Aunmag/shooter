package aunmag.shooter.core;

import aunmag.shooter.core.utilities.Quad;
import aunmag.shooter.game.client.Constants;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;

public class Window extends Quad {

    public static final int UNDEFINED_ID = 0;
    public final long id;
    public final Matrix4fc projection;
    private boolean isCursorGrabbed = false;
    private boolean isInitialized = false;

    Window() {
        super(1024, 576);

        var monitorId = GLFW.glfwGetPrimaryMonitor();
        var monitorSizeX = GLFW.glfwGetVideoMode(monitorId).width();
        var monitorSizeY = GLFW.glfwGetVideoMode(monitorId).height();

        if (Configs.isFullscreen()) {
            setSize(monitorSizeX, monitorSizeY);
        }

        var sizeX = (int) getSizeX();
        var sizeY = (int) getSizeY();

        projection = new Matrix4f().setOrtho2D(
                -getCenterX(),
                +getCenterX(),
                -getCenterY(),
                +getCenterY()
        );

        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, Configs.getAntialiasing());

        id = GLFW.glfwCreateWindow(
                sizeX,
                sizeY,
                Constants.TITLE, // TODO: Do not access game package!
                Configs.isFullscreen() ? monitorId : 0,
                0
        );

        if (id == UNDEFINED_ID) {
            throw new IllegalStateException("Failed to create window!");
        }

        if (!Configs.isFullscreen()) {
            var centerX = (monitorSizeX - sizeX) / 2;
            var centerY = (monitorSizeY - sizeY) / 2;
            GLFW.glfwSetWindowPos(id, centerX, centerY);
        }

        GLFW.glfwShowWindow(id);
        GLFW.glfwMakeContextCurrent(id);

        isInitialized = true;
    }

    public Vector2f project(float x, float y) {
        x = (x - getCenterX() + 1) / getCenterX();
        y = (getCenterY() - y - 1) / getCenterY();
        return new Vector2f(x, y);
    }

    public void setCursorGrabbed(boolean isCursorGrabbed) {
        if (isCursorGrabbed == this.isCursorGrabbed) {
            return;
        } else {
            this.isCursorGrabbed = isCursorGrabbed;
        }

        if (isCursorGrabbed) {
            GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
            GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
        } else {
            GLFW.glfwSetInputMode(id, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
    }

    @Override
    public void setSize(float width, float height) {
        if (isInitialized) {
            var message = "Unable to change window size after initialization";
            System.err.println(message);
        } else {
            super.setSize(width, height);
        }
    }

}
