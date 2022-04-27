package me.hsgamer.bentoboxfawehandler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import world.bentobox.bentobox.BentoBox;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintBlock;
import world.bentobox.bentobox.blueprints.dataobjects.BlueprintEntity;
import world.bentobox.bentobox.database.objects.Island;
import world.bentobox.bentobox.nms.PasteHandler;
import world.bentobox.bentobox.util.DefaultPasteUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class FawePaster implements PasteHandler {
    private final BentoBox plugin = BentoBox.getInstance();

    @Override
    public CompletableFuture<Void> pasteBlocks(Island island, World world, Map<Location, BlueprintBlock> map) {
        com.sk89q.worldedit.world.World bukkitWorld = BukkitAdapter.adapt(world);
        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                    .world(bukkitWorld)
                    .maxBlocks(Integer.MAX_VALUE)
                    .forceWNA()
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()
            ) {
                session.setMask(null);
                session.setSourceMask(null);
                map.forEach((location, blueprintBlock) -> {
                    session.setBlock(
                            location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                            BukkitAdapter.adapt(DefaultPasteUtil.createBlockData(blueprintBlock))
                    );
                    if (blueprintBlock.getBiome() != null) {
                        session.setBiome(
                                location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                                BukkitAdapter.adapt(blueprintBlock.getBiome())
                        );
                    }
                });
                session.flushQueue();
            } finally {
                blockFuture.complete(null);
            }
        });

        CompletableFuture<Void> stateFuture = blockFuture.thenRun(() -> map.forEach((location, blueprintBlock) ->
                DefaultPasteUtil.setBlockState(island, location.getBlock(), blueprintBlock))
        );
        return CompletableFuture.allOf(blockFuture, stateFuture);
    }

    @Override
    public CompletableFuture<Void> pasteEntities(Island island, World world, Map<Location, List<BlueprintEntity>> map) {
        CompletableFuture<Void> entityFuture = new CompletableFuture<>();
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            map.forEach((location, list) -> DefaultPasteUtil.setEntity(island, location, list));
            entityFuture.complete(null);
        });
        return entityFuture;
    }
}
