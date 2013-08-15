/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence;

import com.google.inject.Inject;
import de.chaosfisch.uploader.persistence.dao.ITemplateDao;
import de.chaosfisch.uploader.template.ITemplateService;
import de.chaosfisch.uploader.template.Template;

import java.util.List;

class TemplateServiceImpl implements ITemplateService {

	private final ITemplateDao templateDao;

	@Inject
	public TemplateServiceImpl(final ITemplateDao templateDao) {
		this.templateDao = templateDao;
	}

	@Override
	public List<Template> getAll() {
		return templateDao.getAll();
	}

	@Override
	public Template get(final String id) {
		return templateDao.get(id);
	}

	@Override
	public void insert(final Template template) {
		templateDao.insert(template);
	}

	@Override
	public void update(final Template template) {
		templateDao.update(template);
	}

	@Override
	public void delete(final Template template) {
		templateDao.delete(template);
	}
}
