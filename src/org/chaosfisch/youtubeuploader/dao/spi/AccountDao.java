/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/
/*
 * Copyright (c) 2012, Dennis Fischer
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.chaosfisch.youtubeuploader.dao.spi;

import org.chaosfisch.youtubeuploader.models.Account;

public interface AccountDao extends CRUDDao<Account>
{
	/**
	 * Event: Before Account-object is added
	 */
	String	ACCOUNT_PRE_ADDED		= "accountPreAdded";

	/**
	 * Event: Before Account-object is removed
	 */
	String	ACCOUNT_PRE_REMOVED		= "accountPreRemoved";

	/**
	 * Event: Before Account-object is updated
	 */
	String	ACCOUNT_PRE_UPDATED		= "accountPreUpdated";

	/**
	 * Event: After Account-object was added
	 */
	String	ACCOUNT_POST_ADDED		= "accountPostAdded";

	/**
	 * Event: After Account-object was removed
	 */
	String	ACCOUNT_POST_REMOVED	= "accountPostRemoved";

	/**
	 * Event: After Account-object was updated
	 */
	String	ACCOUNT_POST_UPDATED	= "accountPostUpdated";
}
