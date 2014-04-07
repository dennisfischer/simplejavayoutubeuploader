/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.playlist;

import de.chaosfisch.youtube.Event;

public class PlaylistRemovedEvent implements Event {

	private final PlaylistModel playlistModel;

	public PlaylistRemovedEvent(final PlaylistModel playlistModel) {this.playlistModel = playlistModel;}

	public PlaylistModel getPlaylistModel() {
		return playlistModel;
	}
}
