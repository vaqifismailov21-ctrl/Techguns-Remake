package techguns.machines;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
/** Standalone source access modes: public and owner-only, without adding the NPC/faction subsystem. */
public final class MachineSecurity {
    private UUID owner;private boolean restricted;private final Runnable changed;
    public MachineSecurity(Runnable changed){this.changed=changed;}
    public void place(Player player){owner=player.getUUID();restricted=false;changed.run();}
    public boolean owner(Player player){return owner!=null&&owner.equals(player.getUUID());}
    public UUID ownerId(){return owner;}
    public boolean allows(Player player){return owner==null||!restricted||owner(player);}
    public boolean restricted(){return restricted;}
    public boolean toggle(Player player){if(!owner(player))return false;restricted=!restricted;changed.run();return true;}
    public void save(CompoundTag tag){if(owner!=null)tag.putUUID("owner",owner);tag.putBoolean("private",restricted);}
    public void load(CompoundTag tag){owner=tag.hasUUID("owner")?tag.getUUID("owner"):null;restricted=tag.getBoolean("private");}
}
