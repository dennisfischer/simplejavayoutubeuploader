///*
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
