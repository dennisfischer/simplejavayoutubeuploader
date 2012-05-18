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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi;

import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 15:38
 * To change this template use File | Settings | File Templates.
 */
public interface AccountService
{
	public static final String ACCOUNT_ENTRY_ADDED   = "accountEntryAdded"; //NON-NLS
	public static final String ACCOUNT_ENTRY_REMOVED = "accountEntryRemoved"; //NON-NLS
	public static final String ACCOUNT_ENTRY_UPDATED = "accountEntryUpdated"; //NON-NLS

	Account createAccountEntry(Account account);

	Account deleteAccountEntry(Account account);

	Account updateAccountEntry(Account account);

	Account findAccountEntry(int identifier);

	List<Account> getAllAccountEntry();
}
