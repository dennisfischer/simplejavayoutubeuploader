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

package org.chaosfisch.youtubeuploader.plugins.coreplugin.models;

import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.bushe.swing.event.annotation.EventTopicSubscriber;
import org.chaosfisch.youtubeuploader.db.AccountEntry;
import org.chaosfisch.youtubeuploader.services.AccountService;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 21:53
 * To change this template use File | Settings | File Templates.
 */
public class AccountListModel extends AbstractListModel implements ComboBoxModel
{

	private final IdentityList<AccountEntry> accountEntries = new IdentityList<AccountEntry>();
	private       int                        selectedRow    = 0;

	public AccountListModel()
	{
		AnnotationProcessor.process(this);
	}

	public AccountListModel(final List<AccountEntry> l)
	{
		this.accountEntries.addAll(l);
		AnnotationProcessor.process(this);
	}

	@Override
	public int getSize()
	{
		return this.accountEntries.size();
	}

	@Override
	public Object getElementAt(final int index)
	{
		return this.accountEntries.get(index);
	}

	void addAccountEntry(final AccountEntry accountEntry)
	{
		this.accountEntries.add(accountEntry);
		this.fireIntervalAdded(this, 0, this.getSize());
	}

	public void addAccountEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof AccountEntry) {
				this.addAccountEntry((AccountEntry) o);
			}
		}
	}

	public AccountEntry removeSelectedAccountEntry()
	{
		final AccountEntry accountEntry = this.accountEntries.remove(this.selectedRow);
		this.fireContentsChanged(this, 0, this.getSize());
		return accountEntry;
	}

	public List<AccountEntry> getAccountList()
	{
		return new ArrayList<AccountEntry>(this.accountEntries);
	}

	void removeAccountEntry(final AccountEntry accountEntry)
	{
		this.accountEntries.remove(accountEntry);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public void setSelectedItem(final Object selectedItem)
	{
		final AccountEntry accountEntry = (AccountEntry) selectedItem;
		this.selectedRow = this.accountEntries.indexOf(accountEntry);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public Object getSelectedItem()
	{
		if (this.accountEntries.size() - 1 >= this.selectedRow) {
			return this.accountEntries.get(this.selectedRow);
		} else {
			this.selectedRow = 0;
		}
		return null;
	}

	public boolean hasAccountEntryAt(final int selectedRow)
	{
		return this.accountEntries.size() >= selectedRow && selectedRow != -1;
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = AccountService.ACCOUNT_ENTRY_ADDED)
	public void onAccountAdded(final String topic, final Object o)
	{
		this.addAccountEntry((AccountEntry) o);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = AccountService.ACCOUNT_ENTRY_REMOVED)
	public void onAccountRemoved(final String topic, final Object o)
	{
		this.removeAccountEntry((AccountEntry) o);
	}
}
