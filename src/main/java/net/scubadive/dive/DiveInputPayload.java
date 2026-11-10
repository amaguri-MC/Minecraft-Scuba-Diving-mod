package net.scubadive.dive;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.scubadive.ScubaDive;

/** Client -> server: BCD key state. dir: +1 inflate (N), -1 deflate (M), 0 released. Resent while held. */
public record DiveInputPayload(int dir) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<DiveInputPayload> TYPE = new CustomPacketPayload.Type<>(ScubaDive.id("dive_input"));
	public static final StreamCodec<RegistryFriendlyByteBuf, DiveInputPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, DiveInputPayload::dir,
			DiveInputPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
