package co.pyragon.jamoss.compat.ponder.scenes;

import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import co.pyragon.jamoss.content.sieve.VibratingSieveBlockEntity;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.items.ItemHandlerHelper;

public class SieveScenes {

	/** Structure "sieve": depot (2,1,2), sieve (2,2,2), resonator (2,3,2), shaft (2,4,2). */
	public static void sieve(SceneBuilder builder, SceneBuildingUtil util) {
		CreateSceneBuilder scene = new CreateSceneBuilder(builder);
		scene.title("vibrating_sieve", "Sifting with the Vibrating Sieve");
		scene.configureBasePlate(0, 0, 5);
		scene.world().showSection(util.select().layer(0), Direction.UP);
		scene.idle(5);

		BlockPos depot = util.grid().at(2, 1, 2);
		BlockPos sieve = util.grid().at(2, 2, 2);
		BlockPos resonator = util.grid().at(2, 3, 2);
		Selection kinetics = util.select().fromTo(2, 3, 2, 2, 4, 2);

		scene.world().showSection(util.select().position(sieve), Direction.DOWN);
		scene.idle(10);
		scene.overlay().showText(70)
			.attachKeyFrame()
			.text("The Vibrating Sieve is a mesh cage. It has no power of its own")
			.pointAt(util.vector().blockSurface(sieve, Direction.WEST))
			.placeNearTarget();
		scene.idle(80);

		scene.world().showSection(kinetics, Direction.DOWN);
		scene.idle(10);
		scene.world().setKineticSpeed(kinetics, 32);
		scene.idle(10);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("A Resonator directly above shakes it (32 RPM or more)")
			.pointAt(util.vector().blockSurface(resonator, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.world().showSection(util.select().position(depot), Direction.UP);
		scene.idle(10);
		ItemStack gravel = new ItemStack(Items.GRAVEL);
		scene.world().modifyBlockEntity(sieve, VibratingSieveBlockEntity.class,
			be -> ItemHandlerHelper.insertItem(be.inputInv, new ItemStack(Items.GRAVEL, 8), false));
		scene.overlay().showControls(util.vector().blockSurface(sieve, Direction.WEST), Pointing.RIGHT, 30).withItem(gravel);
		scene.idle(20);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.text("Insert gravel, sand or similar through any open side: by hand, funnel or belt")
			.pointAt(util.vector().blockSurface(sieve, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showOutline(PonderPalette.GREEN, new Object(), util.select().position(depot), 80);
		scene.overlay().showText(80)
			.attachKeyFrame()
			.colored(PonderPalette.GREEN)
			.text("What falls through the mesh lands in the inventory below: a depot, chest or belt")
			.pointAt(util.vector().blockSurface(depot, Direction.WEST))
			.placeNearTarget();
		scene.idle(90);

		scene.overlay().showText(70)
			.text("Results can also be extracted from the sides with funnels or hoppers")
			.pointAt(util.vector().blockSurface(sieve, Direction.NORTH))
			.placeNearTarget();
		scene.idle(80);
		scene.markAsFinished();
	}
}
