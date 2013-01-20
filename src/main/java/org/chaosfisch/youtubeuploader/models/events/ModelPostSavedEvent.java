package org.chaosfisch.youtubeuploader.models.events;

import org.javalite.activejdbc.Model;

public class ModelPostSavedEvent extends ModelEvent {

	public ModelPostSavedEvent(final Model model) {
		super(model);
	}
}
