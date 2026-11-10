package net.scubadive.world;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.phys.Vec3;

/** /scubadive trench: prints the coordinates of the nearest deep trench. */
public final class TrenchCommand {
	private static final int STEP = 16;
	private static final int MAX_RADIUS = 4000;
	private static final int SAMPLE_Y = -32;

	public static void init() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
				dispatcher.register(Commands.literal("scubadive")
						.then(Commands.literal("trench").executes(TrenchCommand::run))));
	}

	private static int run(CommandContext<CommandSourceStack> ctx) {
		CommandSourceStack source = ctx.getSource();
		ServerLevel level = source.getLevel();
		Vec3 here = source.getPosition();
		long seed = level.getSeed();
		BiomeSource biomes = level.getChunkSource().getGenerator().getBiomeSource();
		Climate.Sampler sampler = level.getChunkSource().randomState().sampler();

		int ox = (int) Math.floor(here.x);
		int oz = (int) Math.floor(here.z);
		double bestSq = Double.MAX_VALUE;
		int bestX = 0;
		int bestZ = 0;
		boolean found = false;

		// Square rings around the player; keep going until a ring cannot beat the best hit.
		for (int k = 0; k * STEP <= MAX_RADIUS; k++) {
			if (found && (double) (k - 1) * STEP > Math.sqrt(bestSq)) {
				break;
			}
			for (int i = -k; i <= k; i++) {
				for (int j = -k; j <= k; j++) {
					if (Math.max(Math.abs(i), Math.abs(j)) != k) {
						continue;
					}
					int x = ox + i * STEP;
					int z = oz + j * STEP;
					double dSq = (double) (i * STEP) * (i * STEP) + (double) (j * STEP) * (j * STEP);
					if (dSq >= bestSq || TrenchField.shape(seed, x + 0.5, z + 0.5) < 0.6) {
						continue;
					}
					Holder<Biome> biome = biomes.getNoiseBiome(x >> 2, SAMPLE_Y >> 2, z >> 2, sampler);
					if (biome.is(BiomeTags.IS_DEEP_OCEAN)) {
						bestSq = dSq;
						bestX = x;
						bestZ = z;
						found = true;
					}
				}
			}
		}

		if (!found) {
			source.sendFailure(Component.translatable("message.scubadive.trench.none", MAX_RADIUS));
			return 0;
		}
		int dist = (int) Math.round(Math.sqrt(bestSq));
		int fx = bestX;
		int fz = bestZ;
		source.sendSuccess(() -> Component.translatable("message.scubadive.trench.found", fx, fz, dist), false);
		return 1;
	}

	private TrenchCommand() {
	}
}
