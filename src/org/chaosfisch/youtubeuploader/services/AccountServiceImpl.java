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

package org.chaosfisch.youtubeuploader.services;

import com.google.inject.Inject;
import org.bushe.swing.event.EventBus;
import org.chaosfisch.youtubeuploader.db.AccountEntry;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 10.01.12
 * Time: 19:36
 * To change this template use File | Settings | File Templates.
 */
public class AccountServiceImpl implements AccountService
{

	private final SessionFactory sessionFactory;

	@Inject
	public AccountServiceImpl(final SessionFactory sessionFactory)
	{
		this.sessionFactory = sessionFactory;
	}

	@Override
	public AccountEntry deleteAccountEntry(final AccountEntry accountEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.delete(accountEntry);
		session.getTransaction().commit();
		EventBus.publish(ACCOUNT_ENTRY_REMOVED, accountEntry);
		return accountEntry;
	}

	@Override
	public List getAllAccountEntry()
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final List returnList = session.createQuery("select a from AccountEntry as a order by name").list(); //NON-NLS
		session.getTransaction().commit();
		return returnList;
	}

	@Override
	public AccountEntry createAccountEntry(final AccountEntry accountEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		final Query temp;
		temp = session.createQuery("Select Count(*) From AccountEntry Where name = :name");
		temp.setParameter("name", accountEntry.getName());

		if ((Long) temp.uniqueResult() > 0) {
			session.getTransaction().commit();
			return null;
		}
		session.save(accountEntry);
		session.getTransaction().commit();
		EventBus.publish(ACCOUNT_ENTRY_ADDED, accountEntry);
		return accountEntry;
	}

	@Override
	public AccountEntry updateAccountEntry(final AccountEntry accountEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.update(accountEntry);
		session.getTransaction().commit();
		EventBus.publish(ACCOUNT_ENTRY_UPDATED, accountEntry);
		return accountEntry;
	}

	@Override
	public AccountEntry findAccountEntry(final int identifier)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		return (AccountEntry) session.load(AccountEntry.class, identifier);
	}

	@Override
	public void refreshAccount(final AccountEntry accountEntry)
	{
		final Session session = this.sessionFactory.getCurrentSession();
		session.getTransaction().begin();
		session.refresh(accountEntry);
		session.getTransaction().commit();
	}
}
