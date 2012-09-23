/*******************************************************************************
 * Copyright (c) 2012 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Dennis Fischer
 ******************************************************************************/

package org.chaosfisch.youtubeuploader;

import java.io.IOException;
import java.io.Reader;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.SqlSessionFactory;
import org.bushe.swing.event.annotation.AnnotationProcessor;
import org.chaosfisch.youtubeuploader.services.uploader.Uploader;

import com.google.inject.Inject;

public class CorePlugin
{
	@Inject
	private Uploader				uploader;
	@Inject
	private final SqlSessionFactory	sessionFactory;

	@Inject
	public CorePlugin(final SqlSessionFactory sessionFactory) throws IOException
	{
		this.sessionFactory = sessionFactory;
		AnnotationProcessor.process(this);
		loadDatabase();
	}

	// uses the new MyBatis style of lookup
	public void loadDatabase() throws IOException
	{
		final Reader schemaReader = Resources.getResourceAsReader("scripts/scheme.sql");
		final ScriptRunner scriptRunner = new ScriptRunner(sessionFactory.openSession().getConnection());
		scriptRunner.setStopOnError(true);
		scriptRunner.setLogWriter(null);
		scriptRunner.setAutoCommit(true);
		scriptRunner.setDelimiter(";");
		scriptRunner.runScript(schemaReader);
	}

	public void onStart()
	{
		uploader.runStarttimeChecker();
	}

	public void onEnd()
	{
		uploader.stopStarttimeChecker();
		uploader.exit();
	}
}
