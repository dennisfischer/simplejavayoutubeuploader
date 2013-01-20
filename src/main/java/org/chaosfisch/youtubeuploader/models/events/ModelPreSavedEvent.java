package org.chaosfisch.youtubeuploader.models.events;

import org.javalite.activejdbc.Model;

public class ModelPreSavedEvent extends ModelEvent {

	public ModelPreSavedEvent(final Model model) {
		super(model);
	}
}
