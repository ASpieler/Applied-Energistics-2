/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.render;


import java.util.EnumSet;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.exceptions.MissingDefinition;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.util.AEPartLocation;
import appeng.api.util.IAESprite;
import appeng.api.util.ModelGenerator;
import appeng.block.AEBaseBlock;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.tile.AEBaseTile;


@SideOnly( Side.CLIENT )
public final class BusRenderHelper implements IPartRenderHelper
{
	public static final BusRenderHelper INSTANCE = new BusRenderHelper();
	private static final int HEX_WHITE = 0xffffff;

	private final BoundBoxCalculator bbc;
	private final boolean noAlphaPass;
	private final BaseBlockRender bbr;
	private final Optional<Block> maybeBlock;
	private final Optional<AEBaseBlock> maybeBaseBlock;
	private int renderingForPass;
	private int currentPass;
	private int itemsRendered;
	private double minX;
	private double minY;
	private double minZ;
	private double maxX;
	private double maxY;
	private double maxZ;
	private EnumFacing ax;
	private EnumFacing ay;
	private EnumFacing az;
	private int color;

	public BusRenderHelper()
	{
		this.bbc = new BoundBoxCalculator( this );
		this.noAlphaPass = !AEConfig.instance.isFeatureEnabled( AEFeature.AlphaPass );
		this.bbr = new BaseBlockRender<AEBaseBlock, AEBaseTile>();
		this.renderingForPass = 0;
		this.currentPass = 0;
		this.itemsRendered = 0;
		this.minX = 0;
		this.minY = 0;
		this.minZ = 0;
		this.maxX = 16;
		this.maxY = 16;
		this.maxZ = 16;
		this.ax = EnumFacing.EAST;
		this.az = EnumFacing.SOUTH;
		this.ay = EnumFacing.UP;
		this.color = HEX_WHITE;
		this.maybeBlock = AEApi.instance().definitions().blocks().multiPart().maybeBlock();
		this.maybeBaseBlock = this.maybeBlock.transform( new BaseBlockTransformFunction() );
	}

	public int getItemsRendered()
	{
		return this.itemsRendered;
	}

	public void setPass( final int pass )
	{
		this.renderingForPass = 0;
		this.currentPass = pass;
		this.itemsRendered = 0;
	}

	public double getBound( final AEPartLocation side )
	{
		switch( side )
		{
			default:
			case INTERNAL:
				return 0.5;
			case DOWN:
				return this.minY;
			case EAST:
				return this.maxX;
			case NORTH:
				return this.minZ;
			case SOUTH:
				return this.maxZ;
			case UP:
				return this.maxY;
			case WEST:
				return this.minX;
		}
	}

	/*
	 * public void setRenderColor( int color )
	 * {
	 * for( Block block : AEApi.instance().definitions().blocks().multiPart().maybeBlock().asSet() )
	 * {
	 * final BlockCableBus cableBus = (BlockCableBus) block;
	 * cableBus.setRenderColor( color );
	 * }
	 * }
	 */

	public void setOrientation( final EnumFacing dx, final EnumFacing dy, final EnumFacing dz )
	{
		this.ax = dx == null ? EnumFacing.EAST : dx;
		this.ay = dy == null ? EnumFacing.UP : dy;
		this.az = dz == null ? EnumFacing.SOUTH : dz;
	}

	public double[] getBounds()
	{
		return new double[] { this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ };
	}

	public void setBounds( final double[] bounds )
	{
		if( bounds == null || bounds.length != 6 )
		{
			return;
		}

		this.minX = bounds[0];
		this.minY = bounds[1];
		this.minZ = bounds[2];
		this.maxX = bounds[3];
		this.maxY = bounds[4];
		this.maxZ = bounds[5];
	}

	private static class BoundBoxCalculator implements IPartCollisionHelper
	{
		private final BusRenderHelper helper;
		private boolean started = false;

		private float minX;
		private float minY;
		private float minZ;

		private float maxX;
		private float maxY;
		private float maxZ;

		public BoundBoxCalculator( final BusRenderHelper helper )
		{
			this.helper = helper;
		}

		@Override
		public void addBox( final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ )
		{
			if( this.started )
			{
				this.minX = Math.min( this.minX, (float) minX );
				this.minY = Math.min( this.minY, (float) minY );
				this.minZ = Math.min( this.minZ, (float) minZ );
				this.maxX = Math.max( this.maxX, (float) maxX );
				this.maxY = Math.max( this.maxY, (float) maxY );
				this.maxZ = Math.max( this.maxZ, (float) maxZ );
			}
			else
			{
				this.started = true;
				this.minX = (float) minX;
				this.minY = (float) minY;
				this.minZ = (float) minZ;
				this.maxX = (float) maxX;
				this.maxY = (float) maxY;
				this.maxZ = (float) maxZ;
			}
		}

		@Override
		public EnumFacing getWorldX()
		{
			return this.helper.ax;
		}

		@Override
		public EnumFacing getWorldY()
		{
			return this.helper.ay;
		}

		@Override
		public EnumFacing getWorldZ()
		{
			return this.helper.az;
		}

		@Override
		public boolean isBBCollision()
		{
			return false;
		}
	}

	private static final class BaseBlockTransformFunction implements Function<Block, AEBaseBlock>
	{
		@Nullable
		@Override
		public AEBaseBlock apply( final Block input )
		{
			if( input instanceof AEBaseBlock )
			{
				return( (AEBaseBlock) input );
			}

			return null;
		}
	}

	@Override
	public void renderForPass( final int pass )
	{
		this.renderingForPass = pass;
	}

	private boolean renderThis()
	{
		if( this.renderingForPass == this.currentPass || this.noAlphaPass )
		{
			this.itemsRendered++;
			return true;
		}
		return false;
	}

	@Override
	public void setBounds( final float minX, final float minY, final float minZ, final float maxX, final float maxY, final float maxZ )
	{
		this.minX = minX;
		this.minY = minY;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxY = maxY;
		this.maxZ = maxZ;
	}

	@Override
	public void setInvColor( final int newColor )
	{
		this.color = newColor;
	}

	@Override
	public void setTexture( final IAESprite ico )
	{
		for( final AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			baseBlock.getRendererInstance().setTemporaryRenderIcon( ico );
		}
	}

	@Override
	public void setTexture( final IAESprite down, final IAESprite up, final IAESprite north, final IAESprite south, final IAESprite west, final IAESprite east )
	{
		final IAESprite[] list = new IAESprite[6];

		list[0] = down;
		list[1] = up;
		list[2] = north;
		list[3] = south;
		list[4] = west;
		list[5] = east;

		for( final AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			baseBlock.getRendererInstance().setTemporaryRenderIcons( list[this.mapRotation( EnumFacing.UP ).ordinal()], list[this.mapRotation( EnumFacing.DOWN ).ordinal()], list[this.mapRotation( EnumFacing.SOUTH ).ordinal()], list[this.mapRotation( EnumFacing.NORTH ).ordinal()], list[this.mapRotation( EnumFacing.EAST ).ordinal()], list[this.mapRotation( EnumFacing.WEST ).ordinal()] );
		}
	}

	private EnumFacing mapRotation( final EnumFacing dir )
	{
		final EnumFacing forward = this.az;
		final EnumFacing up = this.ay;

		if( forward == null || up == null )
		{
			return dir;
		}

		final int west_x = forward.getFrontOffsetY() * up.getFrontOffsetZ() - forward.getFrontOffsetZ() * up.getFrontOffsetY();
		final int west_y = forward.getFrontOffsetZ() * up.getFrontOffsetX() - forward.getFrontOffsetX() * up.getFrontOffsetZ();
		final int west_z = forward.getFrontOffsetX() * up.getFrontOffsetY() - forward.getFrontOffsetY() * up.getFrontOffsetX();

		EnumFacing west = null;
		for( final EnumFacing dx : EnumFacing.VALUES )
		{
			if( dx.getFrontOffsetX() == west_x && dx.getFrontOffsetY() == west_y && dx.getFrontOffsetZ() == west_z )
			{
				west = dx;
			}
		}

		if( dir == forward )
		{
			return EnumFacing.SOUTH;
		}
		if( dir == forward.getOpposite() )
		{
			return EnumFacing.NORTH;
		}

		if( dir == up )
		{
			return EnumFacing.UP;
		}
		if( dir == up.getOpposite() )
		{
			return EnumFacing.DOWN;
		}

		if( dir == west )
		{
			return EnumFacing.WEST;
		}
		if( dir == west.getOpposite() )
		{
			return EnumFacing.EAST;
		}

		return null;
	}

	@Override
	public void renderInventoryBox( final ModelGenerator renderer )
	{
		renderer.setRenderBounds( this.minX / 16.0, this.minY / 16.0, this.minZ / 16.0, this.maxX / 16.0, this.maxY / 16.0, this.maxZ / 16.0 );

		for( final AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			this.bbr.renderInvBlock( EnumSet.allOf( AEPartLocation.class ), baseBlock, null, this.color, renderer );
		}
	}

	@Override
	public void renderInventoryFace( final IAESprite icon, final EnumFacing face, final ModelGenerator renderer )
	{
		renderer.setRenderBounds( this.minX / 16.0, this.minY / 16.0, this.minZ / 16.0, this.maxX / 16.0, this.maxY / 16.0, this.maxZ / 16.0 );
		this.setTexture( icon );

		for( final AEBaseBlock baseBlock : this.maybeBaseBlock.asSet() )
		{
			this.bbr.renderInvBlock( EnumSet.of( AEPartLocation.fromFacing( face ) ), baseBlock, null, this.color, renderer );
		}
	}

	@Override
	public void renderBlock( final BlockPos pos, final ModelGenerator renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		for( final Block multiPart : AEApi.instance().definitions().blocks().multiPart().maybeBlock().asSet() )
		{
			final AEBaseBlock block = (AEBaseBlock) multiPart;

			final BlockRenderInfo info = block.getRendererInstance();
			final EnumFacing forward = BusRenderHelper.INSTANCE.az;
			final EnumFacing up = BusRenderHelper.INSTANCE.ay;

			renderer.setUvRotateBottom( info.getTexture( AEPartLocation.DOWN ).setFlip( BaseBlockRender.getOrientation( EnumFacing.DOWN, forward, up ) ) );
			renderer.setUvRotateTop( info.getTexture( AEPartLocation.UP ).setFlip( BaseBlockRender.getOrientation( EnumFacing.UP, forward, up ) ) );

			renderer.setUvRotateEast( info.getTexture( AEPartLocation.EAST ).setFlip( BaseBlockRender.getOrientation( EnumFacing.EAST, forward, up ) ) );
			renderer.setUvRotateWest( info.getTexture( AEPartLocation.WEST ).setFlip( BaseBlockRender.getOrientation( EnumFacing.WEST, forward, up ) ) );

			renderer.setUvRotateNorth( info.getTexture( AEPartLocation.NORTH ).setFlip( BaseBlockRender.getOrientation( EnumFacing.NORTH, forward, up ) ) );
			renderer.setUvRotateSouth( info.getTexture( AEPartLocation.SOUTH ).setFlip( BaseBlockRender.getOrientation( EnumFacing.SOUTH, forward, up ) ) );

			this.bbr.renderBlockBounds( renderer, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );

			renderer.renderStandardBlock( block, pos );
		}
	}

	@Override
	public Block getBlock()
	{
		for( final Block block : AEApi.instance().definitions().blocks().multiPart().maybeBlock().asSet() )
		{
			return block;
		}

		throw new MissingDefinition( "Tried to access the multi part block without it being defined." );
	}

	public void prepareBounds( final ModelGenerator renderer )
	{
		this.bbr.renderBlockBounds( renderer, this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, this.ax, this.ay, this.az );
	}

	@Override
	public void setFacesToRender( final EnumSet<EnumFacing> faces )
	{
		BusRenderer.INSTANCE.getRenderer().setRenderFaces( faces );
	}

	@Override
	public void renderBlockCurrentBounds( final BlockPos pos, final ModelGenerator renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		for( final Block block : this.maybeBlock.asSet() )
		{
			renderer.renderStandardBlock( block, pos );
		}
	}

	@Override
	public void renderFaceCutout( final BlockPos pos, final IAESprite ico, EnumFacing face, final float edgeThickness, final ModelGenerator renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		switch( face )
		{
			case DOWN:
				face = this.ay.getOpposite();
				break;
			case EAST:
				face = this.ax;
				break;
			case NORTH:
				face = this.az.getOpposite();
				break;
			case SOUTH:
				face = this.az;
				break;
			case UP:
				face = this.ay;
				break;
			case WEST:
				face = this.ax.getOpposite();
				break;
			default:
				break;
		}

		for( final AEBaseBlock block : this.maybeBaseBlock.asSet() )
		{
			this.bbr.renderCutoutFace( block, ico, pos, renderer, face, edgeThickness );
		}
	}

	@Override
	public void renderFace( final BlockPos pos, final IAESprite ico, EnumFacing face, final ModelGenerator renderer )
	{
		if( !this.renderThis() )
		{
			return;
		}

		this.prepareBounds( renderer );
		switch( face )
		{
			case DOWN:
				face = this.ay.getOpposite();
				break;
			case EAST:
				face = this.ax;
				break;
			case NORTH:
				face = this.az.getOpposite();
				break;
			case SOUTH:
				face = this.az;
				break;
			case UP:
				face = this.ay;
				break;
			case WEST:
				face = this.ax.getOpposite();
				break;
			default:
				break;
		}

		for( final AEBaseBlock block : this.maybeBaseBlock.asSet() )
		{
			this.bbr.renderFace( pos, block, ico, renderer, face );
		}
	}

	@Override
	public EnumFacing getWorldX()
	{
		return this.ax;
	}

	@Override
	public EnumFacing getWorldY()
	{
		return this.ay;
	}

	@Override
	public EnumFacing getWorldZ()
	{
		return this.az;
	}
}
