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

import org.chaosfisch.youtubeuploader.designmanager.spi.DesignMap;
import org.chaosfisch.youtubeuploader.plugins.coreplugin.util.DesignImpl;
import org.pushingpixels.substance.api.skin.*;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 01.03.12
 * Time: 22:21
 * To change this template use File | Settings | File Templates.
 */
public class LookAndFeels extends DesignMap
{
	private static final long serialVersionUID = 2119730730764578776L;

	public LookAndFeels()
	{
		try {
			//noinspection unchecked
			add(new DesignImpl((Class<? extends LookAndFeel>) Class.forName(UIManager.getSystemLookAndFeelClassName()), "System-Default", "System default")); //NON-NLS
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("LaF class couldn't be found.", e);
		}
		add(new DesignImpl(SubstanceAutumnLookAndFeel.class, "SubstanceAutumnLookAndFeel", "Substance Autumn")); //NON-NLS
		add(new DesignImpl(SubstanceBusinessBlackSteelLookAndFeel.class, "SubstanceBusinessBlackSteelLookAndFeel", "Substance Buisness Black Steel")); //NON-NLS
		add(new DesignImpl(SubstanceBusinessBlueSteelLookAndFeel.class, "SubstanceBusinessBlueSteelLookAndFeel", "Substance Buisness Blue Steel")); //NON-NLS
		add(new DesignImpl(SubstanceBusinessLookAndFeel.class, "SubstanceBusinessLookAndFeel", "Substance Buisness")); //NON-NLS
		add(new DesignImpl(SubstanceChallengerDeepLookAndFeel.class, "SubstanceChallengerDeepLookAndFeel", "Substance Challenger Deep")); //NON-NLS
		add(new DesignImpl(SubstanceCremeCoffeeLookAndFeel.class, "SubstanceCremeCoffeeLookAndFeel", "Substance Creme Coffee")); //NON-NLS
		add(new DesignImpl(SubstanceCremeLookAndFeel.class, "SubstanceCremeLookAndFeel", "Substance Creme")); //NON-NLS
		add(new DesignImpl(SubstanceDustCoffeeLookAndFeel.class, "SubstanceDustCoffeeLookAndFeel", "Substance Dust Coffee")); //NON-NLS
		add(new DesignImpl(SubstanceDustLookAndFeel.class, "SubstanceDustLookAndFeel", "Substance Dust")); //NON-NLS
		add(new DesignImpl(SubstanceEmeraldDuskLookAndFeel.class, "SubstanceEmeraldDuskLookAndFeel", "Substance Emerald Dusk")); //NON-NLS
		add(new DesignImpl(SubstanceGeminiLookAndFeel.class, "SubstanceGeminiLookAndFeel", "Substance Gemini")); //NON-NLS
		add(new DesignImpl(SubstanceGraphiteAquaLookAndFeel.class, "SubstanceGraphiteAquaLookAndFeel", "Substance Graphite Aqua")); //NON-NLS
		add(new DesignImpl(SubstanceGraphiteGlassLookAndFeel.class, "SubstanceGraphiteGlassLookAndFeel", "Substance Graphite Glass")); //NON-NLS
		add(new DesignImpl(SubstanceGraphiteLookAndFeel.class, "SubstanceGraphiteLookAndFeel", "Substance Graphite")); //NON-NLS
		add(new DesignImpl(SubstanceMagellanLookAndFeel.class, "SubstanceMagellanLookAndFeel", "Substance Magellan")); //NON-NLS
		add(new DesignImpl(SubstanceMarinerLookAndFeel.class, "SubstanceMarinerLookAndFeel", "Substance Mariner")); //NON-NLS
		add(new DesignImpl(SubstanceMistAquaLookAndFeel.class, "SubstanceMistAquaLookAndFeel", "Substance Mist Aqua")); //NON-NLS
		add(new DesignImpl(SubstanceMistSilverLookAndFeel.class, "SubstanceMistSilverLookAndFeel", "Substance Mist Silver")); //NON-NLS
		add(new DesignImpl(SubstanceModerateLookAndFeel.class, "SubstanceModerateLookAndFeel", "Substance Moderate")); //NON-NLS
		add(new DesignImpl(SubstanceNebulaBrickWallLookAndFeel.class, "SubstanceNebulaBrickWallLookAndFeel", "Substance Nebula Brick")); //NON-NLS
		add(new DesignImpl(SubstanceNebulaLookAndFeel.class, "SubstanceNebulaLookAndFeel", "Substance Nebula")); //NON-NLS
		add(new DesignImpl(SubstanceOfficeBlack2007LookAndFeel.class, "SubstanceOfficeBlack2007LookAndFeel", "Substance Office Black 2007")); //NON-NLS
		add(new DesignImpl(SubstanceOfficeBlue2007LookAndFeel.class, "SubstanceOfficeBlue2007LookAndFeel", "Substance Office Blue 2007")); //NON-NLS
		add(new DesignImpl(SubstanceOfficeSilver2007LookAndFeel.class, "SubstanceOfficeSilver2007LookAndFeel", "Substance Office Silver 2007")); //NON-NLS
		add(new DesignImpl(SubstanceRavenLookAndFeel.class, "SubstanceRavenLookAndFeel", "Substance Raven")); //NON-NLS
		add(new DesignImpl(SubstanceSaharaLookAndFeel.class, "SubstanceSaharaLookAndFeel", "Substance Sahara")); //NON-NLS
		add(new DesignImpl(SubstanceTwilightLookAndFeel.class, "SubstanceTwilightLookAndFeel", "Substance Twilight")); //NON-NLS
	}
}                                                                       