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
	@Override
	protected Void doInBackground() throws Exception
	{
		this.background();
		return null;
	}

	@Override
	protected void done()
	{
		try {
			this.get();
		} catch (InterruptedException ignored) {
		} catch (CancellationException ignored) {
		} catch (ExecutionException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		this.onDone();
	}

	protected abstract void background();

	protected abstract void onDone();
}