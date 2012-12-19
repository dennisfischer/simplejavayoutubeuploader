package org.chaosfisch.youtubeuploader.models.validation;

import org.chaosfisch.util.TagParser;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.validation.ValidatorAdapter;

public class TagValidator extends ValidatorAdapter<Model>
{
	@Override
	public void validate(final Model m)
	{
		if ((m.getString("keywords") != null) && !TagParser.isValid(m.getString("keywords")))
		{
			m.addValidator(this, "tag_error");
		}
	}
}
