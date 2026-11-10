package net.scubadive.silt;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import net.scubadive.ScubaDive;

/** Server -> client: the sediment clouds near the player (same list the server uses). */
public record SiltSyncPayload(List<Entry> clouds) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<SiltSyncPayload> TYPE = new CustomPacketPayload.Type<>(ScubaDive.id("silt_sync"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SiltSyncPayload> CODEC = StreamCodec.ofMember(SiltSyncPayload::write, SiltSyncPayload::read);

	/** One cloud: centre, radius (blocks) and density 0..1. */
	public record Entry(float x, float y, float z, float radius, float density) {
	}

	private void write(RegistryFriendlyByteBuf buf) {
		buf.writeVarInt(clouds.size());
		for (Entry e : clouds) {
			buf.writeFloat(e.x());
			buf.writeFloat(e.y());
			buf.writeFloat(e.z());
			buf.writeFloat(e.radius());
			buf.writeFloat(e.density());
		}
	}

	private static SiltSyncPayload read(RegistryFriendlyByteBuf buf) {
		int n = Math.min(buf.readVarInt(), 512);
		List<Entry> list = new ArrayList<>(n);
		for (int i = 0; i < n; i++) {
			list.add(new Entry(buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat()));
		}
		return new SiltSyncPayload(list);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
