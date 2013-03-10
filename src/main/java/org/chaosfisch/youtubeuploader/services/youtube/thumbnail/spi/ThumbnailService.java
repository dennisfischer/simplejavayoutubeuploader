package org.chaosfisch.youtubeuploader.services.youtube.thumbnail.spi;

import org.chaosfisch.exceptions.SystemException;

public interface ThumbnailService {
	Integer upload(final String content, final String thumbnail, final String videoid) throws SystemException;
}
