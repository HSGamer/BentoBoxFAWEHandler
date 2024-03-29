package me.hsgamer.bentoboxfawehandler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.visitor.EntityVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.RegenOptions;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.util.BoundingBox;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.api.addons.GameModeAddon;
import world.bentobox.bentobox.database.objects.IslandDeletion;
import world.bentobox.bentobox.nms.WorldRegenerator;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FaweRegenerator implements WorldRegenerator {
    private final BentoBox plugin = BentoBox.getInstance();

    @Override
    public CompletableFuture<Void> regenerate(GameModeAddon gameModeAddon, IslandDeletion islandDeletion, World world) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        BoundingBox boundingBox = islandDeletion.getBox();
        CuboidRegion region = new CuboidRegion(
                bukkitWorld,
                BlockVector3.at(boundingBox.getMinX(), boundingBox.getMinY(), boundingBox.getMinZ()),
                BlockVector3.at(boundingBox.getMaxX(), boundingBox.getMaxY(), boundingBox.getMaxZ())
        );

        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()
            ) {
                session.setMask(null);
                session.setSourceMask(null);
                RegenOptions options = RegenOptions.builder()
                        .regenBiomes(true)
                        .seed(world.getSeed())
                        .build();
                bukkitWorld.regenerate(region, session, options);
            } finally {
                blockFuture.complete(null);
            }
        });

        CompletableFuture<Void> entityFuture = new CompletableFuture<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(bukkitWorld)
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()
            ) {
                session.setMask(null);
                session.setSourceMask(null);
                List<? extends Entity> entities = session.getEntities(region);
                Operations.completeLegacy(new EntityVisitor(entities.iterator(), Entity::remove));
            } finally {
                entityFuture.complete(null);
            }
        });
        return CompletableFuture.allOf(blockFuture, entityFuture);
    }

    @Override
    public CompletableFuture<Void> regenerateChunk(Chunk chunk) {
        World world = chunk.getWorld();
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(chunk.getWorld());
        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()
            ) {
                session.setMask(null);
                session.setSourceMask(null);
                RegenOptions options = RegenOptions.builder()
                        .regenBiomes(true)
                        .seed(world.getSeed())
                        .build();
                bukkitWorld.regenerate(
                        new CuboidRegion(
                                bukkitWorld,
                                BlockVector3.at(chunk.getX() << 4, world.getMinHeight(), chunk.getZ() << 4),
                                BlockVector3.at((chunk.getX() << 4) + 15, world.getMaxHeight() - 1, (chunk.getZ() << 4) + 15)
                        ),
                        session,
                        options
                );
            } finally {
                blockFuture.complete(null);
            }
        });
        return blockFuture;
    }
}
