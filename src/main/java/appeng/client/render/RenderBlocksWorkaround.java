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


import java.util.EnumSet;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


@SideOnly( Side.CLIENT )
public class RenderBlocksWorkaround extends BakingModelGenerator
{

	private boolean flipTexture;
	private EnumSet<EnumFacing> faces;
	private boolean useTextures;
	private EnumSet<EnumFacing> renderFaces;
	private float opacity;

	public void setTexture( final Object object )
	{
		// TODO Auto-generated method stub
	}

	public void setOpacity( final float f )
	{
		this.opacity = f;
	}

	public EnumSet<EnumFacing> getFaces()
	{
		return this.faces;
	}

	public void setFaces( final EnumSet<EnumFacing> faces )
	{
		this.faces = faces;
	}

}
