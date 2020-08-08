package codechicken.lib.vec;

import net.minecraft.util.math.vector.Vector3i;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class Translation extends Transformation {

    public static final Translation CENTER = new Translation(Vector3.CENTER);

    public Vector3 vec;

    public Translation(Vector3 vec) {
        this.vec = vec;
    }

    public Translation(Vector3i vec) {
        this.vec = Vector3.fromVec3i(vec);
    }

    public Translation(double x, double y, double z) {
        this(new Vector3(x, y, z));
    }

    @Override
    public void apply(Vector3 vec) {
        vec.add(this.vec);
    }

    @Override
    public void applyN(Vector3 normal) {
    }

    @Override
    public void apply(Matrix4 mat) {
        mat.translate(vec);
    }

    @Override
    public Transformation at(Vector3 point) {
        return this;
    }

    //    @Override
    //    @OnlyIn (Dist.CLIENT)
    //    public void glApply() {
    //        GlStateManager.translated(vec.x, vec.y, vec.z);
    //    }

    @Override
    public Transformation inverse() {
        return new Translation(-vec.x, -vec.y, -vec.z);
    }

    @Override
    public Transformation merge(Transformation next) {
        if (next instanceof Translation) {
            return new Translation(vec.copy().add(((Translation) next).vec));
        }

        return null;
    }

    @Override
    public boolean isRedundant() {
        return vec.equalsT(Vector3.ZERO);
    }

    @Override
    public String toString() {
        MathContext cont = new MathContext(4, RoundingMode.HALF_UP);
        return "Translation(" + new BigDecimal(vec.x, cont) + ", " + new BigDecimal(vec.y, cont) + ", " + new BigDecimal(vec.z, cont) + ")";
    }
}
