/*
 * Copyright (c) 2014 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.template.events;

import de.chaosfisch.uploader.template.Template;

public class TemplateRemoved {
	private final Template template;

	public TemplateRemoved(final Template template) {
		this.template = template;
	}

	public Template getTemplate() {
		return template;
	}
}
