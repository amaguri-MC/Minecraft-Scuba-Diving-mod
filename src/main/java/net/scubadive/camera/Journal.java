package net.scubadive.camera;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Per-player photo journal: species id -> (best score, shot count) plus the cumulative total of all shot scores. */
public record Journal(Map<String, Entry> entries, int total) {
	public static final Journal EMPTY = new Journal(Map.of(), 0);

	public static final Codec<Journal> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.unboundedMap(Codec.STRING, Entry.CODEC).fieldOf("entries").forGetter(Journal::entries),
			Codec.INT.fieldOf("total").forGetter(Journal::total)
	).apply(instance, Journal::new));

	public static final StreamCodec<ByteBuf, Journal> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, Entry.STREAM_CODEC), Journal::entries,
			ByteBufCodecs.VAR_INT, Journal::total,
			Journal::new
	);

	public Journal {
		entries = Map.copyOf(entries);
	}

	public Entry get(String speciesId) {
		return entries.get(speciesId);
	}

	public boolean has(String speciesId) {
		return entries.containsKey(speciesId);
	}

	/** Returns a new journal with one more shot of the species recorded. */
	public Journal withShot(String speciesId, int score) {
		Map<String, Entry> copy = new HashMap<>(entries);
		Entry old = copy.get(speciesId);
		copy.put(speciesId, old == null ? new Entry(score, 1) : new Entry(Math.max(old.best(), score), old.count() + 1));
		return new Journal(copy, total + score);
	}

	public record Entry(int best, int count) {
		public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("best").forGetter(Entry::best),
				Codec.INT.fieldOf("count").forGetter(Entry::count)
		).apply(instance, Entry::new));

		public static final StreamCodec<ByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_INT, Entry::best,
				ByteBufCodecs.VAR_INT, Entry::count,
				Entry::new
		);
	}
}
