package me.iron.mGine.mod.clientside.map;

import org.schema.schine.graphicsengine.forms.Sprite;

/**
* STARMADE MOD
* CREATOR: Max1M
* DATE: 21.09.2021
* TIME: 20:06
 * every icon has its own entry, with sprite and index on that sprite.
*/
public enum MapIcon {
    WP_MOVE(SpriteList.MAPICONS,16),
    WP_SCAN(SpriteList.MAPICONS,17),
    WP_ATTACK(SpriteList.MAPICONS,18),
    WP_DEFEND(SpriteList.MAPICONS,19),
    WP_PICKUP(SpriteList.MAPICONS,20),
    WP_DROPOFF(SpriteList.MAPICONS,21),
    WP_QUEST(SpriteList.MAPICONS,22),
    WP_QUEST_CIRCLE(SpriteList.MAPICONS,23),
    WP_COMM(SpriteList.MAPICONS,24);


    private SpriteList spriteList;
    private int subSprite;

    MapIcon(SpriteList sprite, int subSprite) {
        this.subSprite = subSprite;
        this.spriteList = sprite;
    }

    public SpriteList getSpriteList() {
        return spriteList;
    }

    public int getSubSprite() {
        return subSprite;
    }

    public Sprite getSprite() {
        return spriteList.getSprite();
    }
}
