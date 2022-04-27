package me.hsgamer.bentoboxfawehandler;

import world.bentobox.bentobox.api.addons.Addon;
import world.bentobox.bentobox.util.Util;

public final class BentoBoxFAWEHandler extends Addon {
    @Override
    public void onEnable() {
        Util.setRegenerator(new FaweRegenerator());
    }

    @Override
    public void onDisable() {
        // EMPTY
    }
}
