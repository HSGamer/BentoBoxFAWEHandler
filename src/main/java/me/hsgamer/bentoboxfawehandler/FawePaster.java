package me.hsgamer.bentoboxfawehandler;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
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
        CompletableFuture<Void> blockFuture = new CompletableFuture<>();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(BukkitAdapter.adapt(world))
                    .fastMode(true)
                    .changeSetNull()
                    .limitUnlimited()
                    .compile()
                    .build()
            ) {
                session.setMask(null);
                session.setSourceMask(null);
                map.forEach((location, blueprintBlock) -> {
                    DefaultPasteUtil.convertBlockData(blueprintBlock);
                    session.setBlock(
                            location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                            BukkitAdapter.adapt(DefaultPasteUtil.convertBlockData(blueprintBlock))
                    );
                });
                session.flushQueue();
            } finally {
                blockFuture.complete(null);
            }
        });

        CompletableFuture<Void> stateFuture = blockFuture.thenRun(() -> map.forEach((location, blueprintBlock) -> {
            Block block = location.getBlock();
            DefaultPasteUtil.setBlockState(island, block, blueprintBlock);
            if (blueprintBlock.getBiome() != null) {
                block.setBiome(blueprintBlock.getBiome());
            }
        }));
        return CompletableFuture.allOf(blockFuture, stateFuture);
    }

    @Override
    public CompletableFuture<Void> pasteEntities(Island island, World world, Map<Location, List<BlueprintEntity>> map) {
        map.forEach((location, list) -> DefaultPasteUtil.setEntity(island, location, list));
        return CompletableFuture.completedFuture(null);
    }
}
