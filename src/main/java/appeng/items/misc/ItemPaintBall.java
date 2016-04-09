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

package appeng.items.misc;


import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.util.AEColor;
import appeng.client.ClientHelper;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;


public class ItemPaintBall extends AEBaseItem
{

	private static final int DAMAGE_THRESHOLD = 20;

	public ItemPaintBall()
	{
		this.setFeature( EnumSet.of( AEFeature.PaintBalls ) );
		this.setHasSubtypes( true );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void registerIcons( final ClientHelper ir, final String name )
	{
		final ModelResourceLocation sloc = ir.setIcon( this, name + "Shimmer" );
		final ModelResourceLocation loc = ir.setIcon( this, name );

		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( this, new ItemMeshDefinition(){

			@Override
			public ModelResourceLocation getModelLocation( final ItemStack stack )
			{
				if( ItemPaintBall.this.isLumen( stack ) )
				{
					return sloc;
				}

				return loc;
			}
		} );
	}

	@Override
	public String getItemStackDisplayName( final ItemStack is )
	{
		return super.getItemStackDisplayName( is ) + " - " + this.getExtraName( is );
	}

	private String getExtraName( final ItemStack is )
	{
		return ( is.getItemDamage() >= DAMAGE_THRESHOLD ? GuiText.Lumen.getLocal() + ' ' : "" ) + this.getColor( is );
	}

	@Override
	public int getColorFromItemStack( final ItemStack stack, final int renderPass )
	{
		final AEColor col = this.getColor( stack );

		final int colorValue = stack.getItemDamage() >= 20 ? col.mediumVariant : col.mediumVariant;
		final int r = ( colorValue >> 16 ) & 0xff;
		final int g = ( colorValue >> 8 ) & 0xff;
		final int b = ( colorValue ) & 0xff;

		if( stack.getItemDamage() >= 20 )
		{
			final float fail = 0.7f;
			final int full = (int) ( 255 * 0.3 );
			return (int) ( full + r * fail ) << 16 | (int) ( full + g * fail ) << 8 | (int) ( full + b * fail ) | 0xff << 24;
		}
		else
		{
			return r << 16 | g << 8 | b | 0xff << 24;
		}
	}

	public AEColor getColor( final ItemStack is )
	{
		int dmg = is.getItemDamage();
		if( dmg >= DAMAGE_THRESHOLD )
		{
			dmg -= DAMAGE_THRESHOLD;
		}

		if( dmg >= AEColor.values().length )
		{
			return AEColor.Transparent;
		}

		return AEColor.values()[dmg];
	}

	@Override
	protected void getCheckedSubItems( final Item sameItem, final CreativeTabs creativeTab, final List<ItemStack> itemStacks )
	{
		for( final AEColor c : AEColor.values() )
		{
			if( c != AEColor.Transparent )
			{
				itemStacks.add( new ItemStack( this, 1, c.ordinal() ) );
			}
		}

		for( final AEColor c : AEColor.values() )
		{
			if( c != AEColor.Transparent )
			{
				itemStacks.add( new ItemStack( this, 1, DAMAGE_THRESHOLD + c.ordinal() ) );
			}
		}
	}

	public boolean isLumen( final ItemStack is )
	{
		final int dmg = is.getItemDamage();
		return dmg >= DAMAGE_THRESHOLD;
	}
}
