package aunmag.shooter.game.scenarios;

import aunmag.shooter.core.graphics.Graphics;
import aunmag.shooter.core.gui.Button;
import aunmag.shooter.core.gui.Label;
import aunmag.shooter.core.gui.Notification;
import aunmag.shooter.core.gui.Page;
import aunmag.shooter.core.gui.font.FontStyle;
import aunmag.shooter.core.structures.Texture;
import aunmag.shooter.core.utilities.Timer;
import aunmag.shooter.core.utilities.UtilsMath;
import aunmag.shooter.core.utilities.UtilsRandom;
import aunmag.shooter.game.ai.Ai;
import aunmag.shooter.game.client.Context;
import aunmag.shooter.game.client.player.Player;
import aunmag.shooter.game.data.Sounds;
import aunmag.shooter.game.environment.World;
import aunmag.shooter.game.environment.actor.Actor;
import aunmag.shooter.game.environment.actor.ActorType;
import aunmag.shooter.game.environment.decorations.Decoration;
import aunmag.shooter.game.environment.decorations.DecorationType;
import aunmag.shooter.game.environment.weapon.Weapon;
import aunmag.shooter.game.environment.weapon.WeaponBonus;
import aunmag.shooter.game.environment.weapon.WeaponType;
import org.lwjgl.opengl.GL11;

public class ScenarioEncircling extends Scenario {

    private static final int BORDERS_DISTANCE = 32;
    private static final int BLUFF_BLOCK_SIZE = 4;
    private static final float DIFFICULTY = 1.1f;
    private static final float LASER_GUN_CHANCE = 0.001f;
    private static final int WAVE_FINAL = 6;
    private static final int ZOMBIES_QUANTITY_INITIAL = 5;
    private static final float HUMAN_SPAWN_CHANCE = 1.0f / WAVE_FINAL / 4.0f;

    private int wave = 0;
    private int zombiesToSpawn = 0;
    private final Timer spawnTimer = new Timer(world.time, 0.5f);
    private final Timer waveCheckTimer = new Timer(Context.main.getTime(), 2);
    private float bonusChance = 0;
    private ActorType zombie = ActorType.zombie;
    private ActorType zombieAgile = ActorType.zombieAgile;
    private ActorType zombieHeavy = ActorType.zombieHeavy;

    public ScenarioEncircling(World world) {
        super(world);
        initializeBluffs();
    }

    private void initializeBluffs() {
        var ground = world.ground.all;
        var step = BLUFF_BLOCK_SIZE;
        var r1 = (float) Math.PI;
        var r2 = (float) 0;
        var r3 = (float) UtilsMath.PIx0_5;
        var r4 = (float) UtilsMath.PIx1_5;
        var n = BORDERS_DISTANCE;

        for (var i = -n + step; i <= n - step; i += step) {
            ground.add(new Decoration(DecorationType.bluff, i, -n, r1));
            ground.add(new Decoration(DecorationType.bluff, i, +n, r2));
            ground.add(new Decoration(DecorationType.bluff, -n, i, r3));
            ground.add(new Decoration(DecorationType.bluff, +n, i, r4));
        }

        ground.add(new Decoration(DecorationType.bluffCorner, -n, -n, r1));
        ground.add(new Decoration(DecorationType.bluffCorner, +n, +n, r2));
        ground.add(new Decoration(DecorationType.bluffCorner, -n, +n, r3));
        ground.add(new Decoration(DecorationType.bluffCorner, +n, -n, r4));
    }

    @Override
    public Actor createPlayableActor() {
        var actor = new Actor(ActorType.human, world, 0, 0, (float) -UtilsMath.PIx0_5);
        actor.setWeapon(new Weapon(world, WeaponType.pm));
        world.actors.all.add(actor);
        return actor;
    }

    @Override
    public void update() {
        if (Context.main.getPlayerActor().map(Actor::isAlive).orElse(false)) {
            confinePlayerPosition();

            if (zombiesToSpawn == 0) {
                if (isWaveComplete()) {
                    if (wave == WAVE_FINAL) {
                        gameOver(true);
                    } else {
                        startNextWave();
                    }
                }
            } else if (spawnTimer.isDone()) {
                spawnZombie();
                spawnTimer.next();
            }
        } else {
            gameOver(false);
        }
    }

    /**
     * For performance purposes this method actually checks for wave completion once every
     * two seconds only. Otherwise, is always returns false.
     */
    private boolean isWaveComplete() {
        if (waveCheckTimer.isDone()) {
            waveCheckTimer.next();

            for (var actor : world.actors.all) {
                if (actor.type != ActorType.human) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private void confinePlayerPosition() {
        Context.main.getPlayerActor().ifPresent(player -> {
            var position = player.body.position;
            position.x = UtilsMath.limit(position.x, -BORDERS_DISTANCE, BORDERS_DISTANCE);
            position.y = UtilsMath.limit(position.y, -BORDERS_DISTANCE, BORDERS_DISTANCE);
        });
    }

    private void startNextWave() {
        wave++;
        zombiesToSpawn = ZOMBIES_QUANTITY_INITIAL * wave * wave;
        bonusChance = wave / (float) zombiesToSpawn;

        zombie = ActorType.clone(ActorType.zombie, getZombiesSkill());
        zombieAgile = ActorType.clone(ActorType.zombieAgile, getZombiesSkill());
        zombieHeavy = ActorType.clone(ActorType.zombieHeavy, getZombiesSkill());

        Context.main.getPlayer().ifPresent(p -> p.hud.layer.add(new Notification(
                world.time,
                String.format("Wave %d/%d", wave, WAVE_FINAL),
                String.format("Kill %d zombies", zombiesToSpawn)
        )));

        if (wave > 1 && UtilsRandom.chance(HUMAN_SPAWN_CHANCE)) {
            spawnEnemy(ActorType.human);
        }
    }

    private void spawnZombie() {
        var type = (ActorType) null;

        switch (UtilsRandom.between(1, 4)) {
            case 1: type = zombieAgile; break;
            case 2: type = zombieHeavy; break;
            default: type = zombie;
        }

        spawnEnemy(type);
    }

    private void spawnEnemy(ActorType type) {
        var direction = -UtilsRandom.between(0, (float) UtilsMath.PIx2);
        var distance = Player.SCALE_MAX / 2;
        var x = -distance * (float) Math.cos(direction);
        var y = -distance * (float) Math.sin(direction);

        x += Context.main.getPlayerActor().map(a -> a.body.position.x).orElse(0f);
        y += Context.main.getPlayerActor().map(a -> a.body.position.y).orElse(0f);

        var enemy = new Actor(type, world, x, y, direction);
        world.actors.all.add(enemy);
        world.ais.all.add(new Ai(enemy));

        if (enemy.type == ActorType.human) {
            enemy.setWeapon(createRandomWeapon());
        }

        if (UtilsRandom.chance(bonusChance)) {
            world.bonuses.all.add(new WeaponBonus(enemy, createRandomWeapon()));
        }

        zombiesToSpawn--;
    }

    private Weapon createRandomWeapon() {
        var type = (WeaponType) null;
        var index = 0;

        if (!UtilsRandom.chance(LASER_GUN_CHANCE)) {
            index = UtilsRandom.between(1, 2 * wave);
        }

        switch (index) {
            case 1: type = WeaponType.pm; break;
            case 2: type = WeaponType.tt; break;
            case 3: type = WeaponType.mp43sawedOff; break;
            case 4: type = WeaponType.mp27; break;
            case 5: type = WeaponType.pp91kedr; break;
            case 6: type = WeaponType.pp19bizon; break;
            case 7: type = WeaponType.aks74u; break;
            case 8: type = WeaponType.ak74m; break;
            case 9: type = WeaponType.rpk74; break;
            case 10: type = WeaponType.saiga12k; break;
            case 11: type = WeaponType.pkm; break;
            case 12: type = WeaponType.pkpPecheneg; break;
            default: type = WeaponType.laserGun;
        }

        return new Weapon(world, type);
    }

    private void gameOver(boolean isVictory) {
        openGameOverPage(isVictory);
        Context.main.application.endGame();

        if (!isVictory) {
            if (Context.main.application.pause.theme != null) {
                Context.main.application.pause.theme.stop();
            }

            if (Sounds.soundGameOver != null) {
                Sounds.soundGameOver.play();
            }
        }
    }

    private void openGameOverPage(boolean isVictory) {
        var wavesSurvived = wave;
        var wallpaperName = (String) null;
        var title = (String) null;

        if (isVictory) {
            wallpaperName = "victory";
            title = "Well done!";
        } else {
            wavesSurvived -= 1;
            wallpaperName = "death";
            title = "You have died";
        }

        var score = String.format(
                "You killed %d zombies and survived %d/%d waves.",
                Context.main.getPlayerActor().map(a -> a.statistics.kills).orElse(0),
                wavesSurvived,
                WAVE_FINAL
        );

        var wallpaper = Texture.manager
                .asWallpaper()
                .provide(String.format("images/wallpapers/%s", wallpaperName));

        var page = new Page(wallpaper);

        page.open();
        page.add(new Label(4, 3, 4, 1, title));
        page.add(new Label(4, 4, 4, 1, score, FontStyle.LABEL_LIGHT));
        page.add(new Button(4, 9, 4, 1, "Back to main menu", () -> {
            if (Sounds.soundGameOver != null) {
                Sounds.soundGameOver.stop();
            }

            if (Context.main.application.pause.theme != null) {
                Context.main.application.pause.theme.play();
            }

            Button.ACTION_BACK.run();
        }));

        Context.main.application.setPause(true);
    }

    @Override
    public void render() {
        if (Context.main.isDebug()) {
            var n = BORDERS_DISTANCE;
            var camera = Context.main.getCamera();
            GL11.glLineWidth(2);
            GL11.glColor3f(1, 0, 0);
            Graphics.draw.line(-n, -n, +n, -n, camera::project);
            Graphics.draw.line(+n, -n, +n, +n, camera::project);
            Graphics.draw.line(+n, +n, -n, +n, camera::project);
            Graphics.draw.line(-n, +n, -n, -n, camera::project);
            GL11.glLineWidth(1);
        }
    }

    public float getZombiesSkill() {
        return 1 + (DIFFICULTY - 1) * (wave - 1);
    }

}
