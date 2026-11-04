package techguns.client.models.machines;

/** Box model of a power block; the block entity renderer sets these fields before render(). */
public abstract class PowerModel extends ModelMachine {
	/** Working: fire, glow, lamps and moving parts. */
	public boolean active;
	/** Charge bars, 0..4. */
	public int charge;
	/** Angle in radians of the turning parts (flywheel, fans, rotor, pistons). */
	public float spin;
	/** Tank fill, 0..1. */
	public float level;
}
