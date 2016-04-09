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

package appeng.client.render;


import appeng.api.util.AEPartLocation;
import appeng.api.util.IAESprite;
import appeng.client.texture.FlippableIcon;
import appeng.client.texture.TmpFlippableIcon;


public class BlockRenderInfo
{

	private final BaseBlockRender rendererInstance;
	private final TmpFlippableIcon tmpTopIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpBottomIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpSouthIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpNorthIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpEastIcon = new TmpFlippableIcon();
	private final TmpFlippableIcon tmpWestIcon = new TmpFlippableIcon();
	private boolean useTmp = false;
	private FlippableIcon topIcon = null;
	private FlippableIcon bottomIcon = null;
	private FlippableIcon southIcon = null;
	private FlippableIcon northIcon = null;
	private FlippableIcon eastIcon = null;
	private FlippableIcon westIcon = null;

	public BlockRenderInfo( final BaseBlockRender inst )
	{
		this.rendererInstance = inst;
	}

	public void updateIcons( final FlippableIcon bottom, final FlippableIcon top, final FlippableIcon north, final FlippableIcon south, final FlippableIcon east, final FlippableIcon west )
	{
		this.topIcon = top;
		this.bottomIcon = bottom;
		this.southIcon = south;
		this.northIcon = north;
		this.eastIcon = east;
		this.westIcon = west;
	}

	public void setTemporaryRenderIcon( final IAESprite icon )
	{
		if( icon == null )
		{
			this.useTmp = false;
		}
		else
		{
			this.useTmp = true;
			this.tmpTopIcon.setOriginal( icon );
			this.tmpBottomIcon.setOriginal( icon );
			this.tmpSouthIcon.setOriginal( icon );
			this.tmpNorthIcon.setOriginal( icon );
			this.tmpEastIcon.setOriginal( icon );
			this.tmpWestIcon.setOriginal( icon );
		}
	}

	public void setTemporaryRenderIcons( final IAESprite nTopIcon, final IAESprite nBottomIcon, final IAESprite nSouthIcon, final IAESprite nNorthIcon, final IAESprite nEastIcon, final IAESprite nWestIcon )
	{
		this.tmpTopIcon.setOriginal( nTopIcon == null ? this.getTexture( AEPartLocation.UP ) : nTopIcon );
		this.tmpBottomIcon.setOriginal( nBottomIcon == null ? this.getTexture( AEPartLocation.DOWN ) : nBottomIcon );
		this.tmpSouthIcon.setOriginal( nSouthIcon == null ? this.getTexture( AEPartLocation.SOUTH ) : nSouthIcon );
		this.tmpNorthIcon.setOriginal( nNorthIcon == null ? this.getTexture( AEPartLocation.NORTH ) : nNorthIcon );
		this.tmpEastIcon.setOriginal( nEastIcon == null ? this.getTexture( AEPartLocation.EAST ) : nEastIcon );
		this.tmpWestIcon.setOriginal( nWestIcon == null ? this.getTexture( AEPartLocation.WEST ) : nWestIcon );
		this.useTmp = true;
	}

	public FlippableIcon getTexture( final AEPartLocation dir )
	{
		if( this.useTmp )
		{
			switch( dir )
			{
				case DOWN:
					return this.tmpBottomIcon;
				case UP:
					return this.tmpTopIcon;
				case NORTH:
					return this.tmpNorthIcon;
				case SOUTH:
					return this.tmpSouthIcon;
				case EAST:
					return this.tmpEastIcon;
				case WEST:
					return this.tmpWestIcon;
				default:
					break;
			}
		}

		switch( dir )
		{
			case DOWN:
				return this.bottomIcon;
			case UP:
				return this.topIcon;
			case NORTH:
				return this.northIcon;
			case SOUTH:
				return this.southIcon;
			case EAST:
				return this.eastIcon;
			case WEST:
				return this.westIcon;
			default:
				break;
		}

		return this.topIcon;
	}

	boolean isValid()
	{
		return this.topIcon != null && this.bottomIcon != null && this.southIcon != null && this.northIcon != null && this.eastIcon != null && this.westIcon != null;
	}

	public BaseBlockRender getRendererInstance()
	{
		return this.rendererInstance;
	}
}
