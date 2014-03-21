/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.upload;

import de.chaosfisch.youtube.upload.metadata.Metadata;
import de.chaosfisch.youtube.upload.metadata.Monetization;
import de.chaosfisch.youtube.upload.metadata.Social;
import de.chaosfisch.youtube.upload.permissions.Permissions;
import javafx.beans.property.SimpleObjectProperty;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

public class UploadDTO {

	private String        id;
	private String        uploadurl;
	private String        videoid;
	private File          file;
	private File          enddir;
	private File          thumbnail;
	private LocalDateTime dateTimeOfStart;
	private LocalDateTime dateTimeOfRelease;
	private LocalDateTime dateTimeOfEnd;
	private int           order;
	private double        progress;
	private boolean       stopAfter;
	private long          fileSize;
	private Status        status;
	private String        accountId;
	private List<String>  playlistIds;


	private SimpleObjectProperty<Social>       social       = new SimpleObjectProperty<>(new Social());
	private SimpleObjectProperty<Monetization> monetization = new SimpleObjectProperty<>(new Monetization());
	private SimpleObjectProperty<Permissions>  permissions  = new SimpleObjectProperty<>(new Permissions());
	private SimpleObjectProperty<Metadata>     metadata     = new SimpleObjectProperty<>(new Metadata());

}
