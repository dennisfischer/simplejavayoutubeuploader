package org.chaosfisch.youtubeuploader.services.youtube.impl;

import org.chaosfisch.util.ExtendedPlaceholders;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BrowserImpl
{
	Upload					queue;
	Logger					logger	= LoggerFactory.getLogger(getClass());
	ExtendedPlaceholders	extendedPlacerholders;

	public void browserAction()
	{
		if ((!queue.getBoolean("monetize")) && (!queue.getBoolean("claim")) && (queue.get("release") == null) && !queue.getBoolean("thumbnail")) { return; }

		logger.info("Monetizing, Releasing, Partner-features, Saving...");

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
