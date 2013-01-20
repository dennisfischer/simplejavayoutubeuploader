package org.chaosfisch.youtubeuploader.models.events;

import org.javalite.activejdbc.Model;

public class ModelPostRemovedEvent extends ModelEvent {

	public ModelPostRemovedEvent(final Model model) {
		super(model);
	}
}
