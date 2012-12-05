package org.chaosfisch.youtubeuploader.services.youtube.spi;

import java.io.File;

import org.chaosfisch.youtubeuploader.models.Upload;

public interface EnddirService
{

	public abstract void moveFileByUpload(File fileToMove, Upload upload);

}