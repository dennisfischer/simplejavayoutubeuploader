/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package org.chaosfisch.youtubeuploader.db.converter;

import org.chaosfisch.google.youtube.Comment;
import org.jooq.impl.EnumConverter;

public class CommentConverter extends EnumConverter<String, Comment> {

	private static final long serialVersionUID = -5791845905794108072L;

	public CommentConverter() {
		super(String.class, Comment.class);
	}
}
