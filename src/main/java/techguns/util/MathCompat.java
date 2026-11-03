package techguns.util;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

/** JOML-style chaining helpers for the 1.19.2 com.mojang.math types. */
public final class MathCompat {
    private MathCompat() {}
    public static Quaternion identity() { return Quaternion.ONE.copy(); }
    public static Quaternion axis(float radians, Vector3f axis) { return new Quaternion(axis, radians, false); }
    public static Quaternion rotX(Quaternion q, float r) { q.mul(Vector3f.XP.rotation(r)); return q; }
    public static Quaternion rotY(Quaternion q, float r) { q.mul(Vector3f.YP.rotation(r)); return q; }
    public static Quaternion rotZ(Quaternion q, float r) { q.mul(Vector3f.ZP.rotation(r)); return q; }
    public static Vector3f rotZ(Vector3f v, float r) { v.transform(Vector3f.ZP.rotation(r)); return v; }
    public static Vector3f norm(Vector3f v) { v.normalize(); return v; }
    public static Vector3f rot(Vector3f v, Quaternion q) { v.transform(q); return v; }
    public static Vector3f mul(Vector3f v, float f) { v.mul(f); return v; }
    public static Vector3f add(Vector3f v, float x, float y, float z) { v.add(x, y, z); return v; }
    public static Vector3f cross(Vector3f a, float x, float y, float z) { a.cross(new Vector3f(x, y, z)); return a; }
    /** JOML setOrtho(left,right,bottom,top,near,far) equivalent. */
    public static com.mojang.math.Matrix4f ortho(float l, float r, float b, float t, float n, float f) { return com.mojang.math.Matrix4f.orthographic(l, r, t, b, n, f); }
    public static Quaternion conj(Quaternion q) { var c = q.copy(); c.conj(); return c; }
    public static float lengthSquared(Vector3f v) { return v.dot(v); }
}
