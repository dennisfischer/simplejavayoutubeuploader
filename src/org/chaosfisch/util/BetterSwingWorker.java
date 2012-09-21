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

package org.chaosfisch.util;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 01.01.12
 * Time: 10:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class BetterSwingWorker extends SwingWorker<Void, Void>
{

	private final Logger logger = Logger.getLogger(getClass().getName());

	@Override
	protected Void doInBackground() throws Exception
	{
		background();
		return null;
	}

	@Override
	protected void done()
	{
		try {
			get();
		} catch (InterruptedException ex) {
			logger.warn(ex.getMessage(), ex);
		} catch (CancellationException ex) {
			logger.warn(ex.getMessage(), ex);
		} catch (ExecutionException ex) {
			logger.warn(ex.getMessage(), ex);
		}
		onDone();
	}

	protected abstract void background();

	protected abstract void onDone();
}