package net.scubadive.camera;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.scubadive.ScubaDive;

/** Client -> server: "I photographed this". The score is NOT sent; the server recomputes it. */
public record CapturePayload(String species, int entityId, int x, int y, int z, float fov) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<CapturePayload> TYPE = new CustomPacketPayload.Type<>(ScubaDive.id("camera_capture"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CapturePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, CapturePayload::species,
			ByteBufCodecs.INT, CapturePayload::entityId,
			ByteBufCodecs.INT, CapturePayload::x,
			ByteBufCodecs.INT, CapturePayload::y,
			ByteBufCodecs.INT, CapturePayload::z,
			ByteBufCodecs.FLOAT, CapturePayload::fov,
			CapturePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
