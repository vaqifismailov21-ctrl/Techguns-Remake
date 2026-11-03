package techguns.util;

import java.util.BitSet;

public final class DataUtil {
    private DataUtil() {}
    public static byte compress(boolean... values) {
        if (values.length > 8) throw new IllegalArgumentException("Cannot store more than 8 flags in one byte");
        byte result = 0;
        for (int i = 0; i < values.length; i++) if (values[i]) result |= 1 << i;
        return result;
    }
    public static BitSet uncompress(byte data) { return BitSet.valueOf(new byte[]{data}); }
}
