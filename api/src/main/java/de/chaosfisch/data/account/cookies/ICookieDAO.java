/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.account.cookies;

import de.chaosfisch.data.IGenericDAO;

import java.util.List;

public interface ICookieDAO extends IGenericDAO<CookieDTO> {
	List<CookieDTO> getAll(String accountId);

	CookieDTO get(String accountId, String name);

	void clearOld(String accountId);
}
