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

package appeng.block.crafting;


import java.util.EnumSet;
import java.util.List;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEPartLocation;
import appeng.api.util.IAESprite;
import appeng.block.AEBaseTileBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderBlockCraftingCPU;
import appeng.client.texture.ExtraBlockTextures;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.tile.crafting.TileCraftingTile;
import appeng.util.Platform;


/**
 *
 */
public class BlockCraftingUnit extends AEBaseTileBlock
{
	public static final PropertyBool POWERED = PropertyBool.create( "powered" );
	public static final PropertyBool FORMED = PropertyBool.create( "formed" );

	public final CraftingUnitType type;

	public BlockCraftingUnit( final CraftingUnitType type )
	{
		super( Material.iron, Optional.of( type.name() ) );

		this.type = type;
		this.setTileEntity( TileCraftingTile.class );
		this.setFeature( EnumSet.of( AEFeature.CraftingCPU ) );
	}

	@Override
	public IBlockState getStateFromMeta( final int meta )
	{
		return this.getDefaultState().withProperty( POWERED, ( meta & 1 ) == 1 ).withProperty( FORMED, ( meta & 2 ) == 2 );
	}

	@Override
	public int getMetaFromState( final IBlockState state )
	{
		final boolean p = (boolean) state.getValue( POWERED );
		final boolean f = (boolean) state.getValue( FORMED );
		return ( p ? 1 : 0 ) | ( f ? 2 : 0 );
	}

	@Override
	public void onNeighborBlockChange( final World worldIn, final BlockPos pos, final IBlockState state, final Block neighborBlock )
	{
		final TileCraftingTile cp = this.getTileEntity( worldIn, pos );
		if( cp != null )
		{
			cp.updateMultiBlock();
		}
	}

	@Override
	public EnumWorldBlockLayer getBlockLayer()
	{
		return EnumWorldBlockLayer.CUTOUT;
	}

	@Override
	public void breakBlock( final World w, final BlockPos pos, final IBlockState state )
	{
		final TileCraftingTile cp = this.getTileEntity( w, pos );
		if( cp != null )
		{
			cp.breakCluster();
		}

		super.breakBlock( w, pos, state );
	}

	@Override
	public boolean onBlockActivated( final World w, final BlockPos pos, final IBlockState state, final EntityPlayer p, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		final TileCraftingTile tg = this.getTileEntity( w, pos );
		if( tg != null && !p.isSneaking() && tg.isFormed() && tg.isActive() )
		{
			if( Platform.isClient() )
			{
				return true;
			}

			Platform.openGUI( p, tg, AEPartLocation.fromFacing( side ), GuiBridge.GUI_CRAFTING_CPU );
			return true;
		}

		return false;
	}

	protected String getItemUnlocalizedName( final ItemStack is )
	{
		return super.getUnlocalizedName( is );
	}

	@Override
	protected IProperty[] getAEStates()
	{
		return new IProperty[] { POWERED, FORMED };
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderBlockCraftingCPU.class;
	}

	@Override
	public IAESprite getIcon( final EnumFacing side, final IBlockState state )
	{
		if( this.type == CraftingUnitType.ACCELERATOR )
		{
			if( (boolean) state.getValue( FORMED ) )
			{
				return ExtraBlockTextures.BlockCraftingAcceleratorFit.getIcon();
			}

			return ExtraBlockTextures.BlockCraftingAccelerator.getIcon();
		}

		if( (boolean) state.getValue( FORMED ) )
		{
			return ExtraBlockTextures.BlockCraftingUnitFit.getIcon();
		}

		return super.getIcon( side, state );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void getCheckedSubBlocks( final Item item, final CreativeTabs tabs, final List<ItemStack> itemStacks )
	{
		itemStacks.add( new ItemStack( this, 1, 0 ) );
		itemStacks.add( new ItemStack( this, 1, 1 ) );
	}

	@Override
	public String getUnlocalizedName( final ItemStack is )
	{
		if( is.getItemDamage() == 1 )
		{
			return "tile.appliedenergistics2.BlockCraftingAccelerator";
		}

		return this.getItemUnlocalizedName( is );
	}

	public enum CraftingUnitType
	{
		UNIT, ACCELERATOR, STORAGE_1K, STORAGE_4K, STORAGE_16K, STORAGE_64K, MONITOR
	}
}
