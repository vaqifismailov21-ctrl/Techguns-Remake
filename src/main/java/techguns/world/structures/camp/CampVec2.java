package techguns.world.structures.camp;
/** Original double-precision center distance used by the camp's split probabilities. */
public record CampVec2(double x,double y) {
    public static CampVec2 substract(CampVec2 a,CampVec2 b){return new CampVec2(a.x-b.x,a.y-b.y);}
    public double lenSquared(){return x*x+y*y;}
}
