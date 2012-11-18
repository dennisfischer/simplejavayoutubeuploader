package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.util.regex.Pattern;

import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.youtubeuploader.models.Placeholder;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.javalite.activejdbc.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserImpl
{
	Queue					queue;
	Logger					logger	= LoggerFactory.getLogger(getClass());
	ExtendedPlaceholders	extendedPlacerholders;

	public void browserAction()
	{
		if ((!queue.getBoolean("monetize")) && (!queue.getBoolean("claim")) && (queue.get("release") == null) && !queue.getBoolean("thumbnail")) { return; }

		logger.info("Monetizing, Releasing, Partner-features, Saving...");

		for (final Model placeholder : Placeholder.findAll())
		{

			queue.setString("webTitle",
							queue.getString("webTitle").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("webDescription",
							queue.getString("webDescription").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																			placeholder.getString("replacement")));
			queue.setString("webID",
							queue.getString("webID").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
																placeholder.getString("replacement")));
			queue.setString("webNotes",
							queue.getString("webNotes").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));

			queue.setString("tvTMSID",
							queue.getString("tvTMSID").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("tvISAN",
							queue.getString("tvISAN").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("tvEIDR",
							queue.getString("tvEIDR").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("showTitle",
							queue.getString("showTitle").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("episodeTitle",
							queue.getString("episodeTitle").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																		placeholder.getString("replacement")));
			queue.setString("seasonNb",
							queue.getString("seasonNb").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("episodeNb",
							queue.getString("episodeNb").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("tvID",
							queue.getString("tvID").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																placeholder.getString("replacement")));
			queue.setString("tvNotes",
							queue.getString("tvNotes").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));

			queue.setString("movieTitle",
							queue.getString("movieTitle").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																		placeholder.getString("replacement")));
			queue.setString("movieDescription",
							queue.getString("movieDescription").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																			placeholder.getString("replacement")));
			queue.setString("movieTMSID",
							queue.getString("movieTMSID").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																		placeholder.getString("replacement")));
			queue.setString("movieISAN",
							queue.getString("movieISAN").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("movieEIDR",
							queue.getString("movieEIDR").replaceAll(Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("movieID",
							queue.getString("movieID").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																	placeholder.getString("replacement")));
			queue.setString("movieNotes",
							queue.getString("movieNotes").replaceAll(	Pattern.quote(placeholder.getString("placeholder")),
																		placeholder.getString("replacement")));
		}
		extendedPlacerholders.register("{title}", queue.getString("title"));
		extendedPlacerholders.register("{description}", queue.getString("description"));

		queue.setString("webTitle", extendedPlacerholders.replace(queue.getString("webTitle")));
		queue.setString("webDescription", extendedPlacerholders.replace(queue.getString("webDescription")));
		queue.setString("webID", extendedPlacerholders.replace(queue.getString("webID")));
		queue.setString("webNotes", extendedPlacerholders.replace(queue.getString("webNotes")));

		queue.setString("tvTMSID", extendedPlacerholders.replace(queue.getString("tvTMSID")));
		queue.setString("tvISAN", extendedPlacerholders.replace(queue.getString("tvISAN")));
		queue.setString("tvEIDR", extendedPlacerholders.replace(queue.getString("tvEIDR")));
		queue.setString("showTitle", extendedPlacerholders.replace(queue.getString("showTitle")));
		queue.setString("episodeTitle", extendedPlacerholders.replace(queue.getString("episodeTitle")));
		queue.setString("seasonNb", extendedPlacerholders.replace(queue.getString("seasonNb")));
		queue.setString("episodeNb", extendedPlacerholders.replace(queue.getString("episodeNb")));
		queue.setString("tvID", extendedPlacerholders.replace(queue.getString("tvID")));
		queue.setString("tvNotes", extendedPlacerholders.replace(queue.getString("tvNotes")));

		queue.setString("movieTitle", extendedPlacerholders.replace(queue.getString("movieTitle")));
		queue.setString("movieDescription", extendedPlacerholders.replace(queue.getString("movieDescription")));
		queue.setString("movieTMSID", extendedPlacerholders.replace(queue.getString("movieTMSID")));
		queue.setString("movieISAN", extendedPlacerholders.replace(queue.getString("movieISAN")));
		queue.setString("movieEIDR", extendedPlacerholders.replace(queue.getString("movieEIDR")));
		queue.setString("movieID", extendedPlacerholders.replace(queue.getString("movieID")));
		queue.setString("movieNotes", extendedPlacerholders.replace(queue.getString("movieNotes")));

		final MetadataFrontendChangerServiceImpl metadataChanger = new MetadataFrontendChangerServiceImpl(queue);
		metadataChanger.run();
	}
}
