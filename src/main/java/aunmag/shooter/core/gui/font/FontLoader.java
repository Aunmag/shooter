package aunmag.shooter.core.gui.font;

import aunmag.shooter.core.Context;
import aunmag.shooter.core.structures.Texture;
import aunmag.shooter.core.utilities.UtilsFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FontLoader {

    private static final float LINE_HEIGHT = 0.03f;
    private Map<Integer, Character> characters = new HashMap<>();
    private Map<String, String> meta = new HashMap<>();
    private List<HashMap<String, String>> charactersMeta = new ArrayList<>();

    public Font load(String name) throws IOException {
        read(name);

        var padding = meta.get("padding").split(",");
        var paddingX = toFloat(padding[1]) + toFloat(padding[3]);
        var paddingY = toFloat(padding[0]) + toFloat(padding[2]);

        var lineHeight = toFloat(meta.get("lineHeight")) - paddingY;
        var lineStretchY = LINE_HEIGHT / lineHeight;
        var lineStretchX = lineStretchY / Context.main.getWindow().getAspectRatio();

        var textureScale = toFloat(meta.get("scaleW"));
        var spaceWidth = 0f;

        for (var character : charactersMeta) {
            var ascii = toInt(character.get("id"));
            var sizeAdvance = toFloat(character.get("xadvance")) - paddingX;

            if (ascii == ' ') {
                spaceWidth = lineStretchX * sizeAdvance;
            } else {
                var sizeX = toFloat(character.get("width"));
                var sizeY = toFloat(character.get("height"));

                characters.put(ascii, new Character(
                        ascii,
                        toFloat(character.get("x")) / textureScale,
                        toFloat(character.get("y")) / textureScale,
                        sizeX / textureScale,
                        sizeY / textureScale,
                        lineStretchX * toFloat(character.get("xoffset")),
                        lineStretchY * toFloat(character.get("yoffset")),
                        lineStretchX * sizeAdvance,
                        sizeX * lineStretchX,
                        sizeY * lineStretchY
                ));
            }
        }

        var texture = Texture.manager.asFont().provide("fonts/" + name);

        if (texture == null) {
            texture = Texture.empty;
        }

        return new Font(characters, texture, spaceWidth, LINE_HEIGHT);
    }

    private void read(String name) throws IOException {
        UtilsFile.readByLine("/fonts/" + name + ".fnt", line -> {
            var type = (String) null;
            var data = new HashMap<String, String>();

            for (var column: line.split(" ")) {
                if (type == null) {
                    type = column;
                } else {
                    var property = column.split("=");

                    if (property.length == 2) {
                        data.put(property[0], property[1]);
                    }
                }
            }

            if (type != null) {
                if ("char".equals(type)) {
                    charactersMeta.add(data);
                } else {
                    meta.putAll(data);
                }
            }
        });
    }

    private int toInt(String string) {
        return Integer.parseInt(string);
    }

    private float toFloat(String string) {
        return Float.parseFloat(string);
    }

}
