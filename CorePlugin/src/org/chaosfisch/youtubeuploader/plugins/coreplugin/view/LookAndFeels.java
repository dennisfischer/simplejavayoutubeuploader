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

import com.jtattoo.plaf.acryl.AcrylLookAndFeel;
import com.jtattoo.plaf.aero.AeroLookAndFeel;
import com.jtattoo.plaf.aluminium.AluminiumLookAndFeel;
import com.jtattoo.plaf.bernstein.BernsteinLookAndFeel;
import com.jtattoo.plaf.fast.FastLookAndFeel;
import com.jtattoo.plaf.graphite.GraphiteLookAndFeel;
import com.jtattoo.plaf.hifi.HiFiLookAndFeel;
import com.jtattoo.plaf.luna.LunaLookAndFeel;
import com.jtattoo.plaf.mint.MintLookAndFeel;
import com.jtattoo.plaf.noire.NoireLookAndFeel;
import com.jtattoo.plaf.smart.SmartLookAndFeel;
import org.chaosfisch.youtubeuploader.designmanager.spi.DesignMap;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.DesignImpl;
import org.pushingpixels.substance.api.skin.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 01.03.12
 * Time: 22:21
 * To change this template use File | Settings | File Templates.
 */
public class LookAndFeels extends DesignMap
{
	public LookAndFeels()
	{
		super();

		this.add(new DesignImpl(SubstanceAutumnLookAndFeel.class, "SubstanceAutumnLookAndFeel", "Substance Autumn")); //NON-NLS
		this.add(new DesignImpl(SubstanceBusinessBlackSteelLookAndFeel.class, "SubstanceBusinessBlackSteelLookAndFeel", "Substance Buisness Black Steel")); //NON-NLS
		this.add(new DesignImpl(SubstanceBusinessBlueSteelLookAndFeel.class, "SubstanceBusinessBlueSteelLookAndFeel", "Substance Buisness Blue Steel")); //NON-NLS
		this.add(new DesignImpl(SubstanceBusinessLookAndFeel.class, "SubstanceBusinessLookAndFeel", "Substance Buisness")); //NON-NLS
		this.add(new DesignImpl(SubstanceChallengerDeepLookAndFeel.class, "SubstanceChallengerDeepLookAndFeel", "Substance Challenger Deep")); //NON-NLS
		this.add(new DesignImpl(SubstanceCremeCoffeeLookAndFeel.class, "SubstanceCremeCoffeeLookAndFeel", "Substance Creme Coffee")); //NON-NLS
		this.add(new DesignImpl(SubstanceCremeLookAndFeel.class, "SubstanceCremeLookAndFeel", "Substance Creme")); //NON-NLS
		this.add(new DesignImpl(SubstanceDustCoffeeLookAndFeel.class, "SubstanceDustCoffeeLookAndFeel", "Substance Dust Coffee")); //NON-NLS
		this.add(new DesignImpl(SubstanceDustLookAndFeel.class, "SubstanceDustLookAndFeel", "Substance Dust")); //NON-NLS
		this.add(new DesignImpl(SubstanceEmeraldDuskLookAndFeel.class, "SubstanceEmeraldDuskLookAndFeel", "Substance Emerald Dusk")); //NON-NLS
		this.add(new DesignImpl(SubstanceGeminiLookAndFeel.class, "SubstanceGeminiLookAndFeel", "Substance Gemini")); //NON-NLS
		this.add(new DesignImpl(SubstanceGraphiteAquaLookAndFeel.class, "SubstanceGraphiteAquaLookAndFeel", "Substance Graphite Aqua")); //NON-NLS
		this.add(new DesignImpl(SubstanceGraphiteGlassLookAndFeel.class, "SubstanceGraphiteGlassLookAndFeel", "Substance Graphite Glass")); //NON-NLS
		this.add(new DesignImpl(SubstanceGraphiteLookAndFeel.class, "SubstanceGraphiteLookAndFeel", "Substance Graphite")); //NON-NLS
		this.add(new DesignImpl(SubstanceMagellanLookAndFeel.class, "SubstanceMagellanLookAndFeel", "Substance Magellan")); //NON-NLS
		this.add(new DesignImpl(SubstanceMarinerLookAndFeel.class, "SubstanceMarinerLookAndFeel", "Substance Mariner")); //NON-NLS
		this.add(new DesignImpl(SubstanceMistAquaLookAndFeel.class, "SubstanceMistAquaLookAndFeel", "Substance Mist Aqua")); //NON-NLS
		this.add(new DesignImpl(SubstanceMistSilverLookAndFeel.class, "SubstanceMistSilverLookAndFeel", "Substance Mist Silver")); //NON-NLS
		this.add(new DesignImpl(SubstanceModerateLookAndFeel.class, "SubstanceModerateLookAndFeel", "Substance Moderate")); //NON-NLS
		this.add(new DesignImpl(SubstanceNebulaBrickWallLookAndFeel.class, "SubstanceNebulaBrickWallLookAndFeel", "Substance Nebula Brick")); //NON-NLS
		this.add(new DesignImpl(SubstanceNebulaLookAndFeel.class, "SubstanceNebulaLookAndFeel", "Substance Nebula")); //NON-NLS
		this.add(new DesignImpl(SubstanceOfficeBlack2007LookAndFeel.class, "SubstanceOfficeBlack2007LookAndFeel", "Substance Office Black 2007")); //NON-NLS
		this.add(new DesignImpl(SubstanceOfficeBlue2007LookAndFeel.class, "SubstanceOfficeBlue2007LookAndFeel", "Substance Office Blue 2007")); //NON-NLS
		this.add(new DesignImpl(SubstanceOfficeSilver2007LookAndFeel.class, "SubstanceOfficeSilver2007LookAndFeel", "Substance Office Silver 2007")); //NON-NLS
		this.add(new DesignImpl(SubstanceRavenLookAndFeel.class, "SubstanceRavenLookAndFeel", "Substance Raven")); //NON-NLS
		this.add(new DesignImpl(SubstanceSaharaLookAndFeel.class, "SubstanceSaharaLookAndFeel", "Substance Sahara")); //NON-NLS
		this.add(new DesignImpl(SubstanceTwilightLookAndFeel.class, "SubstanceTwilightLookAndFeel", "Substance Twilight")); //NON-NLS

		this.add(new DesignImpl(AcrylLookAndFeel.class, "AcrylLookAndFeel", "Acryl")); //NON-NLS NON-NLS
		this.add(new DesignImpl(AeroLookAndFeel.class, "AeroLookAndFeel", "Aero")); //NON-NLS NON-NLS
		this.add(new DesignImpl(AluminiumLookAndFeel.class, "AlumuniumLookAndFeel", "Aluminium")); //NON-NLS NON-NLS
		this.add(new DesignImpl(BernsteinLookAndFeel.class, "BernsteinLookAndFeel", "Bernstein")); //NON-NLS NON-NLS
		this.add(new DesignImpl(FastLookAndFeel.class, "FastLookAndFeel", "Fast")); //NON-NLS NON-NLS
		this.add(new DesignImpl(GraphiteLookAndFeel.class, "GraphiteLookAndFeel", "Graphite")); //NON-NLS NON-NLS
		this.add(new DesignImpl(HiFiLookAndFeel.class, "HiFiLookAndFeel", "Hifi")); //NON-NLS NON-NLS
		this.add(new DesignImpl(LunaLookAndFeel.class, "LunaLookAndFeel", "Luna")); //NON-NLS NON-NLS
		this.add(new DesignImpl(MintLookAndFeel.class, "MintLookAndFeel", "Mint")); //NON-NLS NON-NLS
		this.add(new DesignImpl(NoireLookAndFeel.class, "NoireLookAndFeel", "Noire")); //NON-NLS NON-NLS
		this.add(new DesignImpl(SmartLookAndFeel.class, "SmartLookAndFeel", "Smart")); //NON-NLS NON-NLS
	}
}                                                                       