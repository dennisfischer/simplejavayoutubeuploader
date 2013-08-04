/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.persistence.dao;

import de.chaosfisch.uploader.template.Template;

import java.util.List;

public interface ITemplateDao {
	List<Template> getAll();

	Template get(int id);

	void insert(Template template);

	void update(Template template);

	void delete(Template template);
}
