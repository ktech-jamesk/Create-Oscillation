package co.pyragon.jamoss.datagen;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.google.common.hash.Hashing;

import co.pyragon.jamoss.CreateOscillation;
import co.pyragon.jamoss.compat.ponder.COPonderStructures;
import co.pyragon.jamoss.compat.ponder.StructureBuilder;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.NbtIo;

/** Writes every Ponder scene structure from {@link COPonderStructures} as a compressed NBT template. */
public class COPonderStructureGen implements DataProvider {

	private final Path root;

	public COPonderStructureGen(PackOutput output) {
		this.root = output.getOutputFolder(PackOutput.Target.RESOURCE_PACK)
			.resolve(CreateOscillation.MOD_ID)
			.resolve("ponder");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cache) {
		List<CompletableFuture<?>> futures = new ArrayList<>();
		for (Map.Entry<String, StructureBuilder> e : COPonderStructures.all().entrySet()) {
			Path path = root.resolve(e.getKey() + ".nbt");
			futures.add(CompletableFuture.runAsync(() -> {
				try {
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					NbtIo.writeCompressed(e.getValue().toNbt(), bytes);
					byte[] data = bytes.toByteArray();
					cache.writeIfNeeded(path, data, Hashing.sha1().hashBytes(data));
				} catch (IOException ex) {
					throw new RuntimeException("Failed to write ponder structure " + path, ex);
				}
			}));
		}
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	@Override
	public String getName() {
		return "Create: Oscillation ponder structures";
	}
}
