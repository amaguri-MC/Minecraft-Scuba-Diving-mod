package net.scubadive.tour;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/** Where a tour diver came from; persisted so nobody is stranded in the tour dimension. */
public record TourOrigin(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {
	public static final Codec<TourOrigin> CODEC = RecordCodecBuilder.create(i -> i.group(
			Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(TourOrigin::dimension),
			Codec.DOUBLE.fieldOf("x").forGetter(TourOrigin::x),
			Codec.DOUBLE.fieldOf("y").forGetter(TourOrigin::y),
			Codec.DOUBLE.fieldOf("z").forGetter(TourOrigin::z),
			Codec.FLOAT.fieldOf("yaw").forGetter(TourOrigin::yaw),
			Codec.FLOAT.fieldOf("pitch").forGetter(TourOrigin::pitch)
	).apply(i, TourOrigin::new));
}
