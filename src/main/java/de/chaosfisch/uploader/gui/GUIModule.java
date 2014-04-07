/**************************************************************************************************
 * Copyright (c) 2014 Dennis Fischer.                                                             *
 * All rights reserved. This program and the accompanying materials                               *
 * are made available under the terms of the GNU Public License v3.0+                             *
 * which accompanies this distribution, and is available at                                       *
 * http://www.gnu.org/licenses/gpl.html                                                           *
 *                                                                                                *
 * Contributors: Dennis Fischer                                                                   *
 **************************************************************************************************/

package de.chaosfisch.uploader.gui;

import dagger.Module;
import dagger.Provides;
import de.chaosfisch.APIModule;
import de.chaosfisch.uploader.ApplicationData;
import de.chaosfisch.uploader.gui.account.AccountPresenter;
import de.chaosfisch.uploader.gui.account.AccountView;
import de.chaosfisch.uploader.gui.account.add.*;
import de.chaosfisch.uploader.gui.account.entry.EntryPresenter;
import de.chaosfisch.uploader.gui.account.entry.EntryView;
import de.chaosfisch.uploader.gui.edit.EditDataModel;
import de.chaosfisch.uploader.gui.edit.EditPresenter;
import de.chaosfisch.uploader.gui.edit.EditView;
import de.chaosfisch.uploader.gui.edit.left.EditLeftPresenter;
import de.chaosfisch.uploader.gui.edit.monetization.EditMonetizationView;
import de.chaosfisch.uploader.gui.edit.partner.EditPartnerView;
import de.chaosfisch.uploader.gui.edit.right.EditRightPresenter;
import de.chaosfisch.uploader.gui.main.MainPresenter;
import de.chaosfisch.uploader.gui.project.ProjectPresenter;
import de.chaosfisch.uploader.gui.upload.UploadDataModel;
import de.chaosfisch.uploader.gui.upload.UploadPresenter;
import de.chaosfisch.uploader.gui.upload.UploadView;
import de.chaosfisch.youtube.account.IAccountService;
import de.chaosfisch.youtube.category.ICategoryService;
import de.chaosfisch.youtube.playlist.IPlaylistService;
import de.chaosfisch.youtube.upload.IUploadService;
import org.sormula.Database;
import org.sormula.translator.TypeTranslator;

import javax.inject.Singleton;
import java.io.File;
import java.sql.*;
import java.time.Instant;

@Module(
		includes = {APIModule.class},
		staticInjections = {AccountAddDataModel.class},
		library = true,
		injects = {EditPresenter.class, StepPresenter.class, AddPresenter.class, MainPresenter.class, EditRightPresenter.class, ProjectPresenter.class,
				UploadPresenter.class, EditLeftPresenter.class, AccountPresenter.class, EntryPresenter.class})
public class GUIModule {

	@Provides
	@Singleton
	StepPresenter provideStepPresenter() {
		return new StepPresenter();
	}

	@Provides
	AccountView provideAccountView() {
		return new AccountView();
	}

	@Provides
	EntryView provideEntryView() {
		return new EntryView();
	}

	@Provides
	EditView provideEditView() {
		return new EditView();
	}

	@Provides
	Step1View provideStep1View() {
		return new Step1View();
	}

	@Provides
	Step2View provideStep2View() {
		return new Step2View();
	}

	@Provides
	Step3View provideStep3View() {
		return new Step3View();
	}

	@Provides
	LoadingView provideLoadingView() {
		return new LoadingView();
	}

	@Provides
	UploadView provideUploadsView() {
		return new UploadView();
	}

	@Provides
	EditMonetizationView provideEditMonetizationView() {
		return new EditMonetizationView();
	}

	@Provides
	EditPartnerView provideEditPartnerView() {
		return new EditPartnerView();
	}

	@Provides
	@Singleton
	DataModel provideDataModel(final IAccountService iAccountService, final IPlaylistService iPlaylistService) {
		return new DataModel(iAccountService, iPlaylistService);
	}

	@Provides
	@Singleton
	UploadDataModel provideUploadModel(final IUploadService iUploadService) {
		return new UploadDataModel(iUploadService);
	}

	@Provides
	@Singleton
	EditDataModel provideEditDataModel(final ICategoryService iCategoryService, final IUploadService iUploadService) {
		return new EditDataModel(iCategoryService, iUploadService);
	}

	@Provides
	@Singleton
	Database provideDatabase() {
		try {
			final String path = String.format("jdbc:sqlite:%s/%s/database.db", ApplicationData.DATA_DIR, ApplicationData.VERSION);
			final File file = new File(String.format("%s/%s", ApplicationData.DATA_DIR, ApplicationData.VERSION));
			file.mkdirs();
			//noinspection resource,JDBCResourceOpenedButNotSafelyClosed
			final Connection connection = DriverManager.getConnection(path);

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					connection.close();
				} catch (final SQLException e) {
					e.printStackTrace();
				}
			}));

			final Database database = new Database(connection);
			database.putTypeTranslator(Instant.class, new TypeTranslator<Instant>() {
				@Override
				public Instant read(final ResultSet resultSet, final int columnIndex) throws Exception {
					// convert from TIMESTAMP to LocalDate
					final Timestamp timestamp = resultSet.getTimestamp(columnIndex);


					if (null == timestamp) {
						return null;
					} else {
						return Instant.ofEpochSecond(timestamp.getTime());
					}
				}

				@Override
				public void write(final PreparedStatement preparedStatement, final int parameterIndex, final Instant parameter) throws Exception {
					// convert from LocalDate to TIMESTAMP
					if (null == parameter) {
						preparedStatement.setTimestamp(parameterIndex, null);
					} else {
						preparedStatement.setTimestamp(parameterIndex, new Timestamp(parameter.getEpochSecond()));
					}
				}
			});
			return database;

		} catch (final SQLException e) {
			e.printStackTrace();
		}

		return null;
	}
}
