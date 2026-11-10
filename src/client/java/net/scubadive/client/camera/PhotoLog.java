package net.scubadive.client.camera;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.loader.api.FabricLoader;

import net.scubadive.ScubaDive;
import net.scubadive.camera.Breakdown;

/** Client-side photo metadata stored in screenshots/scubadive/photos.json (a JSON array). */
public final class PhotoLog {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private PhotoLog() { }

	public record Photo(String species, int score, Breakdown breakdown, String time, String file) { }

	public static Path dir() {
		return FabricLoader.getInstance().getGameDir().resolve("screenshots").resolve("scubadive");
	}

	private static Path indexFile() {
		return dir().resolve("photos.json");
	}

	/** Appends one entry. Failures are only logged. */
	public static synchronized void append(Photo photo) {
		try {
			Files.createDirectories(dir());
			JsonArray array = readArray();
			array.add(toJson(photo));
			try (Writer w = Files.newBufferedWriter(indexFile(), StandardCharsets.UTF_8)) {
				GSON.toJson(array, w);
			}
		} catch (IOException | RuntimeException e) {
			ScubaDive.LOGGER.warn("Failed to write photos.json", e);
		}
	}

	/** All photos of a species, newest first. */
	public static synchronized List<Photo> load(String speciesId) {
		List<Photo> out = new ArrayList<>();
		try {
			for (JsonElement el : readArray()) {
				try {
					Photo p = fromJson(el.getAsJsonObject());
					if (p.species().equals(speciesId)) out.add(p);
				} catch (RuntimeException e) {
					ScubaDive.LOGGER.warn("Skipping bad photos.json entry", e);
				}
			}
		} catch (IOException | RuntimeException e) {
			ScubaDive.LOGGER.warn("Failed to read photos.json", e);
		}
		out.sort(Comparator.comparing(Photo::time).reversed());
		return out;
	}

	private static JsonArray readArray() throws IOException {
		Path file = indexFile();
		if (!Files.exists(file)) return new JsonArray();
		try (Reader r = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			JsonElement root = JsonParser.parseReader(r);
			return root != null && root.isJsonArray() ? root.getAsJsonArray() : new JsonArray();
		}
	}

	private static JsonObject toJson(Photo p) {
		JsonObject o = new JsonObject();
		o.addProperty("species", p.species());
		o.addProperty("score", p.score());
		o.addProperty("time", p.time());
		o.addProperty("file", p.file());
		Breakdown b = p.breakdown();
		JsonObject bo = new JsonObject();
		bo.addProperty("distance", b.distance());
		bo.addProperty("neutral", b.neutral());
		bo.addProperty("brightness", b.brightness());
		bo.addProperty("silt", b.silt());
		bo.addProperty("center", b.center());
		bo.addProperty("size", b.size());
		bo.addProperty("group", b.group());
		o.add("breakdown", bo);
		return o;
	}

	private static Photo fromJson(JsonObject o) {
		JsonObject bo = o.getAsJsonObject("breakdown");
		Breakdown b = new Breakdown(f(bo, "distance"), f(bo, "neutral"), f(bo, "brightness"), f(bo, "silt"),
				f(bo, "center"), f(bo, "size"), f(bo, "group"));
		return new Photo(o.get("species").getAsString(), o.get("score").getAsInt(), b,
				o.get("time").getAsString(), o.get("file").getAsString());
	}

	private static float f(JsonObject o, String key) {
		return o.get(key).getAsFloat();
	}
}
