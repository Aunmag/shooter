package aunmag.shooter.core.gui.font;

import aunmag.shooter.core.Context;
import aunmag.shooter.core.graphics.Graphics;
import aunmag.shooter.core.utilities.Quad;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Text extends Quad {

    public static final TextManager manager = new TextManager();

    public final FontStyle style;
    @Nullable
    private TextVao vao = null;
    private Vector4f colour = Graphics.COLOR_WHITE;
    private Matrix4f projection;
    private boolean isRenderingOrdered = false;
    private boolean isOnWorldRendering = false;

    public Text(float x, float y, String message, FontStyle style) {
        super(x, y, 0, 0);
        this.style = style;

        load(message);
        manager.add(this);
        updateProjection();
    }

    public void load(String message) {
        if (message.equals(getMessage())) {
            return;
        }

        removeVao();
        vao = new TextVao(message, style);

        setSize(
                vao.sizeX * Context.main.getWindow().getCenterX(),
                vao.sizeY * Context.main.getWindow().getCenterY()
        );
    }

    public void updateProjection() {
        var x = position.x;
        var y = position.y;
        Vector2f position;

        if (isOnWorldRendering) {
            position = Context.main.getCamera().project(x, y);
        } else {
            position = Context.main.getWindow().project(x, y);
        }

        projection = new Matrix4f().translate(position.x(), position.y(), 0);
    }

    public void orderRendering() {
        isRenderingOrdered = true;
    }

    @Override
    public void render() {
        if (!isRenderingOrdered || vao == null) {
            return;
        }

        if (isOnWorldRendering) {
            updateProjection();
        }

        vao.bind();

        Context.main.getShader().setUniformColour(colour);
        Context.main.getShader().setUniformProjection(projection);

        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, vao.vertices);
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        isRenderingOrdered = false;
    }

    @Override
    protected void onRemove() {
        removeVao();
        super.onRemove();
    }

    private void removeVao() {
        if (vao != null) {
            vao.remove();
            vao = null;
        }
    }

    public void setColour(Vector4f colour) {
        this.colour = colour;
    }

    public void setOnWorldRendering(boolean isOnWorldRendering) {
        if (this.isOnWorldRendering != isOnWorldRendering) {
            this.isOnWorldRendering = isOnWorldRendering;
            updateProjection();
        }
    }

    @Nullable
    public String getMessage() {
        if (vao == null) {
            return null;
        } else {
            return vao.message;
        }
    }

}
