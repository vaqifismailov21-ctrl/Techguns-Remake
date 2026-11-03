package techguns.client.particle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

/** Original impactfx.txt values. Only the normal bullet's eight systems are enabled at this stage. */
public final class BulletImpactDefinition {
    public static final List<String> NAMES = List.of("BulletImpactFlare", "BulletImpactSparks", "BulletImpactDust",
            "BulletImpactDirt", "BulletImpactDebris", "BulletImpactSparksExtra", "BulletImpactDebrisWood", "BulletImpactDebrisGlass");
    public record Range(float min, float max) {
        public float sample(Random random) { return min + random.nextFloat() * (max-min); }
        public int sampleInt(Random random) { int lo = (int)Math.min(min,max), hi = (int)Math.max(min,max); return lo + random.nextInt(hi-lo+1); }
    }
    public record Alpha(float value, float time) {}
    public record Color(int r, int g, int b, float time) {}
    public ResourceLocation texture;
    public boolean additive, variations, noZTest;
    public int columns = 1, rows = 1, frames = 1;
    public float gravity, volumeRadius;
    public float volumeHeight;
    public String volumeType="POINT",velocityType="NONE";
    public Vec3 offset=Vec3.ZERO;
    public boolean hollow,groundAligned,removeOnGround,particlesMoveWithSystem,particlesStickToSystem,streak;
    public String attachedSystem;
    public final Map<String, Range> ranges = new HashMap<>();
    public final List<Alpha> alphas = new ArrayList<>();
    public final List<Color> colors = new ArrayList<>();
    public Range range(String name) {
        float value=switch(name) {
            case "lifetime" -> 20;
            case "systemlifetime" -> 5;
            case "angle","anglerate","angleratedamping","velocity1","velocity2","velocity3","initialdelay","systemvelocityfactor","startsizerate" -> 0;
            default -> 1;
        };
        return ranges.getOrDefault(name,new Range(value,value));
    }
    public static Map<String,BulletImpactDefinition> read(Reader reader) throws IOException {
        return read(reader,false);
    }
    public static Map<String,BulletImpactDefinition> read(Reader reader,boolean all) throws IOException {
        var result = new HashMap<String,BulletImpactDefinition>();
        String name = null; BulletImpactDefinition definition = null;
        for (String raw : new BufferedReader(reader).lines().toList()) {
            String line = raw.split("//", 2)[0].trim();
            if (line.isEmpty()) continue;
            if (line.endsWith("{")) {
                name = line.substring(0,line.length()-1).trim();
                definition = all || NAMES.contains(name) ? new BulletImpactDefinition() : null;
            } else if (line.equals("}")) {
                if (definition != null) result.put(name,definition);
                name = null; definition = null;
            } else if (definition != null) definition.property(line);
        }
        if (!all && result.size() != NAMES.size()) throw new IOException("Missing original bullet impact definitions: " + result.keySet());
        return Map.copyOf(result);
    }
    private void property(String line) throws IOException {
        String[] pair = line.split("=", 2); if (pair.length != 2) throw new IOException("Invalid effect property " + line);
        String key = pair[0].trim().toLowerCase(Locale.ROOT), value = pair[1].trim();
        String[] values = value.split("\\s+");
        switch (key) {
            case "texture" -> texture = ResourceLocation.parse(value.toLowerCase(Locale.ROOT));
            case "rendertype" -> { if (!value.equals("ALPHA") && !value.equals("ADDITIVE") && !value.equals("NO_Z_TEST")) throw new IOException("Unsupported bullet blend " + value); additive = !value.equals("ALPHA"); noZTest = value.equals("NO_Z_TEST"); }
            case "rows" -> rows = Integer.parseInt(value);
            case "columns" -> columns = Integer.parseInt(value);
            case "frames" -> frames = Integer.parseInt(value);
            case "hasvariations" -> variations = Boolean.parseBoolean(value);
            case "gravity" -> gravity = Float.parseFloat(value);
            case "volume1" -> volumeRadius = Float.parseFloat(value);
            case "volume2" -> volumeHeight=Float.parseFloat(value);
            case "volumetype" -> volumeType=value;
            case "velocitytype" -> velocityType=value;
            case "offset" -> offset=new Vec3(Double.parseDouble(values[0]),Double.parseDouble(values[1]),Double.parseDouble(values[2]));
            case "ishollow" -> hollow=Boolean.parseBoolean(value);
            case "groundaligned" -> groundAligned=Boolean.parseBoolean(value);
            case "removeonground" -> removeOnGround=Boolean.parseBoolean(value);
            case "particlesmovewithsystem" -> particlesMoveWithSystem=Boolean.parseBoolean(value);
            case "particlessticktosystem" -> particlesStickToSystem=Boolean.parseBoolean(value);
            case "streak" -> streak=Boolean.parseBoolean(value);
            case "attachedsystem" -> attachedSystem=value;
            case "randomrotation" -> { /* Original parser retains this obsolete key without using it. */ }
            case "alpha" -> alphas.add(new Alpha(Float.parseFloat(values[0]),Float.parseFloat(values[1])));
            case "color" -> colors.add(new Color(Integer.parseInt(values[0]),Integer.parseInt(values[1]),Integer.parseInt(values[2]),Float.parseFloat(values[3])));
            case "angle", "anglerate", "angleratedamping", "lifetime", "spawndelay", "systemlifetime", "size", "sizerate", "sizeratedamping",
                    "particlecount", "velocity1", "velocity2", "velocity3", "velocitydamping", "velocitydampingonground", "initialdelay", "systemvelocityfactor", "animationspeed", "startsizerate", "startsizeratedamping" ->
                    ranges.put(key, new Range(Float.parseFloat(values[0]),Float.parseFloat(values[1])));
            default -> throw new IOException("Unported bullet impact property " + key);
        }
    }
    public record Sample(Vec3 position,Vec3 motion) {}
    public Sample sample(Random random,float pitch,float yaw) {
        double angle=random.nextDouble()*2*Math.PI,y=1-random.nextDouble()*2,a=Math.sqrt(1-y*y);
        Vec3 direction=new Vec3(a*Math.cos(angle),y,a*Math.sin(angle)),position=Vec3.ZERO;
        float radius=hollow?volumeRadius:random.nextFloat()*volumeRadius;
        switch(volumeType) {
            case "SPHERE" -> position=direction.scale(radius);
            case "HEMISPHERE" -> {y=random.nextDouble();a=Math.sqrt(1-y*y);direction=new Vec3(a*Math.cos(angle),y,a*Math.sin(angle));position=direction.scale(radius);}
            case "CYLINDER" -> {direction=new Vec3(Math.cos(angle),0,-Math.sin(angle));position=direction.scale(radius).add(0,random.nextFloat()*volumeHeight,0);}
            case "CYLINDER2" -> {direction=new Vec3(0,Math.cos(angle),-Math.sin(angle));position=new Vec3(0,direction.y*radius,random.nextFloat()*volumeHeight);}
            case "TRAIL" -> {}
            case "POINT" -> {}
            default -> throw new IllegalStateException("Unported weapon particle volume "+volumeType);
        }
        double r=range("velocity1").sample(random);Vec3 motion;
        switch(velocityType) {
            case "ORTHO" -> motion=new Vec3(r,range("velocity2").sample(random),range("velocity3").sample(random));
            case "SPHERICAL","HEMISPHERICAL" -> {
                angle=random.nextDouble()*2*Math.PI;y=velocityType.equals("HEMISPHERICAL")?random.nextDouble():1-random.nextDouble()*2;a=Math.sqrt(1-y*y);
                motion=velocityType.equals("HEMISPHERICAL")?new Vec3(r*a*Math.cos(angle),r*a*Math.sin(angle),r*y):new Vec3(r*a*Math.cos(angle),r*y,r*a*Math.sin(angle));
            }
            case "OUTWARD" -> motion=volumeType.equals("SPHERE")||volumeType.equals("HEMISPHERE")?direction.scale(r):new Vec3(Math.cos(angle)*r,range("velocity2").sample(random),-Math.sin(angle)*r);
            case "NONE" -> motion=Vec3.ZERO;
            default -> throw new IllegalStateException("Unported weapon particle velocity "+velocityType);
        }
        return new Sample(position.xRot((float)Math.toRadians(-pitch)).yRot((float)Math.toRadians(-yaw)),motion.xRot((float)Math.toRadians(-pitch)).yRot((float)Math.toRadians(-yaw)).scale(0.05));
    }
    public Vec3 position(Random random) {
        double angle = random.nextDouble()*2*Math.PI, y = 1-random.nextDouble()*2, a = Math.sqrt(1-y*y);
        float radius = random.nextFloat()*volumeRadius;
        return new Vec3(a*Math.cos(angle)*radius,y*radius,a*Math.sin(angle)*radius);
    }
    public Vec3 velocity(Random random, float pitch, float yaw) {
        // TGParticleSystem converts units per second to units per tick after rotation.
        return new Vec3(range("velocity1").sample(random),range("velocity2").sample(random),range("velocity3").sample(random))
                .xRot((float)Math.toRadians(-pitch)).yRot((float)Math.toRadians(-yaw)).scale(0.05);
    }
    public int rgb(float progress) {
        if (colors.isEmpty()) return 0xFFFFFF;
        var a = colors.get(0); var b = a;
        for (int i=1;i<colors.size();i++) { b=colors.get(i); if (progress < b.time) break; else a=b; }
        if (a == b) return a.r<<16 | a.g<<8 | a.b;
        float p = (progress-a.time)/(b.time-a.time);
        float[] h1 = java.awt.Color.RGBtoHSB(a.r,a.g,a.b,null), h2 = java.awt.Color.RGBtoHSB(b.r,b.g,b.b,null);
        return java.awt.Color.HSBtoRGB(h1[0]*(1-p)+h2[0]*p,h1[1]*(1-p)+h2[1]*p,h1[2]*(1-p)+h2[2]*p) & 0xFFFFFF;
    }
    public float alpha(float progress) {
        if (alphas.isEmpty()) return 1;
        var a=alphas.get(0); var b=a;
        for (int i=1;i<alphas.size();i++) { b=alphas.get(i); if (progress < b.time) break; else a=b; }
        if (a.time == b.time) return a.value;
        float p=(progress-a.time)/(b.time-a.time); return a.value*(1-p)+b.value*p;
    }
}
