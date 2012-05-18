///*
// * Copyright (c) 2012, Dennis Fischer
// *
// * This is free software; you can redistribute it and/or modify it
// * under the terms of the GNU Lesser General Public License as
// * published by the Free Software Foundation; either version 2.1 of
// * the License, or (at your option) any later version.
// *
// * This software is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
// * Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public
// * License along with this software; if not, write to the Free
// * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
// * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
// */
//
//package org.chaosfisch.youtubeuploader.plugins.directoryplugin.models;
//
//import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Account;
//import org.chaosfisch.youtubeuploader.plugins.coreplugin.models.Preset;
//import org.hibernate.HibernateException;
//import org.hibernate.Session;
//import org.hibernate.SessionFactory;
//import org.hibernate.event.spi.PreDeleteEvent;
//import org.hibernate.event.spi.PreDeleteEventListener;
//
//import java.util.Collection;
//import java.util.Collections;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * Created with IntelliJ IDEA.
// * User: Dennis
// * Date: 02.04.12
// * Time: 13:15
// * To change this template use File | Settings | File Templates.
// */
//public class EventListener implements PreDeleteEventListener
//{
//	private final SessionFactory sessionFactory;
//
//	private final Collection<String> processingEntities = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
//
//	public EventListener(final SessionFactory sessionFactory)
//	{
//		this.sessionFactory = sessionFactory;
//	}
//
//	@Override public boolean onPreDelete(final PreDeleteEvent preDeleteEvent) throws HibernateException
//	{
//		if (preDeleteEvent.getEntity() instanceof Preset) {
//			final Preset presetEntry = (Preset) preDeleteEvent.getEntity();
//			if (this.processingEntities.contains(presetEntry.getClass().getName() + "_" + presetEntry.getIdentity())) {
//				return false;
//			}
//
//			this.processingEntities.add(presetEntry.getClass().getName() + "_" + presetEntry.getIdentity());
//			final Session session = this.sessionFactory.openSession();
//			try {
//				session.getTransaction().begin();
//				session.createQuery("UPDATE Directory SET preset_id = NULL, locked = true WHERE preset_id = " + presetEntry.getIdentity()).executeUpdate(); //NON-NLS
//				session.getTransaction().commit();
//			} catch (Exception ex) {
//				session.getTransaction().rollback();
//			} finally {
//				this.processingEntities.remove(presetEntry.getClass().getName() + "_" + presetEntry.getIdentity());
//			}
//		} else if (preDeleteEvent.getEntity() instanceof Account) {
//			final Account accountEntry = (Account) preDeleteEvent.getEntity();
//			// check if it's already been processed
//			if (this.processingEntities.contains(accountEntry.getClass().getName() + "_" + accountEntry.getIdentity())) {
//				return false;
//			}
//			// block it by ID
//			this.processingEntities.add(accountEntry.getClass().getName() + "_" + accountEntry.getIdentity());
//			final Session session = this.sessionFactory.openSession();
//			try {
//
//				session.getTransaction().begin();
//				session.createQuery("DELETE FROM Playlist WHERE account_id = " + accountEntry.getIdentity()).executeUpdate();  //NON-NLS
//				session.createQuery("UPDATE Preset SET account_id = NULL, playlist_id = NULL WHERE ACCOUNT_ID = " + accountEntry.getIdentity()).executeUpdate(); //NON-NLS
//				session.createQuery("UPDATE Queue SET account_id = NULL, locked = true WHERE account_id = " + accountEntry.getIdentity()).executeUpdate(); //NON-NLS
//				session.getTransaction().commit();
//			} catch (Exception ex) {
//				session.getTransaction().rollback();
//			} finally {
//				// release
//				this.processingEntities.remove(accountEntry.getClass().getName() + "_" + accountEntry.getIdentity());
//			}
//		}
//		return false;
//	}
//}
