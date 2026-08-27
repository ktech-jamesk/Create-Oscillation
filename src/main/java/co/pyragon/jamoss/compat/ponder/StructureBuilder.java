package co.pyragon.jamoss.compat.ponder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Builds a vanilla structure template ({@code size}/{@code palette}/{@code blocks}) without a
 * level, so Ponder structures can be produced headlessly by datagen. Unset positions are air.
 */
public class StructureBuilder {

	private final int sizeX, sizeY, sizeZ;
	private final Map<BlockPos, BlockState> blocks = new LinkedHashMap<>();

	public StructureBuilder(int sizeX, int sizeY, int sizeZ) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.sizeZ = sizeZ;
	}

	public StructureBuilder put(int x, int y, int z, BlockState state) {
		if (x < 0 || y < 0 || z < 0 || x >= sizeX || y >= sizeY || z >= sizeZ)
			throw new IllegalArgumentException("(" + x + "," + y + "," + z + ") outside " + sizeX + "x" + sizeY + "x" + sizeZ);
		blocks.put(new BlockPos(x, y, z), state);
		return this;
	}

	public StructureBuilder fill(int x1, int y1, int z1, int x2, int y2, int z2, BlockState state) {
		for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++)
			for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++)
				for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++)
					put(x, y, z, state);
		return this;
	}

	public Map<BlockPos, BlockState> blocks() {
		return blocks;
	}

	public CompoundTag toNbt() {
		List<BlockState> palette = new ArrayList<>();
		ListTag blockList = new ListTag();
		for (Map.Entry<BlockPos, BlockState> e : blocks.entrySet()) {
			int index = palette.indexOf(e.getValue());
			if (index < 0) {
				index = palette.size();
				palette.add(e.getValue());
			}
			CompoundTag block = new CompoundTag();
			block.put("pos", ints(e.getKey().getX(), e.getKey().getY(), e.getKey().getZ()));
			block.putInt("state", index);
			blockList.add(block);
		}
		ListTag paletteList = new ListTag();
		for (BlockState state : palette) {
			CompoundTag entry = NbtUtils.writeBlockState(state);
			// writeBlockState uses the registry; make sure the block is actually registered
			BuiltInRegistries.BLOCK.getKey(state.getBlock());
			paletteList.add(entry);
		}
		CompoundTag root = new CompoundTag();
		root.put("size", ints(sizeX, sizeY, sizeZ));
		root.put("palette", paletteList);
		root.put("blocks", blockList);
		root.put("entities", new ListTag());
		root.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
		return root;
	}

	private static ListTag ints(int... values) {
		ListTag list = new ListTag();
		for (int v : values)
			list.add(net.minecraft.nbt.IntTag.valueOf(v));
		return list;
	}
}
