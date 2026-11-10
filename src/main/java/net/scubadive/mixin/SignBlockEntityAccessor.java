package net.scubadive.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;

/**
 * Sets sign text without SignBlockEntity#markUpdated, which needs a level: during world generation the block
 * entity has none yet and setText crashed the chunk worker (the game froze waiting for that chunk).
 */
@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {
	@Accessor("frontText")
	void scubadive$setFrontText(SignText text);
}
