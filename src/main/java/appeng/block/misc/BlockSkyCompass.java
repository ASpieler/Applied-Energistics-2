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

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockSkyCompass;
import appeng.core.features.AEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.misc.TileSkyCompass;


public class BlockSkyCompass extends AEBaseTileBlock implements ICustomCollision
{

	public BlockSkyCompass()
	{
		super( Material.iron );
		this.setTileEntity( TileSkyCompass.class );
		this.setOpaque( this.setFullSize( false ) );
		this.lightOpacity = 0;
		this.setFeature( EnumSet.of( AEFeature.MeteoriteCompass ) );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockSkyCompass.class;
	}

	@Override
	public boolean isValidOrientation( final World w, final BlockPos pos, final EnumFacing forward, final EnumFacing up )
	{
		final TileSkyCompass sc = this.getTileEntity( w, pos );
		if( sc != null )
		{
			return false;
		}
		return this.canPlaceAt( w, pos, forward.getOpposite() );
	}

	private boolean canPlaceAt( final World w, final BlockPos pos, final EnumFacing dir )
	{
		return w.isSideSolid( pos.offset( dir ), dir.getOpposite(), false );
	}

	@Override
	public void onNeighborBlockChange( final World w, final BlockPos pos, final IBlockState state, final Block neighborBlock )
	{
		final TileSkyCompass sc = this.getTileEntity( w, pos );
		final EnumFacing up = sc.getForward();
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
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity thePlayer, final boolean b )
	{
		final TileSkyCompass tile = this.getTileEntity( w, pos );
		if( tile != null )
		{
			final EnumFacing forward = tile.getForward();

			double minX = 0;
			double minY = 0;
			double minZ = 0;
			double maxX = 1;
			double maxY = 1;
			double maxZ = 1;

			switch( forward )
			{
				case DOWN:
					minZ = minX = 5.0 / 16.0;
					maxZ = maxX = 11.0 / 16.0;
					maxY = 1.0;
					minY = 14.0 / 16.0;
					break;
				case EAST:
					minZ = minY = 5.0 / 16.0;
					maxZ = maxY = 11.0 / 16.0;
					maxX = 2.0 / 16.0;
					minX = 0.0;
					break;
				case NORTH:
					minY = minX = 5.0 / 16.0;
					maxY = maxX = 11.0 / 16.0;
					maxZ = 1.0;
					minZ = 14.0 / 16.0;
					break;
				case SOUTH:
					minY = minX = 5.0 / 16.0;
					maxY = maxX = 11.0 / 16.0;
					maxZ = 2.0 / 16.0;
					minZ = 0.0;
					break;
				case UP:
					minZ = minX = 5.0 / 16.0;
					maxZ = maxX = 11.0 / 16.0;
					maxY = 2.0 / 16.0;
					minY = 0.0;
					break;
				case WEST:
					minZ = minY = 5.0 / 16.0;
					maxZ = maxY = 11.0 / 16.0;
					maxX = 1.0;
					minX = 14.0 / 16.0;
					break;
				default:
					break;
			}

			return Collections.singletonList( AxisAlignedBB.fromBounds( minX, minY, minZ, maxX, maxY, maxZ ) );
		}
		return Collections.singletonList( AxisAlignedBB.fromBounds( 0.0, 0, 0.0, 1.0, 1.0, 1.0 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{

	}
}
