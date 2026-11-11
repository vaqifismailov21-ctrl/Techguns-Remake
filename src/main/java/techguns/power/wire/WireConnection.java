package techguns.power.wire;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

/** One wire between two points. Equal no matter which end is named first. */
public record WireConnection(WireNode a,WireNode b,WireTier tier,float length,boolean insulated){
    public WireConnection(WireNode a,WireNode b,WireTier tier,float length){this(a,b,tier,length,false);}
    public WireNode other(WireNode node){return node.equals(a)?b:a;}
    public boolean touches(BlockPos pos){return a.pos().equals(pos)||b.pos().equals(pos);}
    public boolean sameEnds(WireConnection o){return a.equals(o.a)&&b.equals(o.b)||a.equals(o.b)&&b.equals(o.a);}
    public CompoundTag save(){
        var t=new CompoundTag();t.putLong("A",a.pos().asLong());t.putInt("AP",a.point());t.putLong("B",b.pos().asLong());t.putInt("BP",b.point());
        t.putInt("Tier",tier.ordinal());t.putFloat("Length",length);if(insulated)t.putBoolean("Insulated",true);return t;
    }
    public static WireConnection load(CompoundTag t){
        return new WireConnection(new WireNode(BlockPos.of(t.getLong("A")),t.getInt("AP")),new WireNode(BlockPos.of(t.getLong("B")),t.getInt("BP")),WireTier.of(t.getInt("Tier")),t.getFloat("Length"),t.getBoolean("Insulated"));
    }
    public void write(FriendlyByteBuf buf){buf.writeBlockPos(a.pos());buf.writeVarInt(a.point());buf.writeBlockPos(b.pos());buf.writeVarInt(b.point());buf.writeVarInt(tier.ordinal());buf.writeFloat(length);buf.writeBoolean(insulated);}
    public static WireConnection read(FriendlyByteBuf buf){
        return new WireConnection(new WireNode(buf.readBlockPos(),buf.readVarInt()),new WireNode(buf.readBlockPos(),buf.readVarInt()),WireTier.of(buf.readVarInt()),buf.readFloat(),buf.readBoolean());
    }
}
