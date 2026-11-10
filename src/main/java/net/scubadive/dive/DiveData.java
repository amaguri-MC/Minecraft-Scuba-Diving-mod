package net.scubadive.dive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Per-player dive state: tank pressure (bar), BCD air (0..1), dive time (ticks), max depth (m). */
public record DiveData(float pressure, float air, int diveTicks, float maxDepth) {
	public static final float FULL_TANK = 200f;
	public static final DiveData DEFAULT = new DiveData(FULL_TANK, 0f, 0, 0f);

	public static final Codec<DiveData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("pressure").forGetter(DiveData::pressure),
			Codec.FLOAT.fieldOf("air").forGetter(DiveData::air),
			Codec.INT.fieldOf("dive_ticks").forGetter(DiveData::diveTicks),
			Codec.FLOAT.fieldOf("max_depth").forGetter(DiveData::maxDepth)
	).apply(instance, DiveData::new));

	public static final StreamCodec<ByteBuf, DiveData> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, DiveData::pressure,
			ByteBufCodecs.FLOAT, DiveData::air,
			ByteBufCodecs.VAR_INT, DiveData::diveTicks,
			ByteBufCodecs.FLOAT, DiveData::maxDepth,
			DiveData::new
	);
}
