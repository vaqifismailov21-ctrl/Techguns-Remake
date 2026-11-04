package techguns.gametest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import techguns.TGItems;
import techguns.TGSteelTools;
import techguns.TGTurrets;
import techguns.TGuns;
import techguns.Techguns;
import techguns.api.npc.factions.TGNpcFaction;
import techguns.entities.npcs.NPCTurret;
import techguns.items.guns.GenericGun;
import techguns.items.guns.TurretGunOffsets;
import techguns.machines.TurretMenu;
import techguns.tileentities.TurretTileEnt;

/** Executed checks of the ported original turret: parameters, placement, menu, power/repair, targeting and firing. */
@GameTestHolder(Techguns.MODID) @PrefixGameTestTemplate(false)
public final class TurretGameTests {
    private static BlockPos floor(GameTestHelper h,int x,int y,int z){for(int dx=-1;dx<=1;dx++)for(int dz=-1;dz<=1;dz++)h.setBlock(x+dx,y,z+dz,Blocks.STONE);return new BlockPos(x,y,z);}
    /** Places a turret with the real block item on the given face of a relative block, as a player would. */
    private static TurretTileEnt place(GameTestHelper h,Player player,BlockPos against,Direction face){
        var level=h.getLevel();var abs=h.absolutePos(against);var stack=new ItemStack(TGTurrets.ITEM.get());player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var hit=new BlockHitResult(Vec3.atCenterOf(abs).relative(face,.5),face,abs,false);
        var result=stack.useOn(new UseOnContext(level,player,InteractionHand.MAIN_HAND,stack,hit));
        h.assertTrue(result.consumesAction(),"Turret item must place a base");
        return (TurretTileEnt)level.getBlockEntity(abs.relative(face));
    }

    @GameTest(template="empty",batch="turret") public static void turretKeepsOriginalParameters(GameTestHelper h){
        h.setBlock(2,1,2,TGTurrets.BLOCK.get());var tile=(TurretTileEnt)h.getBlockEntity(new BlockPos(2,1,2));
        h.assertTrue(tile.energy.getMaxEnergyStored()==5000&&tile.inventory.getSlots()==20,"Original 5000 FE base with 20 slots");
        h.assertTrue(tile.getRepairTimeMax()==200&&tile.getRepairPowerAmount()==50&&tile.getOperationPowerAmount()==5&&tile.getPowerAmountHealTick()==500&&tile.getHealTickDelay()==60,"Original repair/operation/heal power and timings");
        var armors=new Object[][]{{TGItems.TURRET_ARMOR_IRON,5,1},{TGItems.TURRET_ARMOR_STEEL,10,2},{TGItems.TURRET_ARMOR_OBSIDIAN_STEEL,15,3},{TGItems.TURRET_ARMOR_CARBON,20,4}};
        h.assertTrue(tile.getTurretArmorValue()==0&&tile.getTurretTier()==0,"No armor: value 0, tier 0");
        for(var row:armors){@SuppressWarnings("unchecked") var item=((net.minecraftforge.registries.RegistryObject<? extends net.minecraft.world.item.Item>)row[0]).get();tile.inventory.setStackInSlot(TurretTileEnt.SLOT_ARMOR,new ItemStack(item));
            h.assertTrue(tile.getTurretArmorValue()==(int)row[1]&&tile.getTurretTier()==(int)row[2],"Original turret armor value/tier for "+item);
            h.assertTrue(tile.getTurretBaseTexture().getPath().equals("textures/blocks/turret_base"+((int)row[2]+1)+".png"),"Original tier texture");}
        var turret=TGTurrets.TURRET.get().create(h.getLevel());
        h.assertTrue(turret.getAttributeBaseValue(Attributes.MAX_HEALTH)==30&&turret.getAttributeBaseValue(Attributes.FOLLOW_RANGE)==20&&turret.getAttributeBaseValue(Attributes.MOVEMENT_SPEED)==0&&turret.getAttributeBaseValue(Attributes.KNOCKBACK_RESISTANCE)==1,"Original turret attributes");
        h.assertTrue(Math.abs(turret.getBbWidth()-.6f)<1e-6&&Math.abs(turret.getBbHeight()-.9f)<1e-6,"Original 0.6 x 0.9 size");
        h.assertTrue(Math.abs(turret.getEyeHeight()-.75f)<1e-6,"Upright eye height 0.75");turret.setTurretFacing(Direction.DOWN);h.assertTrue(Math.abs(turret.getEyeHeight()-.15f)<1e-6,"Hanging eye height 0.15");
        h.assertTrue(turret.getTGFaction()==TGNpcFaction.TURRET&&turret.isNoGravity()&&turret.getWeaponPosX()==.05f&&turret.getWeaponPosY()==.7f&&turret.getWeaponPosZ()==.8f,"Original faction, gravity and weapon position");
        h.assertTrue(TurretGunOffsets.size()==24,"All 24 original setTurretPosOffset calls");
        float[] minigun=TurretGunOffsets.get("minigun"),ak=TurretGunOffsets.get("ak47"),none=TurretGunOffsets.get("revolver");
        h.assertTrue(minigun[0]==0&&minigun[1]==-.49f&&minigun[2]==-.14f&&ak[2]==.08f&&none[0]==0&&none[1]==0&&none[2]==0,"Original per-gun turret offsets (commented minigun call ignored)");
        var recipe=h.getLevel().getRecipeManager().byKey(new ResourceLocation(Techguns.MODID,"basicmachine_3_turret"));
        h.assertTrue(recipe.isPresent()&&recipe.get().getResultItem(h.getLevel().registryAccess()).is(TGTurrets.ITEM.get()),"Original turret recipe loaded");
        Techguns.LOGGER.info("TECHGUNS_TURRET_SOURCE_PASSED: 5000 FE base, repair 200 ticks/50 FE, 5 FE operation, 500 FE heal every 60 ticks, four armor tiers/textures, 30 HP 0.6x0.9 entity, 24 gun offsets and loaded recipe");
        h.succeed();
    }

    @GameTest(template="weapon_arena",batch="turret") public static void turretPlacementOwnershipWrenchAndRelink(GameTestHelper h){
        var owner=h.makeMockPlayer();var other=h.makeMockPlayer();
        floor(h,10,10,10);var up=place(h,owner,new BlockPos(10,10,10),Direction.UP);
        h.assertTrue(up!=null&&up.getFacing()==Direction.UP&&up.isOwnedByPlayer(owner)&&up.getSecurity()==0,"Upright base owned by the placer");
        var turret=up.mountedTurret;h.assertTrue(turret!=null&&turret.isAlive()&&turret.getTurretFacing()==Direction.UP,"Placing spawns an upright turret");
        var abs=h.absolutePos(new BlockPos(10,11,10));h.assertTrue(turret.position().distanceTo(new Vec3(abs.getX()+.5,abs.getY()+1,abs.getZ()+.5))<1e-6,"Upright turret stands on top of the base");
        h.setBlock(20,14,20,Blocks.STONE);var down=place(h,owner,new BlockPos(20,14,20),Direction.DOWN);
        var dabs=h.absolutePos(new BlockPos(20,13,20));
        h.assertTrue(down.getFacing()==Direction.DOWN&&down.mountedTurret.position().distanceTo(new Vec3(dabs.getX()+.5,dabs.getY()-.9,dabs.getZ()+.5))<1e-6,"Bottom face places a hanging turret 0.9 below the base");
        // Wrench rotation keeps health and respawns the turret on the new side.
        turret.setHealth(17);up.rotateTile(Direction.DOWN);var rotated=up.mountedTurret;
        h.assertTrue(turret.isRemoved()&&rotated!=turret&&rotated.getHealth()==17&&rotated.getTurretFacing()==Direction.DOWN,"Wrench rotation respawns with the same health");
        up.rotateTile(Direction.NORTH);h.assertTrue(up.getFacing()==Direction.DOWN&&up.mountedTurret==rotated,"Only up/down mounting is accepted");
        // Security: owner-only blocks other players; buttons are owner/user restricted as in the original.
        up.buttonClicked(TurretTileEnt.BUTTON_ID_SECURITY,other);h.assertTrue(up.getSecurity()==0,"Only the owner changes security");
        up.buttonClicked(TurretTileEnt.BUTTON_ID_SECURITY,owner);h.assertTrue(up.getSecurity()==3,"Security toggles 0 <-> 3 without a team mod");
        other.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(12,11,10))));owner.setPos(other.position());
        h.assertTrue(!up.isUseableByPlayer(other)&&up.isUseableByPlayer(owner),"Owner-only access");
        // Dismantling with the crowbar keeps inventory and settings but not the owner.
        var gun=TGuns.m4.get().getDefaultInstance();up.inventory.setStackInSlot(TurretTileEnt.SLOT_WEAPON,gun.copy());
        var ammo=TGuns.m4.get().getAmmoType().getAmmo(0)[0].copyWithCount(5);up.inventory.setStackInSlot(0,ammo.copy());
        owner.setShiftKeyDown(true);owner.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(TGSteelTools.CROWBAR.get()));
        var basePos=h.absolutePos(new BlockPos(10,11,10));var state=h.getLevel().getBlockState(basePos);
        state.use(h.getLevel(),owner,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(basePos),Direction.UP,basePos,false));
        h.assertTrue(h.getLevel().getBlockState(basePos).isAir()&&rotated.isRemoved(),"Dismantle removes the base and its turret");
        var drops=h.getLevel().getEntitiesOfClass(ItemEntity.class,new net.minecraft.world.phys.AABB(basePos).inflate(2));
        h.assertTrue(drops.size()==1&&drops.get(0).getItem().is(TGTurrets.ITEM.get()),"Dismantle drops only the base item, contents stay inside");
        var item=drops.get(0).getItem();var data=item.getTagElement("BlockEntityTag");
        h.assertTrue(data!=null&&!data.contains("owner")&&data.getByte("security")==3&&data.getByte("facing")==Direction.DOWN.get3DDataValue(),"Dismantled tag keeps settings, removes the owner");
        drops.get(0).discard();owner.setShiftKeyDown(false);
        var placer=h.makeMockPlayer();floor(h,30,10,30);
        var abs2=h.absolutePos(new BlockPos(30,10,30));player(placer,item);
        var result=item.useOn(new UseOnContext(h.getLevel(),placer,InteractionHand.MAIN_HAND,item,new BlockHitResult(Vec3.atCenterOf(abs2),Direction.UP,abs2,false)));
        var restored=(TurretTileEnt)h.getLevel().getBlockEntity(abs2.above());
        h.assertTrue(result.consumesAction()&&restored.isOwnedByPlayer(placer)&&restored.getSecurity()==3&&ItemStack.isSameItem(restored.getWeapon(),gun)&&restored.inventory.getStackInSlot(0).getCount()==5,"Re-placing restores inventory/security for the new owner");
        // Relink after reload: a turret without a base link finds the base below; an orphan is hurt.
        var relinked=TGTurrets.TURRET.get().create(h.getLevel());relinked.moveTo(restored.mountedTurret.position());restored.mountedTurret.discard();restored.setMountedTurret(null);
        h.getLevel().addFreshEntity(relinked);relinked.tick();
        h.assertTrue(restored.mountedTurret==relinked&&relinked.mountedTileEnt==restored,"Loaded turret relinks to the base below it");
        var orphan=TGTurrets.TURRET.get().create(h.getLevel());orphan.moveTo(Vec3.atCenterOf(h.absolutePos(new BlockPos(5,20,5))));h.getLevel().addFreshEntity(orphan);float hp=orphan.getHealth();orphan.tick();
        h.assertTrue(orphan.getHealth()<hp,"An orphan turret takes out-of-world damage");orphan.discard();
        var saved=relinked.saveWithoutId(new CompoundTag());var loaded=TGTurrets.TURRET.get().create(h.getLevel());loaded.load(saved);h.assertTrue(loaded.getTurretFacing()==relinked.getTurretFacing(),"Facing survives entity save/load");
        Techguns.LOGGER.info("TECHGUNS_TURRET_PLACEMENT_PASSED: real item placement up/down, owner security, crowbar rotation/dismantle with restored contents and settings, relink and orphan rules");
        h.succeed();
    }
    private static void player(Player player,ItemStack stack){player.setItemInHand(InteractionHand.MAIN_HAND,stack);}

    @GameTest(template="weapon_arena",batch="turret") public static void turretMenuKeepsOriginalLayoutAndShiftClick(GameTestHelper h){
        var owner=h.makeMockPlayer();var other=h.makeMockPlayer();floor(h,10,10,10);var tile=place(h,owner,new BlockPos(10,10,10),Direction.UP);
        owner.setPos(Vec3.atCenterOf(h.absolutePos(new BlockPos(12,11,10))));other.setPos(owner.position());
        var menu=new TurretMenu(1,owner.getInventory(),tile);
        h.assertTrue(menu.slots.size()==56,"20 turret slots and 36 player slots");
        for(int i=0;i<3;i++)for(int j=0;j<3;j++){var in=menu.slots.get(i*3+j);var out=menu.slots.get(9+i*3+j);h.assertTrue(in.x==18+18*i&&in.y==17+18*j&&out.x==116+18*i&&out.y==17+18*j,"Original input/output grid");}
        h.assertTrue(menu.slots.get(18).x==85&&menu.slots.get(18).y==19&&menu.slots.get(19).x==85&&menu.slots.get(19).y==42&&menu.slots.get(20).y==84&&menu.slots.get(47).y==142,"Original gun/armor/player slot positions");
        var gun=TGuns.m4.get().getDefaultInstance();var ammo=TGuns.m4.get().getAmmoType().getAmmo(0)[0].copyWithCount(8);var armor=new ItemStack(TGItems.TURRET_ARMOR_STEEL.get(),3);
        h.assertTrue(!menu.slots.get(9).mayPlace(ammo)&&menu.slots.get(0).mayPlace(ammo)&&!menu.slots.get(18).mayPlace(ammo)&&menu.slots.get(18).mayPlace(gun)&&menu.slots.get(19).mayPlace(armor),"Original slot validity");
        owner.getInventory().setItem(9,gun);owner.getInventory().setItem(10,armor);owner.getInventory().setItem(11,ammo);
        menu.quickMoveStack(owner,20);menu.quickMoveStack(owner,21);menu.quickMoveStack(owner,22);
        h.assertTrue(tile.getWeapon().getItem() instanceof GenericGun&&tile.inventory.getStackInSlot(TurretTileEnt.SLOT_ARMOR).getCount()==1&&owner.getInventory().getItem(10).getCount()==2,"Shift-click moves a gun and a single armor plate");
        h.assertTrue(owner.getInventory().getItem(11).getCount()==8&&tile.inventory.getStackInSlot(0).isEmpty(),"Original empty input range: shift-clicked ammunition stays in the player inventory");
        menu.quickMoveStack(owner,18);h.assertTrue(tile.getWeapon().isEmpty(),"Shift-click returns the gun to the player");
        h.assertTrue(menu.clickMenuButton(owner,TurretTileEnt.BUTTON_ID_REDSTONE)&&tile.redstoneBehaviour==1,"Redstone mode cycles");
        menu.clickMenuButton(owner,TurretTileEnt.BUTTON_ID_REDSTONE);menu.clickMenuButton(owner,TurretTileEnt.BUTTON_ID_REDSTONE);h.assertTrue(tile.redstoneBehaviour==0,"Three redstone modes");
        menu.clickMenuButton(other,TurretTileEnt.BUTTON_ID_PVP_SETTING);h.assertTrue(tile.getPvpSetting()==0,"Only the owner changes PvP");
        menu.clickMenuButton(owner,TurretTileEnt.BUTTON_ID_PVP_SETTING);h.assertTrue(tile.getPvpSetting()==4,"PvP toggles 0 <-> 4 without a team mod");
        menu.clickMenuButton(other,TurretTileEnt.BUTTON_ID_TARGET_ANIMALS);h.assertTrue(tile.attackAnimals,"Any permitted user toggles animal targeting");
        h.assertTrue(!menu.clickMenuButton(owner,7),"Unknown buttons are rejected");
        h.assertTrue(menu.tile.data.get(0)==0&&menu.tile.data.get(6)==4&&menu.tile.data.get(7)==1,"Menu data mirrors the base");
        Techguns.LOGGER.info("TECHGUNS_TURRET_MENU_PASSED: original 3x3 input/output grids, gun/armor slots, original shift-click routing including its empty ammunition range, redstone/security/PvP/animal buttons");
        h.succeed();
    }

    @GameTest(template="weapon_arena",batch="turret") public static void turretPowerRedstoneDeathRepairAndHealing(GameTestHelper h){
        var owner=h.makeMockPlayer();floor(h,10,10,10);var tile=place(h,owner,new BlockPos(10,10,10),Direction.UP);var turret=tile.mountedTurret;
        tile.tickServer();h.assertTrue(!turret.active,"Without power the turret is disabled");
        tile.energy.receiveEnergy(5000,false);tile.tickServer();h.assertTrue(turret.active&&tile.energy.getEnergyStored()==4995,"Powered turret activates and draws 5 FE per tick");
        tile.redstoneBehaviour=1;tile.tickServer();h.assertTrue(!turret.active&&tile.energy.getEnergyStored()==4995,"Redstone mode 'high' without signal disables the turret");
        h.setBlock(11,11,10,net.minecraft.world.level.block.Blocks.REDSTONE_BLOCK);tile.tickServer();h.assertTrue(turret.active&&tile.energy.getEnergyStored()==4990,"A neighbouring signal seen through the original neighbour-change cache enables mode 'high'");
        var saved=tile.saveWithoutMetadata();h.assertTrue(saved.getBoolean("hasSignal"),"Cached signal is saved like the original, without reading the world while saving");
        h.setBlock(11,11,10,net.minecraft.world.level.block.Blocks.AIR);tile.tickServer();h.assertTrue(!turret.active,"Removing the signal disables mode 'high' again");
        tile.redstoneBehaviour=0;tile.tickServer();h.assertTrue(turret.active,"Ignore-redstone re-enables");
        turret.hurt(h.getLevel().damageSources().generic(),1000);
        h.assertTrue(tile.turretDeath&&tile.repairTime==200&&tile.mountedTurret==null,"Death starts the original 200 tick repair");
        int before=tile.energy.getEnergyStored();tile.tickServer();h.assertTrue(tile.repairTime==199&&tile.energy.getEnergyStored()==before-50,"Repair draws 50 FE per tick");
        tile.energy.extractEnergy(10000,false);tile.tickServer();h.assertTrue(tile.repairTime==199,"Repair pauses without power");
        tile.energy.receiveEnergy(5000,false);tile.repairTime=1;tile.tickServer();
        var revived=tile.mountedTurret;h.assertTrue(!tile.turretDeath&&revived!=null&&revived!=turret&&revived.getHealth()==10,"Repaired turret respawns with 10 health");
        tile.turretHealTime=0;int energy=tile.energy.getEnergyStored();tile.tickServer();
        h.assertTrue(revived.getHealth()==11&&tile.turretHealTime==60&&tile.energy.getEnergyStored()==energy-5-500,"Healing: 1 HP for 500 FE every 60 ticks");
        tile.tickServer();h.assertTrue(revived.getHealth()==11&&tile.turretHealTime==59,"Heal delay counts down");
        h.assertTrue(revived.getExperienceReward()==0,"Turrets give no experience");
        Techguns.LOGGER.info("TECHGUNS_TURRET_POWER_PASSED: power-gated activation, redstone modes, death/repair at 50 FE per tick for 200 ticks, 10 HP respawn and 500 FE healing every 60 ticks");
        h.succeed();
    }

    @GameTest(template="weapon_arena",batch="turret") public static void turretTargetsFollowOriginalRules(GameTestHelper h){
        var owner=h.makeMockPlayer();var stranger=h.makeMockPlayer();floor(h,10,10,10);var tile=place(h,owner,new BlockPos(10,10,10),Direction.UP);var turret=tile.mountedTurret;
        var level=h.getLevel();
        var zombie=EntityType.ZOMBIE.create(level);var cow=EntityType.COW.create(level);var villager=EntityType.VILLAGER.create(level);var golem=EntityType.IRON_GOLEM.create(level);
        var wolf=EntityType.WOLF.create(level);var tamedWolf=EntityType.WOLF.create(level);tamedWolf.tame(owner);
        var horse=EntityType.HORSE.create(level);var tamedHorse=EntityType.HORSE.create(level);tamedHorse.setTamed(true);
        var donkey=EntityType.DONKEY.create(level);donkey.setTamed(true);donkey.setOwnerUUID(owner.getUUID());
        h.assertTrue(turret.isValidTarget(zombie),"Monsters are always targets");
        h.assertTrue(!turret.isValidTarget(cow)&&!turret.isValidTarget(villager)&&!turret.isValidTarget(owner)&&!turret.isValidTarget(stranger),"Defaults: no animals, villagers or players");
        tile.attackAnimals=true;
        h.assertTrue(turret.isValidTarget(cow)&&turret.isValidTarget(wolf)&&turret.isValidTarget(golem)&&turret.isValidTarget(horse),"Animal setting: wild animals and golems");
        h.assertTrue(!turret.isValidTarget(tamedWolf)&&!turret.isValidTarget(tamedHorse)&&!turret.isValidTarget(villager),"Tamed wolves/horses and villagers are spared");
        h.assertTrue(turret.isValidTarget(donkey),"1.12 rule: only EntityHorse taming was checked, so a tamed donkey is a target");
        tile.attackAnimals=false;tile.buttonClicked(TurretTileEnt.BUTTON_ID_PVP_SETTING,owner);
        h.assertTrue(turret.isValidTarget(stranger)&&!turret.isValidTarget(owner),"PvP: every player except the owner");
        floor(h,30,10,30);floor(h,30,10,20);var foreign=place(h,stranger,new BlockPos(30,10,30),Direction.UP);var mine=place(h,owner,new BlockPos(30,10,20),Direction.UP);
        h.assertTrue(turret.isValidTarget(foreign.mountedTurret)&&!turret.isValidTarget(mine.mountedTurret),"PvP against other players' turrets only");
        tile.buttonClicked(TurretTileEnt.BUTTON_ID_PVP_SETTING,owner);h.assertTrue(!turret.isValidTarget(foreign.mountedTurret)&&!turret.isValidTarget(stranger),"PvP off spares players and turrets");
        // Retaliation: never against the owner, against other players, never against non-hostile factions.
        stranger.setPos(turret.position().add(2,0,0));owner.setPos(turret.position().add(2,0,0));turret.tickCount=100;
        turret.setLastHurtByMob(owner);h.assertTrue(!turret.aiHurt.canUse(),"No retaliation against the owner");
        turret.tickCount=101;turret.setLastHurtByMob(stranger);h.assertTrue(turret.aiHurt.canUse(),"Retaliation against other players");
        Techguns.LOGGER.info("TECHGUNS_TURRET_TARGETING_PASSED: monsters, optional animals with original tame checks, owner-aware PvP against players and turrets, retaliation rules");
        h.succeed();
    }

    @GameTest(template="weapon_arena",batch="turret",timeoutTicks=600) public static void turretReloadsFromInputsAndShootsMonsters(GameTestHelper h){
        var owner=h.makeMockPlayer();floor(h,24,10,8);var tile=place(h,owner,new BlockPos(24,10,8),Direction.UP);
        for(int x=20;x<29;x++)for(int z=6;z<30;z++)h.setBlock(x,10,z,Blocks.STONE);
        tile.energy.receiveEnergy(5000,false);
        var gun=TGuns.m4.get();var stack=gun.getDefaultInstance();gun.setAmmo(stack,0);tile.inventory.setStackInSlot(TurretTileEnt.SLOT_WEAPON,stack);
        var mag=gun.getAmmoType().getAmmo(0)[0];var empty=gun.getAmmoType().getEmptyMag()[0];
        tile.inventory.setStackInSlot(4,mag.copyWithCount(3));
        var husk=h.spawn(EntityType.HUSK,new Vec3(24.5,11,20.5));husk.setNoAi(true);husk.setPersistenceRequired();husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000);husk.setHealth(2000);
        var cow=h.spawn(EntityType.COW,new Vec3(27.5,11,14.5));cow.setNoAi(true);float cowHealth=cow.getHealth();
        h.succeedWhen(()->{
            h.assertTrue(husk.getHealth()<2000,"Turret has not hurt the monster yet");
            h.assertTrue(tile.inventory.getStackInSlot(4).getCount()==2,"Exactly one magazine is loaded from the inputs");
            boolean emptied=false;for(int i=TurretTileEnt.SLOT_OUTPUT1;i<TurretTileEnt.SLOT_OUTPUT1+TurretTileEnt.OUTPUTS_SIZE;i++)emptied|=ItemStack.isSameItem(tile.inventory.getStackInSlot(i),empty);
            h.assertTrue(empty.isEmpty()||emptied,"The empty magazine goes to the outputs");
            h.assertTrue(gun.getCurrentAmmo(tile.getWeapon())<gun.clipsize,"Shots consume the loaded magazine");
            h.assertTrue(cow.getHealth()==cowHealth,"Animals are spared by default");
            Techguns.LOGGER.info("TECHGUNS_TURRET_COMBAT_PASSED: real AI acquired a monster, reloaded one magazine from the inputs, moved the empty magazine to the outputs and hit with the base's gun; animals spared");
        });
    }
}
