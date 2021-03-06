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

package appeng.block.misc;


import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityRegistry;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.entity.EntityIds;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.helpers.ICustomCollision;
import appeng.hooks.DispenserBehaviorTinyTNT;


public class BlockTinyTNT extends AEBaseBlock implements ICustomCollision
{

	public BlockTinyTNT()
	{
		super( Material.tnt );
		this.setLightOpacity( 1 );
		this.setBlockBounds( 0.25f, 0.0f, 0.25f, 0.75f, 0.5f, 0.75f );
		this.setFullSize( this.setOpaque( false ) );
		this.setStepSound( soundTypeGrass );
		this.setHardness( 0F );
		this.setFeature( EnumSet.of( AEFeature.TinyTNT ) );

		EntityRegistry.registerModEntity( EntityTinyTNTPrimed.class, "EntityTinyTNTPrimed", EntityIds.get( EntityTinyTNTPrimed.class ), AppEng.instance(), 16, 4, true );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return null;
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( Item.getItemFromBlock( this ), new DispenserBehaviorTinyTNT() );
	}

	@Override
	public boolean onActivated( final World w, final BlockPos pos, final EntityPlayer player, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.flint_and_steel )
		{
			this.startFuse( w, pos, player );
			w.setBlockToAir( pos );
			player.getCurrentEquippedItem().damageItem( 1, player );
			return true;
		}
		else
		{
			return super.onActivated( w, pos, player, side, hitX, hitY, hitZ );
		}
	}

	public void startFuse( final World w, final BlockPos pos, final EntityLivingBase igniter )
	{
		if( !w.isRemote )
		{
			final EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed( w, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, igniter );
			w.spawnEntityInWorld( primedTinyTNTEntity );
			w.playSoundAtEntity( primedTinyTNTEntity, "game.tnt.primed", 1.0F, 1.0F );
		}
	}

	@Override
	public void onNeighborBlockChange( final World w, final BlockPos pos, final IBlockState state, final Block neighborBlock )
	{
		if( w.isBlockIndirectlyGettingPowered( pos ) > 0 )
		{
			this.startFuse( w, pos, null );
			w.setBlockToAir( pos );
		}
	}

	@Override
	public void onBlockAdded( final World w, final BlockPos pos, final IBlockState state )
	{
		super.onBlockAdded( w, pos, state );

		if( w.isBlockIndirectlyGettingPowered( pos ) > 0 )
		{
			this.startFuse( w, pos, null );
			w.setBlockToAir( pos );
		}
	}

	@Override
	public void onEntityCollidedWithBlock( final World w, final BlockPos pos, final Entity entity )
	{
		if( entity instanceof EntityArrow && !w.isRemote )
		{
			final EntityArrow entityarrow = (EntityArrow) entity;

			if( entityarrow.isBurning() )
			{
				this.startFuse( w, pos, entityarrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) entityarrow.shootingEntity : null );
				w.setBlockToAir( pos );
			}
		}
	}

	@Override
	public boolean canDropFromExplosion( final Explosion exp )
	{
		return false;
	}

	@Override
	public void onBlockExploded( final World w, final BlockPos pos, final Explosion exp )
	{
		if( !w.isRemote )
		{
			final EntityTinyTNTPrimed primedTinyTNTEntity = new EntityTinyTNTPrimed( w, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, exp.getExplosivePlacedBy() );
			primedTinyTNTEntity.fuse = w.rand.nextInt( primedTinyTNTEntity.fuse / 4 ) + primedTinyTNTEntity.fuse / 8;
			w.spawnEntityInWorld( primedTinyTNTEntity );
		}
	}

	@Override
	public Iterable<AxisAlignedBB> getSelectedBoundingBoxesFromPool( final World w, final BlockPos pos, final Entity thePlayer, final boolean b )
	{
		return Collections.singletonList( AxisAlignedBB.fromBounds( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) );
	}

	@Override
	public void addCollidingBlockToList( final World w, final BlockPos pos, final AxisAlignedBB bb, final List<AxisAlignedBB> out, final Entity e )
	{
		out.add( AxisAlignedBB.fromBounds( 0.25, 0, 0.25, 0.75, 0.5, 0.75 ) );
	}
}
