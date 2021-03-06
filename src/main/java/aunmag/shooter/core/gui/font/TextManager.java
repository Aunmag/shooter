package aunmag.shooter.core.gui.font;

import aunmag.shooter.core.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextManager {

    private final Map<Font, List<Text>> all = new HashMap<>();

    public void add(Text text) {
        all.computeIfAbsent(text.style.font, f -> new ArrayList<>()).add(text);
    }

    public void renderAll() {
        Context.main.getShader().bind();

        for (var font: all.keySet()) {
            font.texture.bind();
            var texts = all.get(font);
            var textsToDelete = new ArrayList<Text>();

            for (var text: texts) {
                if (text.isActive()) {
                    text.render();
                } else {
                    text.remove();
                    textsToDelete.add(text);
                }
            }

            texts.removeAll(textsToDelete);
        }

        Context.main.getShader().setUniformColourDefault();
        TextVao.unbind();
    }

    public void removeAll() {
        for (var texts: all.values()) {
            for (var text: texts) {
                text.remove();
            }

            texts.clear();
        }
    }

}
