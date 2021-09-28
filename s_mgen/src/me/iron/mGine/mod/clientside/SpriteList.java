package me.iron.mGine.mod.clientside;

import api.utils.textures.StarLoaderTexture;
import me.iron.mGine.mod.ModMain;
import org.schema.schine.graphicsengine.forms.Sprite;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * STARMADE MOD
 * CREATOR: Max1M
 * DATE: 28.09.2021
 * TIME: 18:20
 */
public enum SpriteList {
    MAPICONS("edencore_sprites.png",8,8,64);
    private String resourceName;
    private Sprite sprite;
    int x;
    int y;
    int size;
    static String path ="me/iron/mGine/mod/res/";
    SpriteList(String resourceName,int x,int y,int size) {
        this.resourceName = resourceName;
        this.x = x;
        this.y = y;
        this.size = size;
    }

    public Sprite getSprite() {
        return sprite;
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
    }

    public static void loadSprites() {
        for (SpriteList sprite: values()) {
            try {
                InputStream in = ModMain.instance.getJarResource(path+sprite.resourceName);
                BufferedImage img = ImageIO.read(in);
                sprite.setSprite(StarLoaderTexture.newSprite(img,ModMain.instance, sprite.resourceName));
                sprite.getSprite().setHeight(sprite.size);
                sprite.getSprite().setWidth(sprite.size);
                sprite.getSprite().setPositionCenter(true);
                sprite.getSprite().setMultiSpriteMax(sprite.x,sprite.y);
            } catch (IOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

}
