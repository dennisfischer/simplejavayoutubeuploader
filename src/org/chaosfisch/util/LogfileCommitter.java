package org.chaosfisch.util;

import java.util.UUID;

import org.chaosfisch.youtubeuploader.models.Setting;

public class LogfileCommitter
{
	public static void commit()
	{
		System.out.println(getUniqueId());
	}

	private static String getUniqueId()
	{
		Setting uuidSetting = Setting.findById("hidden.uuid");
		if (uuidSetting == null)
		{
			uuidSetting = Setting.createIt("id", "hidden.uuid", "value", UUID.randomUUID());
		}

		return uuidSetting.getString("value");
	}
}
