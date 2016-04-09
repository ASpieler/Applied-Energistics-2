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

package appeng.client.render.blocks;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import appeng.api.util.ModelGenerator;
import appeng.block.networking.BlockController;
import appeng.block.networking.BlockController.ControllerBlockState;
import appeng.client.render.BaseBlockRender;
import appeng.client.texture.ExtraBlockTextures;
import appeng.tile.networking.TileController;


public class RenderBlockController extends BaseBlockRender<BlockController, TileController>
{

	public RenderBlockController()
	{
		super( false, 20 );
	}

	@Override
	public boolean renderInWorld( final BlockController blk, final IBlockAccess world, final BlockPos pos, final ModelGenerator renderer )
	{

		final boolean xx = this.getTileEntity( world, pos.offset( EnumFacing.WEST ) ) instanceof TileController && this.getTileEntity( world, pos.offset( EnumFacing.EAST ) ) instanceof TileController;
		final boolean yy = this.getTileEntity( world, pos.offset( EnumFacing.DOWN ) ) instanceof TileController && this.getTileEntity( world, pos.offset( EnumFacing.UP ) ) instanceof TileController;
		final boolean zz = this.getTileEntity( world, pos.offset( EnumFacing.SOUTH ) ) instanceof TileController && this.getTileEntity( world, pos.offset( EnumFacing.NORTH ) ) instanceof TileController;

		final BlockController.ControllerBlockState meta = (ControllerBlockState) world.getBlockState( pos ).getValue( BlockController.CONTROLLER_STATE );
		final boolean hasPower = meta != BlockController.ControllerBlockState.OFFLINE;
		final boolean isConflict = meta == BlockController.ControllerBlockState.CONFLICTED;

		ExtraBlockTextures lights = null;

		if( xx && !yy && !zz )
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumnPowered.getIcon() );
				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerColumnConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerColumnLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumn.getIcon() );
			}

			renderer.setUvRotateEast( 1 );
			renderer.setUvRotateWest( 1 );
			renderer.setUvRotateTop( 1 );
			renderer.setUvRotateBottom( 1 );
		}
		else if( !xx && yy && !zz )
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumnPowered.getIcon() );
				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerColumnConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerColumnLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumn.getIcon() );
			}

			renderer.setUvRotateEast( 0 );
			renderer.setUvRotateNorth( 0 );
		}
		else if( !xx && !yy && zz )
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumnPowered.getIcon() );
				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerColumnConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerColumnLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerColumn.getIcon() );
			}

			renderer.setUvRotateNorth( 1 );
			renderer.setUvRotateSouth( 1 );
			renderer.setUvRotateTop( 0 );
		}
		else if( ( xx ? 1 : 0 ) + ( yy ? 1 : 0 ) + ( zz ? 1 : 0 ) >= 2 )
		{
			final int v = ( Math.abs( pos.getX() ) + Math.abs( pos.getY() ) + Math.abs( pos.getZ() ) ) % 2;
			renderer.setUvRotateEast( renderer.setUvRotateBottom( renderer.setUvRotateNorth( renderer.setUvRotateSouth( renderer.setUvRotateTop( renderer.setUvRotateWest( 0 ) ) ) ) ) );

			if( v == 0 )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerInsideA.getIcon() );
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerInsideB.getIcon() );
			}
		}
		else
		{
			if( hasPower )
			{
				blk.getRendererInstance().setTemporaryRenderIcon( ExtraBlockTextures.BlockControllerPowered.getIcon() );
				if( isConflict )
				{
					lights = ExtraBlockTextures.BlockControllerConflict;
				}
				else
				{
					lights = ExtraBlockTextures.BlockControllerLights;
				}
			}
			else
			{
				blk.getRendererInstance().setTemporaryRenderIcon( null );
			}
		}

		final boolean out = renderer.renderStandardBlock( blk, pos );
		if( lights != null )
		{
			renderer.setColorOpaque_F( 1.0f, 1.0f, 1.0f );
			renderer.setBrightness( 14 << 20 | 14 << 4 );
			renderer.renderFaceXNeg( blk, pos, lights.getIcon() );
			renderer.renderFaceXPos( blk, pos, lights.getIcon() );
			renderer.renderFaceYNeg( blk, pos, lights.getIcon() );
			renderer.renderFaceYPos( blk, pos, lights.getIcon() );
			renderer.renderFaceZNeg( blk, pos, lights.getIcon() );
			renderer.renderFaceZPos( blk, pos, lights.getIcon() );
		}

		blk.getRendererInstance().setTemporaryRenderIcon( null );
		renderer.setUvRotateEast( renderer.setUvRotateBottom( renderer.setUvRotateNorth( renderer.setUvRotateSouth( renderer.setUvRotateTop( renderer.setUvRotateWest( 0 ) ) ) ) ) );
		return out;
	}

	private TileEntity getTileEntity( final IBlockAccess world, final BlockPos pos )
	{
		if( pos.getY() >= 0 )
		{
			return world.getTileEntity( pos );
		}
		return null;
	}
}
