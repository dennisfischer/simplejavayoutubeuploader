package org.chaosfisch.youtubeuploader.models.events;

public class ModelEvent {
	private final Object	model;

	public ModelEvent(final Object model) {
		this.model = model;
	}

	public Object getModel() {
		return model;
	}

}
