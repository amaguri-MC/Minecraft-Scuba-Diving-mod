package net.scubadive.tour.center;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.scubadive.ScubaDive;

/** Client -> server: a button pressed in the guide screen. The server re-validates everything. */
public record GuideActionPayload(int guideId, int action, String arg) implements CustomPacketPayload {
	/** arg = dive point id. */
	public static final int CREATE = 0;
	public static final int JOIN = 1;
	public static final int START = 2;
	public static final int CANCEL = 3;
	public static final int LEAVE = 4;

	public static final CustomPacketPayload.Type<GuideActionPayload> TYPE = new CustomPacketPayload.Type<>(ScubaDive.id("guide_action"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GuideActionPayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GuideActionPayload::guideId,
			ByteBufCodecs.VAR_INT, GuideActionPayload::action,
			ByteBufCodecs.STRING_UTF8, GuideActionPayload::arg,
			GuideActionPayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
