package org.chaosfisch.youtubeuploader.services.youtube.spi;

import org.chaosfisch.google.auth.AuthenticationException;
import org.chaosfisch.youtubeuploader.models.Upload;
import org.chaosfisch.youtubeuploader.services.youtube.impl.ResumeInfo;
import org.chaosfisch.youtubeuploader.services.youtube.uploader.UploadException;

public interface ResumeableManager
{
	String parseVideoId(String atomData);

	boolean canContinue();

	ResumeInfo fetchResumeInfo(Upload queue) throws UploadException, AuthenticationException;

	void setRetries(int i);

	int getRetries();

	void delay();

}
