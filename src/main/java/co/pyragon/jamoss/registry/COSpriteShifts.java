package co.pyragon.jamoss.registry;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;

import co.pyragon.jamoss.CreateOscillation;

/** Connected-texture sprite shifts (client only). */
public class COSpriteShifts {

	public static final CTSpriteShiftEntry CONDENSER = getCT(AllCTTypes.RECTANGLE, "condenser"),
		CONDENSER_TOP = getCT(AllCTTypes.RECTANGLE, "condenser_top"),
		CONDENSER_INNER = getCT(AllCTTypes.RECTANGLE, "condenser_inner");

	private static CTSpriteShiftEntry getCT(CTType type, String name) {
		return CTSpriteShifter.getCT(type, CreateOscillation.asResource("block/" + name),
			CreateOscillation.asResource("block/" + name + "_connected"));
	}
}
