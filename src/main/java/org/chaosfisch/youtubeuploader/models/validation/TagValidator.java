/*******************************************************************************
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors: Dennis Fischer
 ******************************************************************************/
package org.chaosfisch.youtubeuploader.models.validation;

import org.chaosfisch.util.TagParser;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

public class TagValidator extends ValidatorAdapter<Model> {
	@Override public void validate(final Model m) {
		if ((m.getString("keywords") != null) && !TagParser.isValid(m.getString("keywords"))) {
			m.addValidator(this, "tag_error");
		}
	}
}
