package org.chaosfisch.youtubeuploader.services.youtube.impl;

import java.io.File;

import org.chaosfisch.youtubeuploader.models.Queue;
import org.chaosfisch.youtubeuploader.models.Setting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnddirImpl
{
	Queue	queue;
	Logger	logger	= LoggerFactory.getLogger(getClass());
	File	fileToUpload;

	public void enddirAction()
	{
		if ((queue.getString("enddir") != null) && !queue.getString("enddir").isEmpty())
		{
			final File enddir = new File(queue.getString("enddir"));
			if (enddir.exists())
			{
				logger.info("Moving file to {}", enddir);

				final Boolean endDirRename = Setting.findById("coreplugin.general.enddirtitle").getBoolean("value");

				File endFile;
				if ((endDirRename != null) && (endDirRename == true))
				{
					final String fileName = queue.getString("title").replaceAll("[\\?\\*:\\\\<>\"/]", "");
					endFile = new File(enddir.getAbsolutePath() + "/" + fileName
							+ queue.getString("file").substring(queue.getString("file").lastIndexOf(".")));
				} else
				{
					endFile = new File(enddir.getAbsolutePath() + "/" + fileToUpload.getName());
				}
				if (endFile.exists())
				{
					endFile = new File(endFile.getAbsolutePath().substring(0, endFile.getAbsolutePath().lastIndexOf(".")) + "(2)"
							+ endFile.getAbsolutePath().substring(endFile.getAbsolutePath().lastIndexOf(".")));
				}
				if (fileToUpload.renameTo(endFile))
				{
					logger.info("Done moving: {}", endFile.getAbsolutePath());
				} else
				{
					logger.info("Failed moving");
				}
			}
		}
	}
}
