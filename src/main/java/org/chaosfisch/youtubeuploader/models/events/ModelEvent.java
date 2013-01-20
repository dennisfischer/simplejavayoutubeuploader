package org.chaosfisch.youtubeuploader.models.events;

import org.javalite.activejdbc.Model;

public class ModelEvent {
	private final Model	model;

	public ModelEvent(final Model model) {
		this.model = model;
	}

	public Model getModel() {
		return model;
	}
}
