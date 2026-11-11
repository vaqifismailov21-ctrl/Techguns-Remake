package techguns.power.wire;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import techguns.TGPackets;

/** Keeps the clients' copy of the wires (for drawing them) in step with the server's. */
public final class WirePackets {
    private WirePackets(){}
    /** All wires of a dimension (replace), or one wire added or removed. */
    public record Wires(ResourceLocation dimension,int mode,List<WireConnection> list){
        static final int ALL=0,ADD=1,REMOVE=2;
        void encode(FriendlyByteBuf buf){buf.writeResourceLocation(dimension);buf.writeVarInt(mode);buf.writeVarInt(list.size());for(var c:list)c.write(buf);}
        static Wires decode(FriendlyByteBuf buf){
            var dim=buf.readResourceLocation();int mode=buf.readVarInt();int n=buf.readVarInt();var list=new ArrayList<WireConnection>(n);
            for(int i=0;i<n;i++)list.add(WireConnection.read(buf));return new Wires(dim,mode,list);
        }
    }
    public static void register(){
        TGPackets.NETWORK.messageBuilder(Wires.class,40,NetworkDirection.PLAY_TO_CLIENT).encoder(Wires::encode).decoder(Wires::decode)
            .consumerMainThread((msg,ctx)->{
                DistExecutor.unsafeRunWhenOn(Dist.CLIENT,()->()->techguns.client.render.WireClient.receive(ResourceKey.create(Registries.DIMENSION,msg.dimension()),msg.mode(),msg.list()));
                ctx.get().setPacketHandled(true);
            }).add();
    }
    public static void sendAll(ServerPlayer player){
        var level=player.serverLevel();
        TGPackets.NETWORK.send(PacketDistributor.PLAYER.with(()->player),new Wires(level.dimension().location(),Wires.ALL,new ArrayList<>(WireNetwork.get(level).all())));
    }
    static void added(ServerLevel level,WireConnection c){send(level,new Wires(level.dimension().location(),Wires.ADD,List.of(c)));}
    static void removed(ServerLevel level,WireConnection c){send(level,new Wires(level.dimension().location(),Wires.REMOVE,List.of(c)));}
    private static void send(ServerLevel level,Wires msg){TGPackets.NETWORK.send(PacketDistributor.DIMENSION.with(level::dimension),msg);}
    static ResourceKey<Level> key(ResourceLocation id){return ResourceKey.create(Registries.DIMENSION,id);}
}
