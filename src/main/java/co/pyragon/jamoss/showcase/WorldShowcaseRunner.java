package co.pyragon.jamoss.showcase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Dev-only media capture driven by a shot list in the real world. Reads
 * {@code run/showcase/shots.json}, and for each shot: teleports the (creative, flying) player to the
 * camera, optionally dollies to an end position, runs timed commands (fill a wall, drop a crystal
 * in, start a motor...) and saves a frame every {@link #FRAME_INTERVAL} ticks to
 * {@code screenshots/showcase/<shot>/}. {@code tools/showcase_gifs.py} turns those into GIFs.
 *
 * <pre>
 * {
 *   "fps": 10,
 *   "setup": ["time set 6000", "gamerule doDaylightCycle false", "weather clear"],
 *   "between": ["kill @e[type=item]"],
 *   "shots": [
 *     { "name": "pulveriser_low", "from": [100.5, 66, 20.5], "to": [104.5, 66, 20.5],
 *       "yaw": 90, "pitch": 5, "fov": 60, "lead": 20, "ticks": 160,
 *       "commands": [ { "at": 0, "run": "fill 110 64 18 113 70 22 stone" },
 *                     { "at": 30, "run": "item replace block 108 65 20 container.0 with createoscillation:tuned_crystal_low" } ] }
 *   ]
 * }
 * </pre>
 */
public class WorldShowcaseRunner {

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Gson GSON = new GsonBuilder().create();
	private static final int FRAME_INTERVAL = 2;
	private static final int SETTLE_TICKS = 40;

	public static class Command {
		public int at;
		public String run;
	}

	public static class Shot {
		public String name;
		public double[] from;
		public double[] to;
		public float yaw;
		public float pitch;
		public float fov = 70;
		/** Ticks after teleporting before the first frame (lets chunks/lighting settle). */
		public int lead = 20;
		public int ticks = 100;
		public List<Command> commands = new ArrayList<>();
		/** Presentation, passed through untouched to tools/showcase_gifs.py (see tools/shots.example.json). */
		public com.google.gson.JsonElement title;
		public com.google.gson.JsonElement captions;
	}

	public static class ShotList {
		public int fps = 10;
		/** Default title placement for every shot; a shot's own "title" overrides it ("title": false hides it). */
		public com.google.gson.JsonElement title;
		public List<String> setup = new ArrayList<>();
		/** Run at the start of every shot, before its own commands (e.g. "kill @e[type=item]"). */
		public List<String> between = new ArrayList<>();
		public List<Shot> shots = new ArrayList<>();
	}

	private enum State { WAITING_FOR_WORLD, SETTLING, SHOOTING, DONE }

	private State state = State.WAITING_FOR_WORLD;
	private int timer;
	private ShotList list;
	private int shotIndex = -1;
	private Shot shot;
	private int shotTick;
	private int frame;
	private final List<String> manifest = new ArrayList<>();
	private double previousFov;

	public static void install() {
		NeoForge.EVENT_BUS.addListener(new WorldShowcaseRunner()::onClientTick);
	}

	private void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		switch (state) {
			case WAITING_FOR_WORLD -> {
				if (mc.level != null && mc.player != null && mc.screen == null && mc.getSingleplayerServer() != null) {
					state = State.SETTLING;
					timer = 0;
				}
			}
			case SETTLING -> {
				if (++timer < SETTLE_TICKS)
					return;
				if (!load(mc))
					return;
				previousFov = mc.options.fov().get();
				mc.options.hideGui = true;
				// creative + flight rather than spectator: identical camera, but no doubt about chunk ticking
				String name = mc.player.getGameProfile().getName();
				command(mc, "gamemode creative " + name);
				command(mc, "effect give " + name + " minecraft:invisibility infinite 0 true");
				for (String cmd : list.setup)
					command(mc, cmd);
				state = State.SHOOTING;
				nextShot(mc);
			}
			case SHOOTING -> tickShot(mc);
			case DONE -> {}
		}
	}

	private boolean load(Minecraft mc) {
		Path file = new File(mc.gameDirectory, "showcase/shots.json").toPath();
		try {
			list = GSON.fromJson(Files.readString(file), ShotList.class);
		} catch (IOException | RuntimeException e) {
			LOGGER.error("[Showcase] could not read {}: {}", file, e.toString());
			finish(mc);
			return false;
		}
		if (list == null || list.shots.isEmpty()) {
			LOGGER.error("[Showcase] {} has no shots", file);
			finish(mc);
			return false;
		}
		LOGGER.info("[Showcase] {} shots loaded from {}", list.shots.size(), file);
		return true;
	}

	private void nextShot(Minecraft mc) {
		shotIndex++;
		if (shotIndex >= list.shots.size()) {
			finish(mc);
			return;
		}
		shot = list.shots.get(shotIndex);
		shotTick = 0;
		frame = 0;
		if (shot.from == null || shot.from.length != 3) {
			LOGGER.warn("[Showcase] shot {} has no camera position, skipping", shot.name);
			nextShot(mc);
			return;
		}
		mc.options.fov().set((int) shot.fov);
		writeShotSpec(mc);
		for (String cmd : list.between)
			command(mc, cmd);
		moveCamera(mc, 0);
		LOGGER.info("[Showcase] shot {} ({} ticks)", shot.name, shot.ticks);
	}

	/** shot.json next to the frames: fps, frame interval and the presentation the GIF tool needs. */
	private void writeShotSpec(Minecraft mc) {
		com.google.gson.JsonObject spec = new com.google.gson.JsonObject();
		spec.addProperty("name", shot.name);
		spec.addProperty("fps", list.fps);
		spec.addProperty("frameInterval", FRAME_INTERVAL);
		spec.addProperty("ticks", shot.ticks);
		if (shot.title != null)
			spec.add("title", shot.title);
		else if (list.title != null)
			spec.add("title", list.title);
		if (shot.captions != null)
			spec.add("captions", shot.captions);
		Path dir = new File(mc.gameDirectory, "screenshots/showcase/" + shot.name).toPath();
		try {
			Files.createDirectories(dir);
			Files.writeString(dir.resolve("shot.json"), GSON.toJson(spec));
		} catch (IOException e) {
			LOGGER.warn("[Showcase] could not write shot.json for {}", shot.name, e);
		}
	}

	private void tickShot(Minecraft mc) {
		if (shot == null)
			return;
		int total = shot.lead + shot.ticks;
		for (Command c : shot.commands)
			if (c.at + shot.lead == shotTick)
				command(mc, c.run);
		if (shot.to != null && shot.to.length == 3 && shotTick >= shot.lead)
			moveCamera(mc, (float) (shotTick - shot.lead) / Math.max(1, shot.ticks));
		if (shotTick >= shot.lead && (shotTick - shot.lead) % FRAME_INTERVAL == 0)
			grab(mc);
		shotTick++;
		if (shotTick > total) {
			manifest.add(shot.name + "\t" + shot.name + "\t" + frame);
			nextShot(mc);
		}
	}

	/** Teleports the player along the shot's from→to line ({@code t} in 0..1), with the fixed yaw/pitch. */
	private void moveCamera(Minecraft mc, float t) {
		if (mc.player != null) {
			mc.player.getAbilities().flying = true;
			mc.player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
		}
		double[] a = shot.from;
		double[] b = shot.to != null && shot.to.length == 3 ? shot.to : shot.from;
		double x = Mth.lerp(t, a[0], b[0]);
		double y = Mth.lerp(t, a[1], b[1]);
		double z = Mth.lerp(t, a[2], b[2]);
		command(mc, String.format(java.util.Locale.ROOT, "tp %s %.3f %.3f %.3f %.2f %.2f",
			mc.player.getGameProfile().getName(), x, y, z, shot.yaw, shot.pitch));
	}

	private void command(Minecraft mc, String cmd) {
		MinecraftServer server = mc.getSingleplayerServer();
		if (server == null)
			return;
		// not suppressed: a rejected command shows up in the log as "[Showcase] command failed"
		server.execute(() -> {
			net.minecraft.commands.CommandSourceStack source = server.createCommandSourceStack().withPermission(4)
				.withCallback((success, result) -> {
					if (!success)
						LOGGER.warn("[Showcase] command failed: /{}", cmd);
				});
			server.getCommands().performPrefixedCommand(source, cmd);
		});
	}

	private void grab(Minecraft mc) {
		Path dir = new File(mc.gameDirectory, "screenshots/showcase/" + shot.name).toPath();
		Path file = dir.resolve(String.format("frame_%05d.png", frame++));
		NativeImage image = Screenshot.takeScreenshot(mc.getMainRenderTarget());
		Util.ioPool().execute(() -> {
			try (image) {
				Files.createDirectories(dir);
				image.writeToFile(file);
			} catch (IOException e) {
				LOGGER.warn("[Showcase] could not write {}", file, e);
			}
		});
	}

	private void finish(Minecraft mc) {
		state = State.DONE;
		mc.options.hideGui = false;
		if (list != null)
			mc.options.fov().set((int) previousFov);
		try {
			Path dir = new File(mc.gameDirectory, "screenshots/showcase").toPath();
			Files.createDirectories(dir);
			Files.write(dir.resolve("manifest.tsv"), manifest);
		} catch (IOException e) {
			LOGGER.error("[Showcase] could not write manifest", e);
		}
		LOGGER.info("[Showcase] {} shots captured, exiting", manifest.size());
		mc.stop();
	}
}
