package techguns.tileentities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import techguns.*;
import techguns.blocks.BioBlobBlock;
import techguns.damagesystem.*;
import techguns.api.damagesystem.DamageType;
import techguns.deatheffects.EntityDeathUtils.DeathType;
public final class BioBlobBlockEntity extends BlockEntity {
    private int ticks=60;
    public BioBlobBlockEntity(BlockPos pos,BlockState state) {super(TGWeaponBlocks.BIOBLOB_ENTITY.get(),pos,state);}
    public static void tick(Level level,BlockPos pos,BlockState state,BioBlobBlockEntity blob) {
        if(--blob.ticks>0)return;
        int size=state.getValue(BioBlobBlock.SIZE);
        if(size>0) {level.setBlockAndUpdate(pos,state.setValue(BioBlobBlock.SIZE,size-1));blob.ticks=60;blob.setChanged();}
        else level.removeBlock(pos,false);
    }
    public void hitBlob(int power,LivingEntity attacker) {
        if(level==null||level.isClientSide)return;
        int size=getBlockState().getValue(BioBlobBlock.SIZE)+1+power;ticks=60;
        if(size>3) {
            var position=Vec3.atCenterOf(worldPosition);
            var source=new TGDamageSource(level,"tg_poison",null,attacker,DamageType.POISON,DeathType.BIO);source.goreChance=1;source.armorPenetration=0.35F;
            level.removeBlock(worldPosition,false);
            new TGExplosion(level,attacker,null,position.x,position.y,position.z,30,15,3,4.5,0).setDmgSrc(source).doExplosion(false);
            level.playSound(null,worldPosition,TGSounds.DEATH_BIO.get(),net.minecraft.sounds.SoundSource.BLOCKS,4,1);
            TGProjectilePackets.effect(level,"bioblobExplosion",position,0,0);
        } else {level.setBlockAndUpdate(worldPosition,getBlockState().setValue(BioBlobBlock.SIZE,size-1));setChanged();}
    }
    @Override protected void loadAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {super.loadAdditional(tag, registries);ticks=tag.getInt("BlobTicks");}
    @Override protected void saveAdditional(CompoundTag tag, net.minecraft.core.HolderLookup.Provider registries) {super.saveAdditional(tag, registries);tag.putInt("BlobTicks",ticks);}
}
