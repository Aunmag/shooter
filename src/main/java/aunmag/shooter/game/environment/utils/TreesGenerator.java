package aunmag.shooter.game.environment.utils;

import aunmag.shooter.core.utilities.UtilsMath;
import aunmag.shooter.core.utilities.UtilsRandom;
import aunmag.shooter.game.environment.World;
import aunmag.shooter.game.environment.decorations.Decoration;
import aunmag.shooter.game.environment.decorations.DecorationType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;

public class TreesGenerator {

    private final World world;
    private final float size;
    private final float treesQuantity;
    private final float intervalMin;

    public TreesGenerator(
            World world,
            float size,
            float treesQuantityPerMeter,
            float intervalMin
    ) {
        this.world = world;
        this.size = size;
        this.treesQuantity = size * size * treesQuantityPerMeter;
        this.intervalMin = intervalMin;
    }

    public void generate() {
        for (int i = 0; i < treesQuantity; i++) {
            var tree = tryGenerateTree();
            if (tree != null) {
                world.trees.all.add(tree);
            }
        }
    }

    @Nullable
    private Decoration tryGenerateTree() {
        @Nullable
        var position = (Vector2f) null;

        for (int attempt = 0; attempt < 32; attempt++) {
            position = generatePosition();

            if (checkIsPositionUnoccupied(position)) {
                break;
            }

            position = null;
        }

        if (position == null) {
            return null;
        } else {
            return new Decoration(
                    generateType(),
                    position.x,
                    position.y,
                    generateRadians()
            );
        }
    }

    private Vector2f generatePosition() {
        var sizeHalf = size / 2f;
        var x = UtilsRandom.between(-sizeHalf, sizeHalf);
        var y = UtilsRandom.between(-sizeHalf, sizeHalf);
        return new Vector2f(x, y);
    }

    private float generateRadians() {
        return UtilsRandom.between(0, (float) UtilsMath.PIx2);
    }

    private DecorationType generateType() {
        return UtilsRandom.chose(new DecorationType[] {
            DecorationType.tree1,
            DecorationType.tree2,
            DecorationType.tree3
        }).orElse(DecorationType.tree1);
    }

    private boolean checkIsPositionUnoccupied(Vector2f position) {
        for (var tree: world.trees.all) {
            var intervalX = Math.abs(position.x - tree.body.position.x);
            var intervalY = Math.abs(position.y - tree.body.position.y);
            if (intervalX < intervalMin && intervalY < intervalMin) {
                return false;
            }
        }

        return true;
    }

}
