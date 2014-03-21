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

import javafx.beans.property.SimpleListProperty;

import java.io.IOException;

public interface IAccountService {

	SimpleListProperty<AccountModel> accountModelsProperty();

	void remove(AccountModel accountModel);

	String getRefreshToken(String authorizationCode) throws IOException;

	void store(AccountModel accountModel);
}
