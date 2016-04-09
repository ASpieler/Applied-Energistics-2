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

package appeng.items.tools.powered;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Optional;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import appeng.api.util.DimensionalCoord;
import appeng.block.misc.BlockTinyTNT;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.hooks.DispenserBlockTool;
import appeng.hooks.IBlockTool;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.util.InWorldToolOperationResult;
import appeng.util.Platform;


public class ToolEntropyManipulator extends AEBasePoweredItem implements IBlockTool
{
	private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> heatUp;
	private final Map<InWorldToolOperationIngredient, InWorldToolOperationResult> coolDown;

	public ToolEntropyManipulator()
	{
		super( AEConfig.instance.entropyManipulatorBattery, Optional.<String>absent() );

		this.setFeature( EnumSet.of( AEFeature.EntropyManipulator, AEFeature.PoweredTools ) );

		this.heatUp = new HashMap<InWorldToolOperationIngredient, InWorldToolOperationResult>();
		this.coolDown = new HashMap<InWorldToolOperationIngredient, InWorldToolOperationResult>();

		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.stone.getDefaultState() ), new InWorldToolOperationResult( new ItemStack( Blocks.cobblestone ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.stonebrick.getDefaultState() ), new InWorldToolOperationResult( new ItemStack( Blocks.stonebrick, 1, 2 ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.lava, true ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.flowing_lava, true ), new InWorldToolOperationResult( new ItemStack( Blocks.obsidian ) ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.grass, true ), new InWorldToolOperationResult( new ItemStack( Blocks.dirt ) ) );

		final List<ItemStack> snowBalls = new ArrayList<ItemStack>();
		snowBalls.add( new ItemStack( Items.snowball ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.flowing_water, true ), new InWorldToolOperationResult( null, snowBalls ) );
		this.coolDown.put( new InWorldToolOperationIngredient( Blocks.water, true ), new InWorldToolOperationResult( new ItemStack( Blocks.ice ) ) );

		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.ice.getDefaultState() ), new InWorldToolOperationResult( new ItemStack( Blocks.water ) ) );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.flowing_water, true ), new InWorldToolOperationResult() );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.water, true ), new InWorldToolOperationResult() );
		this.heatUp.put( new InWorldToolOperationIngredient( Blocks.snow, true ), new InWorldToolOperationResult( new ItemStack( Blocks.flowing_water ) ) );
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( this, new DispenserBlockTool() );
	}

	private static class InWorldToolOperationIngredient
	{
		private final IBlockState state;
		private final boolean blockOnly;

		public InWorldToolOperationIngredient( final IBlockState state )
		{
			this.state = state;
			this.blockOnly = false;
		}

		public InWorldToolOperationIngredient( final Block blk, final boolean b )
		{
			this.state = blk.getDefaultState();
			this.blockOnly = b;
		}

		@Override
		public int hashCode()
		{
			return this.state.getBlock().hashCode();
		}

		@Override
		public boolean equals( final Object obj )
		{
			if( obj == null )
			{
				return false;
			}
			if( this.getClass() != obj.getClass() )
			{
				return false;
			}
			final InWorldToolOperationIngredient other = (InWorldToolOperationIngredient) obj;
			return this.state == other.state && ( this.blockOnly && this.state.getBlock() == other.state.getBlock() );
		}
	}

	private void heat( final IBlockState state, final World w, final BlockPos pos )
	{
		InWorldToolOperationResult r = this.heatUp.get( new InWorldToolOperationIngredient( state ) );

		if( r == null )
		{
			r = this.heatUp.get( new InWorldToolOperationIngredient( state.getBlock(), true ) );
		}

		if( r.getBlockItem() != null )
		{
			final Block blk = Block.getBlockFromItem( r.getBlockItem().getItem() );
			w.setBlockState( pos, blk.getStateFromMeta( r.getBlockItem().getItemDamage() ), 3 );
		}
		else
		{
			w.setBlockToAir( pos );
		}

		if( r.getDrops() != null )
		{
			Platform.spawnDrops( w, pos, r.getDrops() );
		}
	}

	private boolean canHeat( final IBlockState state )
	{
		InWorldToolOperationResult r = this.heatUp.get( new InWorldToolOperationIngredient( state ) );

		if( r == null )
		{
			r = this.heatUp.get( new InWorldToolOperationIngredient( state.getBlock(), true ) );
		}

		return r != null;
	}

	private void cool( final IBlockState state, final World w, final BlockPos pos )
	{
		InWorldToolOperationResult r = this.coolDown.get( new InWorldToolOperationIngredient( state ) );

		if( r == null )
		{
			r = this.coolDown.get( new InWorldToolOperationIngredient( state.getBlock(), true ) );
		}

		if( r.getBlockItem() != null )
		{
			final Block blk = Block.getBlockFromItem( r.getBlockItem().getItem() );
			w.setBlockState( pos, blk.getStateFromMeta( r.getBlockItem().getItemDamage() ), 3 );
		}
		else
		{
			w.setBlockToAir( pos );
		}

		if( r.getDrops() != null )
		{
			Platform.spawnDrops( w, pos, r.getDrops() );
		}
	}

	private boolean canCool( final IBlockState state )
	{
		InWorldToolOperationResult r = this.coolDown.get( new InWorldToolOperationIngredient( state ) );

		if( r == null )
		{
			r = this.coolDown.get( new InWorldToolOperationIngredient( state.getBlock(), true ) );
		}

		return r != null;
	}

	@Override
	public boolean hitEntity( final ItemStack item, final EntityLivingBase target, final EntityLivingBase hitter )
	{
		if( this.getAECurrentPower( item ) > 1600 )
		{
			this.extractAEPower( item, 1600 );
			target.setFire( 8 );
		}

		return false;
	}

	@Override
	public ItemStack onItemRightClick( final ItemStack item, final World w, final EntityPlayer p )
	{
		final MovingObjectPosition target = this.getMovingObjectPositionFromPlayer( w, p, true );

		if( target == null )
		{
			return item;
		}
		else
		{
			if( target.typeOfHit == MovingObjectType.BLOCK )
			{
				if( w.getBlockState( target.getBlockPos() ).getBlock().getMaterial() == Material.lava || w.getBlockState( target.getBlockPos() ).getBlock().getMaterial() == Material.water )
				{
					if( Platform.hasPermissions( new DimensionalCoord( w, target.getBlockPos() ), p ) )
					{
						this.onItemUse( item, p, w, target.getBlockPos(), EnumFacing.UP, 0.0F, 0.0F, 0.0F );
					}
				}
			}
		}

		return item;
	}

	@Override
	public boolean onItemUse( final ItemStack item, final EntityPlayer p, final World w, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( this.getAECurrentPower( item ) > 1600 )
		{
			if( !p.canPlayerEdit( pos, side, item ) )
			{
				return false;
			}

			final IBlockState state = w.getBlockState( pos );
			final Block blockID = state.getBlock();

			if( p.isSneaking() )
			{
				if( this.canCool( state ) )
				{
					this.extractAEPower( item, 1600 );
					this.cool( state, w, pos );
					return true;
				}
			}
			else
			{
				if( blockID instanceof BlockTNT )
				{
					w.setBlockToAir( pos );
					( (BlockTNT) blockID ).explode( w, pos, state, p );
					return true;
				}

				if( blockID instanceof BlockTinyTNT )
				{
					w.setBlockToAir( pos );
					( (BlockTinyTNT) blockID ).startFuse( w, pos, p );
					return true;
				}

				if( this.canHeat( state ) )
				{
					this.extractAEPower( item, 1600 );
					this.heat( state, w, pos );
					return true;
				}

				final ItemStack[] stack = Platform.getBlockDrops( w, pos );
				final List<ItemStack> out = new ArrayList<ItemStack>();
				boolean hasFurnaceable = false;
				boolean canFurnaceable = true;

				for( final ItemStack i : stack )
				{
					final ItemStack result = FurnaceRecipes.instance().getSmeltingResult( i );

					if( result != null )
					{
						if( result.getItem() instanceof ItemBlock )
						{
							if( Block.getBlockFromItem( result.getItem() ) == blockID && result.getItem().getDamage( result ) == blockID.getMetaFromState( state ) )
							{
								canFurnaceable = false;
							}
						}
						hasFurnaceable = true;
						out.add( result );
					}
					else
					{
						canFurnaceable = false;
						out.add( i );
					}
				}

				if( hasFurnaceable && canFurnaceable )
				{
					this.extractAEPower( item, 1600 );
					final InWorldToolOperationResult or = InWorldToolOperationResult.getBlockOperationResult( out.toArray( new ItemStack[out.size()] ) );
					w.playSoundEffect( pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );

					if( or.getBlockItem() == null )
					{
						w.setBlockState( pos, Platform.AIR_BLOCK.getDefaultState(), 3 );
					}
					else
					{
						final Block blk = Block.getBlockFromItem( or.getBlockItem().getItem() );
						w.setBlockState( pos, blk.getStateFromMeta( or.getBlockItem().getItemDamage() ), 3 );
					}

					if( or.getDrops() != null )
					{
						Platform.spawnDrops( w, pos, or.getDrops() );
					}

					return true;
				}
				else
				{
					final BlockPos offsetPos = pos.offset( side );

					if( !p.canPlayerEdit( offsetPos, side, item ) )
					{
						return false;
					}

					if( w.isAirBlock( offsetPos ) )
					{
						this.extractAEPower( item, 1600 );
						w.playSoundEffect( offsetPos.getX() + 0.5D, offsetPos.getY() + 0.5D, offsetPos.getZ() + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F );
						w.setBlockState( offsetPos, Blocks.fire.getDefaultState() );
					}

					return true;
				}
			}
		}

		return false;
	}
}
