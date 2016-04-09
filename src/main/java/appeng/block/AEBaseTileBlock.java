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

package appeng.block;


import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.implementations.tiles.IColorableTile;
import appeng.api.util.AEColor;
import appeng.api.util.IOrientable;
import appeng.block.networking.BlockCableBus;
import appeng.core.features.AEFeature;
import appeng.core.features.AETileBlockFeatureHandler;
import appeng.core.features.IAEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.storage.TileSkyChest;
import appeng.util.Platform;
import appeng.util.SettingsFrom;


public abstract class AEBaseTileBlock extends AEBaseBlock implements IAEFeature, ITileEntityProvider
{

	@Nonnull
	private Class<? extends TileEntity> tileEntityType;

	public AEBaseTileBlock( final Material mat )
	{
		super( mat );
	}

	protected AEBaseTileBlock( final Material mat, final Optional<String> subName )
	{
		super( mat, subName );
	}

	@Override
	protected void setFeature( final EnumSet<AEFeature> f )
	{
		final AETileBlockFeatureHandler featureHandler = new AETileBlockFeatureHandler( f, this, this.getFeatureSubName() );
		this.setHandler( featureHandler );
	}

	protected void setTileEntity( final Class<? extends TileEntity> c )
	{
		this.tileEntityType = c;
		this.setInventory( IInventory.class.isAssignableFrom( c ) );
		this.setTileProvider( this.hasBlockTileEntity() );
	}

	// update Block value.
	private void setTileProvider( final boolean b )
	{
		ReflectionHelper.setPrivateValue( Block.class, this, b, "isTileProvider" );
	}

	private boolean hasBlockTileEntity()
	{
		return this.tileEntityType != null;
	}

	public Class<? extends TileEntity> getTileEntityClass()
	{
		return this.tileEntityType;
	}

	@Nullable
	public <T extends AEBaseTile> T getTileEntity( final IBlockAccess w, final int x, final int y, final int z )
	{
		return this.getTileEntity( w, new BlockPos( x, y, z ) );
	}

	@Nullable
	public <T extends AEBaseTile> T getTileEntity( final IBlockAccess w, final BlockPos pos )
	{
		if( !this.hasBlockTileEntity() )
		{
			return null;
		}

		final TileEntity te = w.getTileEntity( pos );
		if( this.tileEntityType.isInstance( te ) )
		{
			return (T) te;
		}

		return null;
	}

	@Override
	public final TileEntity createNewTileEntity( final World var1, final int var2 )
	{
		if( this.hasBlockTileEntity() )
		{
			try
			{
				return this.tileEntityType.newInstance();
			}
			catch( final InstantiationException e )
			{
				throw new IllegalStateException( "Failed to create a new instance of an illegal class " + this.tileEntityType, e );
			}
			catch( final IllegalAccessException e )
			{
				throw new IllegalStateException( "Failed to create a new instance of " + this.tileEntityType + ", because lack of permissions", e );
			}
		}

		return null;
	}

	@Override
	public void breakBlock( final World w, final BlockPos pos, final IBlockState state )
	{
		final AEBaseTile te = this.getTileEntity( w, pos );
		if( te != null )
		{
			final ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
			if( te.dropItems() )
			{
				te.getDrops( w, pos, drops );
			}
			else
			{
				te.getNoDrops( w, pos, drops );
			}

			// Cry ;_; ...
			Platform.spawnDrops( w, pos, drops );
		}

		// super will remove the TE, as it is not an instance of BlockContainer
		super.breakBlock( w, pos, state );
	}

	@Override
	public final EnumFacing[] getValidRotations( final World w, final BlockPos pos )
	{
		final AEBaseTile obj = this.getTileEntity( w, pos );
		if( obj != null && obj.canBeRotated() )
		{
			return EnumFacing.VALUES;
		}

		return super.getValidRotations( w, pos );
	}

	@Override
	public boolean recolorBlock( final World world, final BlockPos pos, final EnumFacing side, final EnumDyeColor color )
	{
		final TileEntity te = this.getTileEntity( world, pos );

		if( te instanceof IColorableTile )
		{
			final IColorableTile ct = (IColorableTile) te;
			final AEColor c = ct.getColor();
			final AEColor newColor = AEColor.values()[color.getMetadata()];

			if( c != newColor )
			{
				ct.recolourBlock( side, newColor, null );
				return true;
			}
			return false;
		}

		return super.recolorBlock( world, pos, side, color );
	}

	@Override
	public int getComparatorInputOverride( final World w, final BlockPos pos )
	{
		final TileEntity te = this.getTileEntity( w, pos );
		if( te instanceof IInventory )
		{
			return Container.calcRedstoneFromInventory( (IInventory) te );
		}
		return 0;
	}

	@Override
	public boolean onBlockEventReceived( final World worldIn, final BlockPos pos, final IBlockState state, final int eventID, final int eventParam )
	{
		super.onBlockEventReceived( worldIn, pos, state, eventID, eventParam );
		final TileEntity tileentity = worldIn.getTileEntity( pos );
		return tileentity != null ? tileentity.receiveClientEvent( eventID, eventParam ) : false;
	}

	@Override
	public void onBlockPlacedBy( final World w, final BlockPos pos, final IBlockState state, final EntityLivingBase placer, final ItemStack is )
	{
		if( is.hasDisplayName() )
		{
			final TileEntity te = this.getTileEntity( w, pos );
			if( te instanceof AEBaseTile )
			{
				( (AEBaseTile) w.getTileEntity( pos ) ).setName( is.getDisplayName() );
			}
		}
	}

	@Override
	public boolean onBlockActivated( final World w, final BlockPos pos, final IBlockState state, final EntityPlayer player, final EnumFacing side, final float hitX, final float hitY, final float hitZ )
	{
		if( player != null )
		{
			final ItemStack is = player.inventory.getCurrentItem();
			if( is != null )
			{
				if( Platform.isWrench( player, is, pos ) && player.isSneaking() )
				{
					final Block id = w.getBlockState( pos ).getBlock();
					if( id != null )
					{
						final AEBaseTile tile = this.getTileEntity( w, pos );
						final ItemStack[] drops = Platform.getBlockDrops( w, pos );

						if( tile == null )
						{
							return false;
						}

						if( tile instanceof TileCableBus || tile instanceof TileSkyChest )
						{
							return false;
						}

						final ItemStack op = new ItemStack( this );
						for( final ItemStack ol : drops )
						{
							if( Platform.isSameItemType( ol, op ) )
							{
								final NBTTagCompound tag = tile.downloadSettings( SettingsFrom.DISMANTLE_ITEM );
								if( tag != null )
								{
									ol.setTagCompound( tag );
								}
							}
						}

						if( id.removedByPlayer( w, pos, player, false ) )
						{
							final List<ItemStack> l = Lists.newArrayList( drops );
							Platform.spawnDrops( w, pos, l );
							w.setBlockToAir( pos );
						}
					}
					return false;
				}

				if( is.getItem() instanceof IMemoryCard && !( this instanceof BlockCableBus ) )
				{
					final IMemoryCard memoryCard = (IMemoryCard) is.getItem();
					if( player.isSneaking() )
					{
						final AEBaseTile t = this.getTileEntity( w, pos );
						if( t != null )
						{
							final String name = this.getUnlocalizedName();
							final NBTTagCompound data = t.downloadSettings( SettingsFrom.MEMORY_CARD );
							if( data != null )
							{
								memoryCard.setMemoryCardContents( is, name, data );
								memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
								return true;
							}
						}
					}
					else
					{
						final String name = memoryCard.getSettingsName( is );
						final NBTTagCompound data = memoryCard.getData( is );
						if( this.getUnlocalizedName().equals( name ) )
						{
							final AEBaseTile t = this.getTileEntity( w, pos );
							t.uploadSettings( SettingsFrom.MEMORY_CARD, data );
							memoryCard.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
						}
						else
						{
							memoryCard.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
						}
						return false;
					}
				}
			}
		}

		return this.onActivated( w, pos, player, side, hitX, hitY, hitZ );
	}

	@Override
	public IOrientable getOrientable( final IBlockAccess w, final BlockPos pos )
	{
		return this.getTileEntity( w, pos );
	}

	@Override
	public ICustomCollision getCustomCollision( final World w, final BlockPos pos )
	{
		final AEBaseTile te = this.getTileEntity( w, pos );
		if( te instanceof ICustomCollision )
		{
			return (ICustomCollision) te;
		}

		return super.getCustomCollision( w, pos );
	}

}
