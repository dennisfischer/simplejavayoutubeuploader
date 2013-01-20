package org.chaosfisch.youtubeuploader.models.events;

import org.javalite.activejdbc.Model;

public class ModelPreRemovedEvent extends ModelEvent {

	public ModelPreRemovedEvent(final Model model) {
		super(model);
	}
}
