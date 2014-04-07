/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.youtube.account;

import de.chaosfisch.youtube.Event;

public class AccountRemovedEvent implements Event {

	private final AccountModel accountModel;

	public AccountRemovedEvent(final AccountModel accountModel) {this.accountModel = accountModel;}

	public AccountModel getAccountModel() {
		return accountModel;
	}
}
