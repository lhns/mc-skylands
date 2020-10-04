package de.lolhens.minecraft.skylandsmod.mixin;

import de.lolhens.minecraft.skylandsmod.util.ShouldDrawSideContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("HEAD"), method = "shouldDrawSide", cancellable = true)
    private static void shouldDrawSideHead(BlockState state,
                                           BlockView world,
                                           BlockPos pos,
                                           Direction facing,
                                           CallbackInfoReturnable<Boolean> info) {
        ShouldDrawSideContext.startShouldDrawSide(world, pos);
    }

    @Inject(at = @At("RETURN"), method = "shouldDrawSide", cancellable = true)
    private static void shouldDrawSideReturn(BlockState state,
                                             BlockView world,
                                             BlockPos pos,
                                             Direction facing,
                                             CallbackInfoReturnable<Boolean> info) {
        ShouldDrawSideContext.endShouldDrawSide();
    }
}
