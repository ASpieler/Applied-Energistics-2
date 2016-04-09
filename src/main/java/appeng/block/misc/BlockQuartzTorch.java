/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

package appeng.block.misc;


import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderQuartzTorch;
import appeng.client.render.effects.LightningFX;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.helpers.MetaRotation;


public class BlockQuartzTorch extends AEBaseBlock implements IOrientableBlock, ICustomCollision
{
	public BlockQuartzTorch()
	{
		super( Material.circuits );

		this.setFeature( EnumSet.of( AEFeature.DecorativeLights ) );
		this.setLightLevel( 0.9375F );
		this.setLightOpacity( 0 );
		this.setFullSize( false );
		this.setOpaque( false );
	}

	@Override
	public int getMetaFromState( final IBlockState state )
	{
		return 0;
	}

	@Override
	public IBlockState getStateFromMeta( final int meta )
	{
		return this.getDefaultState();
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { BlockTorch.FACING };
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderQuartzTorch.class;
	}

	@Override
	public boolean isValidOrientation( final World w, final BlockPos pos, final EnumFacing forward, final EnumFacing up )
	{
		return this.canPlaceAt( w, pos, up.getOpposite() );
	}

	private boolean canPlaceAt( final World w, final BlockPos pos, final EnumFacing dir )
	{
		final BlockPos test = pos.offset( dir );
		return w.isSideSolid( test, dir.getOpposite(), false );
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity e, final boolean isVisual )
	{
		final EnumFacing up = this.getOrientable( w, pos ).getUp();
		final double xOff = -0.3 * up.getFrontOffsetX();
		final double yOff = -0.3 * up.getFrontOffsetY();
		final double zOff = -0.3 * up.getFrontOffsetZ();
		return Collections.singletonList( AxisAlignedBB.fromBounds( xOff + 0.3, yOff + 0.3, zOff + 0.3, xOff + 0.7, yOff + 0.7, zOff + 0.7 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List out, final Entity e )
	{/*
	 * double xOff = -0.15 * getUp().offsetX; double yOff = -0.15 * getUp().offsetY; double zOff = -0.15 *
	 * getUp().offsetZ; out.add( AxisAlignedBB.getBoundingBox( xOff + (double) x + 0.15, yOff + (double) y + 0.15, zOff
	 * + (double) z + 0.15,// ahh xOff + (double) x + 0.85, yOff + (double) y + 0.85, zOff + (double) z + 0.85 ) );
	 */
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void randomDisplayTick( final World w, final BlockPos pos, final IBlockState state, final Random r )
	{
		if( !AEConfig.instance.enableEffects )
		{
			return;
		}

		if( r.nextFloat() < 0.98 )
		{
			return;
		}

		final EnumFacing up = this.getOrientable( w, pos ).getUp();
		final double xOff = -0.3 * up.getFrontOffsetX();
		final double yOff = -0.3 * up.getFrontOffsetY();
		final double zOff = -0.3 * up.getFrontOffsetZ();
		for( int bolts = 0; bolts < 3; bolts++ )
		{
			if( CommonHelper.proxy.shouldAddParticles( r ) )
			{
				final LightningFX fx = new LightningFX( w, xOff + 0.5 + pos.getX(), yOff + 0.5 + pos.getY(), zOff + 0.5 + pos.getZ(), 0.0D, 0.0D, 0.0D );

				Minecraft.getMinecraft().effectRenderer.addEffect( fx );
			}
		}
	}

	@Override
	public void onNeighborBlockChange( final World w, final BlockPos pos, final IBlockState state, final Block neighborBlock )
	{
		final EnumFacing up = this.getOrientable( w, pos ).getUp();
		if( !this.canPlaceAt( w, pos, up.getOpposite() ) )
		{
			this.dropTorch( w, pos );
		}
	}

	private void dropTorch( final World w, final BlockPos pos )
	{
		w.destroyBlock( pos, true );
		w.markBlockForUpdate( pos );
	}

	@Override
	public boolean canPlaceBlockAt( final World w, final BlockPos pos )
	{
		for( final EnumFacing dir : EnumFacing.VALUES )
		{
			if( this.canPlaceAt( w, pos, dir ) )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean usesMetadata()
	{
		return true;
	}

	@Override
	public IOrientable getOrientable( final IBlockAccess w, final BlockPos pos )
	{
		return new MetaRotation( w, pos, true );
	}
}
