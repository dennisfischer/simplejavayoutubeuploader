package org.chaosfisch.youtubeuploader.command;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.chaosfisch.google.auth.GoogleAuthUtil;
import org.chaosfisch.youtubeuploader.db.dao.AccountDao;
import org.chaosfisch.youtubeuploader.db.generated.tables.pojos.Account;

import com.google.inject.Inject;

public class AddAccountCommand extends Service<Void> {

	@Inject
	private GoogleAuthUtil	authTokenHelper;

	@Inject
	private AccountDao		accountDao;

	public String			name;
	public String			password;

	@Override
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				final Account account = new Account();
				account.setName(name);
				account.setPassword(password);
				authTokenHelper.verifyAccount(account);
				accountDao.insert(account);
				return null;
			}
		};
	}
}
