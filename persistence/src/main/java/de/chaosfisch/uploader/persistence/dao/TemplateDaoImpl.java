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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import de.chaosfisch.uploader.persistence.dao.transactional.Transactional;
import de.chaosfisch.uploader.template.Template;
import de.chaosfisch.uploader.template.events.TemplateAdded;
import de.chaosfisch.uploader.template.events.TemplateRemoved;
import de.chaosfisch.uploader.template.events.TemplateUpdated;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class TemplateDaoImpl implements ITemplateDao {

	protected ArrayList<Template> templates = new ArrayList<>(10);

	@Inject
	protected EntityManager entityManager;
	@Inject
	protected EventBus      eventBus;

	@Override
	public List<Template> getAll() {
		final List<Template> result = entityManager.createQuery("SELECT t FROM template t", Template.class)
				.getResultList();

		for (final Template template : result) {
			addOrUpdateTemplate(template);
		}
		return templates;
	}

	@Override
	public Template get(final int id) {
		final Template template = entityManager.find(Template.class, id);
		addOrUpdateTemplate(template);
		return getFromTemplateList(template);
	}

	@Override
	@Transactional
	public void insert(final Template template) {
		entityManager.persist(template);
		templates.add(template);
		eventBus.post(new TemplateAdded(template));
	}

	@Override
	@Transactional
	public void update(final Template template) {
		entityManager.merge(template);
		eventBus.post(new TemplateUpdated(template));
	}

	@Override
	@Transactional
	public void delete(final Template template) {
		entityManager.remove(template);
		templates.remove(template);
		eventBus.post(new TemplateRemoved(template));
	}

	private void addOrUpdateTemplate(final Template template) {
		if (templates.contains(template)) {
			refreshTemplate(template);
		} else {
			templates.add(template);
		}
	}

	private void refreshTemplate(final Template template) {
		entityManager.refresh(getFromTemplateList(template));
	}

	private Template getFromTemplateList(final Template template) {
		return templates.get(templates.indexOf(template));
	}
}
