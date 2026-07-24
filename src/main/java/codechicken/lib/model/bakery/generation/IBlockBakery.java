package codechicken.lib.model.bakery.generation;

import codechicken.lib.model.bakery.ModelBakery;
import codechicken.lib.texture.TextureUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Created by covers1624 on 28/10/2016.
 */
public interface IBlockBakery extends IItemBakery {

    /**
     * Used to pass extended state handling from your block to your bakery.
     * Call {@link ModelBakery#handleExtendedState(IExtendedBlockState, IBlockAccess, BlockPos)} from {@link Block#getExtendedState(IBlockState, IBlockAccess, BlockPos)}
     *
     * @param state  The current state of your block.
     * @param access The world you are in.
     * @param pos    The position in that world.
     * @return Modified state.
     */
    @SideOnly (Side.CLIENT)
    IExtendedBlockState handleState(IExtendedBlockState state, IBlockAccess access, BlockPos pos);

    /**
     * Returns the texture used for block particles generated from this bakery.
     *
     * <p>The default implementation preserves compatibility with existing
     * bakeries. Implementations that have a suitable particle texture should
     * override this method.</p>
     *
     * @param state The extended block state being baked.
     * @return The particle texture, or the missing sprite when no texture is
     * available.
     */
    @SideOnly (Side.CLIENT)
    default TextureAtlasSprite getParticleTexture(IExtendedBlockState state) {
        return TextureUtils.getMissingSprite();
    }
}
