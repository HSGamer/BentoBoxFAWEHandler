package me.hsgamer.bentoboxfawehandler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.IslandDeletion;
import world.bentobox.bentobox.nms.NMSAbstraction;

import java.util.concurrent.CompletableFuture;

public class FaweRegenerator implements NMSAbstraction {
    private final BentoBox plugin = BentoBox.getInstance();

    @Override
    public CompletableFuture<Void> regenerate(GameModeAddon gameModeAddon, IslandDeletion islandDeletion, World world) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
            CuboidRegion region = new CuboidRegion(
                    bukkitWorld,
                    BlockVector3.at(islandDeletion.getMinX(), world.getMinHeight(), islandDeletion.getMinZ()),
                    BlockVector3.at(islandDeletion.getMaxX(), world.getMaxHeight(), islandDeletion.getMaxZ())
            );
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(bukkitWorld)
                    .fastMode(true)
                    .build()
            ) {
                session.regenerate(region);
                session.flushQueue();
            } finally {
                future.complete(null);
            }
        });
        return future;
    }
}
