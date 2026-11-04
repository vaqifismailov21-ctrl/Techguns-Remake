package techguns.items.guns;
import java.util.*;
import java.util.function.Supplier;
import com.google.common.collect.*;
import net.minecraft.core.*;
import net.minecraft.sounds.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import techguns.*;
import techguns.capabilities.TGExtendedPlayer;
import techguns.damagesystem.TGDamageSource;
import techguns.api.damagesystem.DamageType;
import techguns.deatheffects.EntityDeathUtils.DeathType;
import techguns.entities.projectiles.*;
import techguns.items.guns.ammo.AmmoType;
/** Common original pneumatic/fuel tool mechanics, retaining distinct attack and mining settings. */
public final class MiningGun extends GenericGunCharge implements LegacyMeleeWeapon {
    public enum Tool {POWERHAMMER,CHAINSAW,MININGDRILL,SHISHKEBAP}
    public static final net.minecraft.tags.TagKey<net.minecraft.world.level.block.Block> SHISHKEBAP_CUTS=BlockTags.create(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(techguns.Techguns.MODID,"mineable/shishkebap"));
    private final Tool tool;
    private final Map<String,Integer> miningLevels=new LinkedHashMap<>();
    private float poweredDamage=6,emptyDamage=2,digSpeed=1;
    private int miningRadius,swingSoundDelay=5;
    private boolean miningPlane;
    private boolean ambient;
    public MiningGun(Tool tool,int interval,int clip,int reload,int range,int recoil,float damage,float speed,float accuracy,float start,float end,float minimum,Supplier<AmmoType> ammo,Supplier<SoundEvent> fire,Supplier<SoundEvent> reloadSound,float fullCharge,int consumption) {
        super(interval,clip,reload,range,recoil,damage,speed,accuracy,start,end,minimum,ammo,fire,reloadSound,fullCharge,consumption);this.tool=tool;
        if(tool==Tool.SHISHKEBAP)setNoBowAnim();
    }
    public Tool tool(){return tool;}
    @Override protected void initializeAmmo(ItemStack stack){super.initializeAmmo(stack);if(!stack.getOrCreateTag().contains("miningHead"))stack.getOrCreateTag().putInt("miningHead",0);}
    public MiningGun setMeleeDmg(float powered,float empty){poweredDamage=powered;emptyDamage=empty;return this;}
    public MiningGun setTool(String name,int level){miningLevels.put(name,level);return this;}
    public MiningGun setDigSpeed(float speed){digSpeed=speed;return this;}
    public MiningGun setMiningRadius(int radius){miningRadius=radius;return this;}
    public MiningGun setSwingSoundDelay(int delay){swingSoundDelay=delay;return this;}
    public MiningGun setHasAmbient(){ambient=true;return this;}
    public boolean hasAmbient(){return ambient;}
    public int getMiningHeadLevel(ItemStack stack){return Mth.clamp(stack.getOrCreateTag().getInt("miningHead"),0,tool==Tool.MININGDRILL?1:2);}
    public int getMiningHeadLevelForHead(ItemStack head) {
        return switch(tool) {
            case POWERHAMMER -> head.is(TGItems.POWERHAMMERHEAD_OBSIDIAN.get())?1:head.is(TGItems.POWERHAMMERHEAD_CARBON.get())?2:0;
            case CHAINSAW -> head.is(TGItems.CHAINSAWBLADES_OBSIDIAN.get())?1:head.is(TGItems.CHAINSAWBLADES_CARBON.get())?2:0;
            case MININGDRILL -> head.is(TGItems.MININGDRILLHEAD_CARBON.get())?1:0;
            case SHISHKEBAP -> 0;
        };
    }
    public float getEffectiveDigSpeed(ItemStack stack){return digSpeed+getMiningHeadLevel(stack)*(tool==Tool.CHAINSAW?3:2);}
    private int harvestLevel(ItemStack stack){return miningLevels.values().stream().mapToInt(Integer::intValue).max().orElse(0)+getMiningHeadLevel(stack);}
    public boolean isEffectiveToolForState(ItemStack stack,BlockState state) {
        if(getCurrentAmmo(stack)<1)return false;
        // 1.12 bricks had no harvest tool, so the Shishkebap cut through them; 1.20 makes them pickaxe-only
        if(tool==Tool.SHISHKEBAP&&state.is(SHISHKEBAP_CUTS))return true;
        if(state.is(BlockTags.MINEABLE_WITH_PICKAXE))return miningLevels.containsKey("pickaxe");
        if(state.is(BlockTags.MINEABLE_WITH_SHOVEL))return miningLevels.containsKey("shovel");
        if(state.is(BlockTags.MINEABLE_WITH_AXE))return miningLevels.containsKey("axe");
        if(state.is(BlockTags.MINEABLE_WITH_HOE))return false;
        return true;
    }
    @Override public float getDestroySpeed(ItemStack stack,BlockState state){return isEffectiveToolForState(stack,state)?getEffectiveDigSpeed(stack):1;}
    @Override public boolean isCorrectToolForDrops(ItemStack stack,BlockState state) {
        int level=harvestLevel(stack);
        return isEffectiveToolForState(stack,state)&&(!state.is(BlockTags.NEEDS_DIAMOND_TOOL)||level>=3)&&(!state.is(BlockTags.NEEDS_IRON_TOOL)||level>=2)&&(!state.is(BlockTags.NEEDS_STONE_TOOL)||level>=1);
    }
    @Override public boolean canPerformAction(ItemStack stack,ToolAction action) {
        if(action==ToolActions.SWORD_SWEEP)return getCurrentAmmo(stack)>0;
        return action==ToolActions.PICKAXE_DIG&&miningLevels.containsKey("pickaxe")||action==ToolActions.SHOVEL_DIG&&miningLevels.containsKey("shovel")||action==ToolActions.AXE_DIG&&miningLevels.containsKey("axe")||action==ToolActions.SWORD_DIG&&miningLevels.containsKey("sword");
    }
    @Override public Multimap<Attribute,AttributeModifier> getAttributeModifiers(EquipmentSlot slot,ItemStack stack) {
        var result=HashMultimap.<Attribute,AttributeModifier>create();
        if(slot==EquipmentSlot.MAINHAND){result.put(Attributes.ATTACK_DAMAGE,new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Weapon modifier",getCurrentAmmo(stack)>0?poweredDamage:emptyDamage,AttributeModifier.Operation.ADDITION));result.put(Attributes.ATTACK_SPEED,new AttributeModifier(BASE_ATTACK_SPEED_UUID,"Weapon modifier",-2.4000000953674316,AttributeModifier.Operation.ADDITION));}
        return result;
    }
    public void animateMelee(Player player){predictAnimation(player,InteractionHand.MAIN_HAND,false,recoiltime,0);}
    public boolean hasSwordSweep(){return true;}
    public TGDamageSource getMeleeDamageSource(Player player,ItemStack stack) {
        boolean powered=getCurrentAmmo(stack)>0;
        var source=new TGDamageSource(player.level(),"tg_melee",player,player,tool==Tool.SHISHKEBAP?DamageType.FIRE:DamageType.PHYSICAL,powered?DeathType.GORE:DeathType.DEFAULT);
        if(powered){source.goreChance=tool==Tool.SHISHKEBAP?0.25F:tool==Tool.MININGDRILL?0.5F:1;source.armorPenetration=getPenetration();source.knockbackMultiplier=tool==Tool.POWERHAMMER?1.5F:1;}
        return source;
    }
    public void consumeAmmoOnMeleeHit(LivingEntity entity,ItemStack stack){if(!(entity instanceof Player player)||!player.getAbilities().instabuild)useAmmo(stack,1);}
    public void onMeleeHitTarget(ItemStack stack,Entity target){if(tool==Tool.SHISHKEBAP&&getCurrentAmmo(stack)>0)target.setSecondsOnFire(3);}
    public void doSweepAttackEffect(Player player) {
        if(player.level().isClientSide)return;
        var p=player.position().add(-Mth.sin(player.getYRot()*Mth.DEG_TO_RAD),player.getBbHeight()*0.8,Mth.cos(player.getYRot()*Mth.DEG_TO_RAD));
        TGProjectilePackets.effect(player,"PowerhammerImpact",p,0,0);
        player.level().playSound(null,player.getX(),player.getY(),player.getZ(),tool==Tool.CHAINSAW?TGSounds.CHAINSAW_HIT.get():tool==Tool.MININGDRILL?TGSounds.DRILLER_HIT.get():TGSounds.POWERHAMMER_IMPACT.get(),SoundSource.PLAYERS,tool==Tool.CHAINSAW?0.65F:1,1);
    }
    @Override public boolean onLeftClickEntity(ItemStack stack,Player player,Entity target){
        if(player instanceof ServerPlayer server&&getCurrentAmmo(stack)>0)TGWeaponPackets.animate(server,InteractionHand.MAIN_HAND,false,recoiltime,0);
        return LegacyMeleeAttack.attack(this,stack,player,target);
    }
    @Override public boolean onEntitySwing(ItemStack stack,LivingEntity entity) {
        if(getCurrentAmmo(stack)<=0)return false;
        if(entity instanceof Player player) {
            var state=TGExtendedPlayer.find(player).orElse(null);if(state==null)return false;
            if(entity.level().isClientSide){if(!state.getAttackTime(false).isRecoiling())predictAnimation(player,InteractionHand.MAIN_HAND,false,5,0);}
            else if(state.swingSoundDelay<=0&&player.containerMenu==player.inventoryMenu){state.swingSoundDelay=swingSoundDelay;player.level().playSound(null,player.getX(),player.getY(),player.getZ(),swingSound(),SoundSource.PLAYERS,0.65F,1);}
        }
        return tool!=Tool.SHISHKEBAP;
    }
    private SoundEvent swingSound(){return (tool==Tool.CHAINSAW?TGSounds.CHAINSAW_LOOP:tool==Tool.MININGDRILL?TGSounds.DRILLER_SWING:tool==Tool.SHISHKEBAP?TGSounds.SHISHKEBAP_SWING:TGSounds.POWERHAMMER_SWING).get();}
    @Override public boolean mineBlock(ItemStack stack,Level level,BlockState state,BlockPos pos,LivingEntity miner) {
        if(miningPlane)return true;
        if(getCurrentAmmo(stack)<=0)return false;
        useAmmo(stack,1);
        if(tool!=Tool.SHISHKEBAP)level.playSound(miner instanceof Player player?player:null,pos,tool==Tool.CHAINSAW?TGSounds.CHAINSAW_HIT.get():tool==Tool.MININGDRILL?TGSounds.DRILLER_BREAK.get():TGSounds.POWERHAMMER_IMPACT.get(),SoundSource.PLAYERS,0.65F,1);
        return true;
    }
    @Override public boolean onBlockStartBreak(ItemStack stack,BlockPos centre,Player player) {
        if(tool==Tool.MININGDRILL&&player.isShiftKeyDown()&&player instanceof ServerPlayer server&&getCurrentAmmo(stack)>0)return silkHarvest(stack,centre,server);
        if(!(player instanceof ServerPlayer server)||player.isShiftKeyDown()||miningRadius==0||miningPlane)return false;
        var hit=player.pick(6,0,false);if(!(hit instanceof BlockHitResult block))return false;
        var axis=block.getDirection().getAxis();miningPlane=true;
        try {
            for(int a=-miningRadius;a<=miningRadius;a++)for(int b=-miningRadius;b<=miningRadius;b++) {
                if(a==0&&b==0)continue;var pos=centre.offset(axis==Direction.Axis.X?0:a,axis==Direction.Axis.Y?0:axis==Direction.Axis.X?a:b,axis==Direction.Axis.Z?0:b);
                if(!player.level().hasChunkAt(pos)||player.level().getBlockEntity(pos)!=null)continue;
                var state=player.level().getBlockState(pos);
                if(!state.isAir()&&state.getDestroySpeed(player.level(),pos)>=0&&isCorrectToolForDrops(stack,state))server.gameMode.destroyBlock(pos);
            }
        } finally {miningPlane=false;}
        return false;
    }
    private boolean silkHarvest(ItemStack stack,BlockPos pos,ServerPlayer player) {
        var level=player.serverLevel();var state=level.getBlockState(pos);
        if(state.isAir()||state.getDestroySpeed(level,pos)<0||!isCorrectToolForDrops(stack,state))return false;
        if(player.blockActionRestricted(level,pos,player.gameMode.getGameModeForPlayer()))return true;
        int xp=net.minecraftforge.common.ForgeHooks.onBlockBreakEvent(level,player.gameMode.getGameModeForPlayer(),player,pos);
        if(xp<0)return true;
        var block=state.getBlock();var tile=level.getBlockEntity(pos);var lootTool=stack.copy();lootTool.enchant(Enchantments.SILK_TOUCH,1);
        block.playerWillDestroy(level,pos,state,player);
        boolean creative=player.getAbilities().instabuild;
        if(state.onDestroyedByPlayer(level,pos,player,!creative,level.getFluidState(pos))) {
            block.destroy(level,pos,state);
            if(!creative) {block.playerDestroy(level,player,pos,state,tile,lootTool);mineBlock(stack,level,state,pos,player);}
            level.levelEvent(2001,pos,net.minecraft.world.level.block.Block.getId(state));
        }
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket(level,pos));return true;
    }
    @Override public InteractionResultHolder<ItemStack> use(Level level,Player player,InteractionHand hand) {
        if(tool==Tool.POWERHAMMER)return super.use(level,player,hand);
        var stack=player.getItemInHand(hand);if(tool==Tool.SHISHKEBAP||hand!=InteractionHand.MAIN_HAND)return InteractionResultHolder.pass(stack);
        player.startUsingItem(hand);shootGunPrimary(stack,level,player,hand,false);return InteractionResultHolder.consume(stack);
    }
    @Override public void onUseTick(Level level,LivingEntity entity,ItemStack stack,int remaining){if(tool!=Tool.POWERHAMMER&&entity instanceof Player player)shootGunPrimary(stack,level,player,player.getUsedItemHand(),false);}
    @Override public boolean shootGunPrimary(ItemStack stack,Level level,Player player,InteractionHand hand,boolean aiming) {
        if(tool==Tool.SHISHKEBAP)return false;
        return tool==Tool.POWERHAMMER?super.shootGunPrimary(stack,level,player,hand,aiming):superFire(stack,level,player,hand,aiming);
    }
    private boolean superFire(ItemStack stack,Level level,Player player,InteractionHand hand,boolean aiming) {
        // Continuous tools fire while the right button is held, rather than discharging on release.
        fireWhileCharging=true;return super.shootGunPrimary(stack,level,player,hand,aiming);
    }
    @Override public void releaseUsing(ItemStack stack,Level level,LivingEntity entity,int remaining){if(tool==Tool.POWERHAMMER)super.releaseUsing(stack,level,entity,remaining);}
    @Override protected GenericProjectile createProjectile(ItemStack stack,Level level,Player player,EnumBulletFirePos side,float spread,boolean blocks){return new ToolProjectile(level,player,damage,speed,(int)Math.ceil(maxRange/speed),spread,damageDropStart,damageDropEnd,damageMin,getPenetration(),blocks,side,tool==Tool.POWERHAMMER);}
    @Override protected GenericProjectile createProjectileForLiving(ItemStack stack,Level level,LivingEntity shooter,EnumBulletFirePos side,float spread,boolean blocks,float damageScale){return new ToolProjectile(level,shooter,damage*damageScale,speed,(int)Math.ceil(maxRange/speed),spread,damageDropStart,damageDropEnd,damageMin*damageScale,getPenetration(),blocks,side,tool==Tool.POWERHAMMER);}
    @Override protected GenericProjectile createChargedProjectile(ItemStack stack,Level level,Player player,EnumBulletFirePos side,float charge,int consumed) {
        float bonus=0.5F*getMiningHeadLevel(stack);
        return new ToolProjectile(level,player,(damage+bonus)*consumed,speed,maxRange,accuracy,damageDropStart,damageDropEnd,(damageMin+bonus)*consumed,getPenetration(),!TGExtendedPlayer.get(player).enableSafemode,side,true);
    }
}
