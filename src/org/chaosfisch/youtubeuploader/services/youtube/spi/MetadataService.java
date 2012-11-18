package org.chaosfisch.youtubeuploader.services.youtube.spi;

import java.io.File;

import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.youtubeuploader.models.Account;
import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.MetadataException;

public interface MetadataService
{
	String atomBuilder(Queue queue);

	String submitMetadata(String atomData, File fileToUpload, Account account) throws MetadataException, AuthenticationException;
}
