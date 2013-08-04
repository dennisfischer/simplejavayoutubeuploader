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

import com.google.inject.Inject;
import de.chaosfisch.uploader.template.Template;

import javax.persistence.EntityManager;
import java.util.List;

public class TemplateDaoImpl implements ITemplateDao {

	@Inject
	protected EntityManager entityManager;

	@Override
	public List<Template> getAll() {
		return (List<Template>) entityManager.createQuery("SELECT t FROM template t").getResultList();
	}

	@Override
	public Template get(final int id) {
		return entityManager.find(Template.class, id);
	}

	@Override
	public void insert(final Template template) {
		entityManager.persist(template);
	}

	@Override
	public void update(final Template template) {
		entityManager.persist(template);
	}

	@Override
	public void delete(final Template template) {
		entityManager.remove(template);
	}
}
