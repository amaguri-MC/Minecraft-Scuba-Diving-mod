package net.scubadive.camera;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.scubadive.ScubaDive;

/** Server -> client: verified result. score <= 0 means the photo was rejected. */
public record CaptureResultPayload(String species, int score, boolean isNew) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<CaptureResultPayload> TYPE = new CustomPacketPayload.Type<>(ScubaDive.id("camera_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, CaptureResultPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8, CaptureResultPayload::species,
			ByteBufCodecs.VAR_INT, CaptureResultPayload::score,
			ByteBufCodecs.BOOL, CaptureResultPayload::isNew,
			CaptureResultPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
