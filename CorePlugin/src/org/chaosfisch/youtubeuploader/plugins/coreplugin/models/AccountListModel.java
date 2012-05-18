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
import org.chaosfisch.youtubeuploader.plugins.coreplugin.services.spi.AccountService;

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

	private final IdentityList<Account> accounts    = new IdentityList<Account>();
	private       int                   selectedRow = 0;

	public AccountListModel()
	{
		AnnotationProcessor.process(this);
	}

	public AccountListModel(final List<Account> l)
	{
		this.accounts.addAll(l);
		AnnotationProcessor.process(this);
	}

	@Override
	public int getSize()
	{
		return this.accounts.size();
	}

	@Override
	public Object getElementAt(final int index)
	{
		return this.accounts.get(index);
	}

	void addAccountEntry(final Account account)
	{
		this.accounts.add(account);
		this.fireIntervalAdded(this, 0, this.getSize());
	}

	public void addAccountEntryList(final List l)
	{
		for (final Object o : l) {
			if (o instanceof Account) {
				this.addAccountEntry((Account) o);
			}
		}
	}

	public Account removeSelectedAccountEntry()
	{
		final Account account = this.accounts.remove(this.selectedRow);
		this.fireContentsChanged(this, 0, this.getSize());
		return account;
	}

	public List<Account> getAccountList()
	{
		return new ArrayList<Account>(this.accounts);
	}

	void removeAccountEntry(final Account account)
	{
		this.accounts.remove(account);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public void setSelectedItem(final Object selectedItem)
	{
		final Account account = (Account) selectedItem;
		this.selectedRow = this.accounts.indexOf(account);
		this.fireContentsChanged(this, 0, this.getSize());
	}

	@Override
	public Account getSelectedItem()
	{
		if (this.accounts.size() - 1 >= this.selectedRow) {
			return this.accounts.get(this.selectedRow);
		} else {
			this.selectedRow = 0;
		}
		return null;
	}

	public boolean hasAccountEntryAt(final int selectedRow)
	{
		return this.accounts.size() >= selectedRow && selectedRow != -1;
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = AccountService.ACCOUNT_ENTRY_ADDED)
	public void onAccountAdded(final String topic, final Account account)
	{
		this.addAccountEntry(account);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = AccountService.ACCOUNT_ENTRY_REMOVED)
	public void onAccountRemoved(final String topic, final Account account)
	{
		this.removeAccountEntry(account);
	}

	@SuppressWarnings("UnusedParameters") @EventTopicSubscriber(topic = AccountService.ACCOUNT_ENTRY_UPDATED)
	public void onAccountUpdated(final String topic, final Account account)
	{
		this.accounts.set(this.accounts.indexOf(account), account);
	}
}
