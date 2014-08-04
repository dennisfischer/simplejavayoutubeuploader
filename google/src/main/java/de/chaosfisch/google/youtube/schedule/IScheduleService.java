/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.google.youtube.schedule;

import com.google.api.client.util.DateTime;
import de.chaosfisch.google.account.Account;

/**
 * Created by Dennis on 04.08.2014.
 */
public interface IScheduleService {

	/**
	 * Sets the release date of a video
	 *
	 * @param dateTime the release date time
	 * @param videoid  the matching videoid
	 * @param account  the matching videoid
	 */
	void schedule(DateTime dateTime, String videoid, Account account) throws ScheduleIOException;
}
