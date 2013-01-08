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

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

public class ByteLengthValidator extends ValidatorAdapter<Model>
{
	private final String	stringToValidate;
	private final int		min;
	private final int		max;

	public ByteLengthValidator(final String stringToValidate, final int min, final int max)
	{
		this.stringToValidate = stringToValidate;
		this.min = min;
		this.max = max;
	}

	@Override
	public void validate(final Model m)
	{
		if ((m.getString(stringToValidate) != null)
				&& ((m.getString(stringToValidate).getBytes().length < min) || (m.getString(stringToValidate).getBytes().length > max)))
		{
			m.addValidator(this, "byte_length_error");
		}
	}

}
