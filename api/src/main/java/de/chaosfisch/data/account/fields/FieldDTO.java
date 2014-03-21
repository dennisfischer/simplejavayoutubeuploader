/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.data.account.fields;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class FieldDTO {
	@NotNull
	private String accountId;
	@NotNull
	private String name;

	public FieldDTO() {
	}

	public FieldDTO(@NotNull final String accountId, @NotNull final String name) {
		this.accountId = accountId;
		this.name = name;
	}

	@NotNull
	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(@NotNull final String accountId) {
		this.accountId = accountId;
	}

	@NotNull
	public String getName() {
		return name;
	}

	public void setName(@NotNull final String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		int result = accountId.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FieldDTO)) {
			return false;
		}

		final FieldDTO fieldDTO = (FieldDTO) obj;
		return accountId.equals(fieldDTO.accountId) && name.equals(fieldDTO.name);
	}

	@Override
	@NonNls
	public String toString() {
		return "FieldDTO{" +
				"accountId='" + accountId + '\'' +
				", name='" + name + '\'' +
				'}';
	}
}
