package net.scubadive.tour.center;

import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.scubadive.ScubaDive;

/**
 * Server -> client: state of a guide's reception for one viewer. {@code open} asks the client to open the
 * screen; otherwise an already open screen of the same guide is refreshed. names[0] is the host.
 */
public record GuideStatePayload(int guideId, boolean open, int mode, String point, int secondsLeft, List<String> names)
		implements CustomPacketPayload {
	/** Nobody is hosting: choose a dive point. */
	public static final int MODE_SELECT = 0;
	/** A reception is open and the viewer is not in it. */
	public static final int MODE_JOIN = 1;
	/** The viewer is the host. */
	public static final int MODE_HOST = 2;
	/** The viewer joined someone else's reception. */
	public static final int MODE_MEMBER = 3;
	/** The screen must close. */
	public static final int MODE_CLOSE = 4;

	public static final CustomPacketPayload.Type<GuideStatePayload> TYPE = new CustomPacketPayload.Type<>(ScubaDive.id("guide_state"));
	public static final StreamCodec<RegistryFriendlyByteBuf, GuideStatePayload> CODEC = StreamCodec.composite(
			ByteBufCodecs.VAR_INT, GuideStatePayload::guideId,
			ByteBufCodecs.BOOL, GuideStatePayload::open,
			ByteBufCodecs.VAR_INT, GuideStatePayload::mode,
			ByteBufCodecs.STRING_UTF8, GuideStatePayload::point,
			ByteBufCodecs.VAR_INT, GuideStatePayload::secondsLeft,
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), GuideStatePayload::names,
			GuideStatePayload::new
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
