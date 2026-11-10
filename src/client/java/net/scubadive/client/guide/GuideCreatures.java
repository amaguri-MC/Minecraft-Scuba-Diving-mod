package net.scubadive.client.guide;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

/**
 * Client-only display creatures for the guide book. Entities are created in the client level but never added to it,
 * and drawn like the inventory player model (InventoryScreen.extractEntityInInventoryFollowsMouse), turning slowly.
 */
final class GuideCreatures {
	private static final int FULL_BRIGHT = 15728880;
	private static final float TILT = (float) Math.toRadians(-12.0);

	private final Map<String, Optional<LivingEntity>> cache = new HashMap<>();

	LivingEntity get(String id) {
		return this.cache.computeIfAbsent(id, k -> Optional.ofNullable(create(k))).orElse(null);
	}

	private static LivingEntity create(String id) {
		Minecraft mc = Minecraft.getInstance();
		Identifier key = Identifier.tryParse(id);
		if (mc.level == null || key == null) {
			return null;
		}
		EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
		if (type == null) {
			return null;
		}
		Entity entity = type.create(mc.level, EntitySpawnReason.LOAD);
		return entity instanceof LivingEntity living ? living : null;
	}

	static EntityType<?> typeOf(String id) {
		Identifier key = Identifier.tryParse(id);
		return key == null ? null : BuiltInRegistries.ENTITY_TYPE.getOptional(key).orElse(null);
	}

	/** Draws the entity centred in the box, turned by spinDeg around the vertical axis. */
	static void draw(GuiGraphicsExtractor graphics, LivingEntity entity, int x0, int y0, int x1, int y1, float spinDeg, float ageTicks) {
		EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
		EntityRenderer<? super LivingEntity, ?> renderer = dispatcher.getRenderer(entity);
		EntityRenderState state = renderer.createRenderState(entity, 1.0f);
		state.lightCoords = FULL_BRIGHT;
		state.shadowPieces.clear();
		state.outlineColor = EntityRenderState.NO_OUTLINE;
		state.nameTag = null;
		state.ageInTicks = ageTicks;
		if (state instanceof LivingEntityRenderState living) {
			living.bodyRot = 180.0f + spinDeg;
			living.yRot = 0.0f;
			living.xRot = 0.0f;
			living.isInWater = true;
			if (living.scale > 0.0f) {
				living.boundingBoxWidth /= living.scale;
				living.boundingBoxHeight /= living.scale;
			}
			living.scale = 1.0f;
		}
		float bw = Math.max(0.1f, state.boundingBoxWidth);
		float bh = Math.max(0.1f, state.boundingBoxHeight);
		// Models are often longer than their hitbox, so leave room for the turn.
		float scale = Math.min((x1 - x0) * 0.55f / bw, (y1 - y0) * 0.6f / bh);
		Vector3f translation = new Vector3f(0.0f, bh / 2.0f, 0.0f);
		Quaternionf tilt = new Quaternionf().rotateX(TILT);
		Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(tilt);
		graphics.entity(state, scale, translation, rotation, tilt, x0, y0, x1, y1);
	}
}
