/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.plugins.coreplugin.view;

import org.chaosfisch.youtubeuploader.designmanager.DesignMap;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.DesignImpl;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 29.02.12
 * Time: 18:11
 * To change this template use File | Settings | File Templates.
 */
public class JTattooLookAndFeels extends DesignMap
{
	public JTattooLookAndFeels()
	{
		super();

		this.add(new DesignImpl("com.jtattoo.plaf.acryl.AcrylLookAndFeel", "AcrylLookAndFeel", "Acryl")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.aero.AeroLookAndFeel", "AeroLookAndFeel", "Aero")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel", "AlumuniumLookAndFeel", "Aluminium")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel", "BernsteinLookAndFeel", "Bernstein")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.fast.FastLookAndFeel", "FastLookAndFeel", "Fast")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.graphite.GraphiteLookAndFeel", "GraphiteLookAndFeel", "Graphite")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.hifi.HiFiLookAndFeel", "HiFiLookAndFeel", "Hifi")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.luna.LunaLookAndFeel", "LunaLookAndFeel", "Luna")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.mint.MintLookAndFeel", "MintLookAndFeel", "Mint")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.noire.NoireLookAndFeel", "NoireLookAndFeel", "Noire")); //NON-NLS NON-NLS
		this.add(new DesignImpl("com.jtattoo.plaf.smart.SmartLookAndFeel", "SmartLookAndFeel", "Smart")); //NON-NLS NON-NLS
	}
}
