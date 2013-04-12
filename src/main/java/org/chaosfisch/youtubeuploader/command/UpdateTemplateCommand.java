/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.command;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.chaosfisch.youtubeuploader.db.dao.TemplateDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Template;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;

public class UpdateTemplateCommand extends Service<Void> {

	@Inject
	private TemplateDao	templateDao;

	public Template		template;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Preconditions.checkNotNull(template);
				templateDao.update(template);
				return null;
			}
		};
	}
}
