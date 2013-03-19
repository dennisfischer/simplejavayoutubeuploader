package org.chaosfisch.youtubeuploader.guice;

import javafx.concurrent.Service;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class CommandProvider implements ICommandProvider {

	@Inject
	public Injector	injector;

	@Override
	public <T extends Service<?>> T get(final Class<T> type) {
		return injector.getInstance(type);
	}
}