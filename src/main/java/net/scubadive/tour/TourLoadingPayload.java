package net.scubadive.tour;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.scubadive.ScubaDive;

/** Server -> client: tour loading screen progress. percent 0..100; a negative value closes the screen. */
public record TourLoadingPayload(int percent, String pointKey) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<TourLoadingPayload> TYPE = new CustomPacketPayload.Type<>(ScubaDive.id("tour_loading"));
	public static final StreamCodec<RegistryFriendlyByteBuf, TourLoadingPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, TourLoadingPayload::percent,
			ByteBufCodecs.STRING_UTF8, TourLoadingPayload::pointKey,
			TourLoadingPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
