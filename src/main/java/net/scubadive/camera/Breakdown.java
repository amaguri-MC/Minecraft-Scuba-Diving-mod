package net.scubadive.camera;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Score multipliers of one shot (each 1.0 = neutral). score = round(base * product of all). */
public record Breakdown(float distance, float neutral, float brightness, float silt,
		float center, float size, float group, float angle) {
	public static final Breakdown NONE = new Breakdown(1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f);

	public static final StreamCodec<ByteBuf, Breakdown> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, Breakdown::distance,
			ByteBufCodecs.FLOAT, Breakdown::neutral,
			ByteBufCodecs.FLOAT, Breakdown::brightness,
			ByteBufCodecs.FLOAT, Breakdown::silt,
			ByteBufCodecs.FLOAT, Breakdown::center,
			ByteBufCodecs.FLOAT, Breakdown::size,
			ByteBufCodecs.FLOAT, Breakdown::group,
			ByteBufCodecs.FLOAT, Breakdown::angle,
			Breakdown::new
	);

	public double product() {
		return (double) distance * neutral * brightness * silt * center * size * group * angle;
	}
}
