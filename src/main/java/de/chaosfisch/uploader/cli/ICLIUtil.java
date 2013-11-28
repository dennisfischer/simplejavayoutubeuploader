/*
 * Copyright (c) 2013 Dennis Fischer.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0+
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 *
 * Contributors: Dennis Fischer
 */

package de.chaosfisch.uploader.cli;

public interface ICLIUtil {

	void printInfo(String msg);

	void printPrompt(String msg);

	void printInput();

	boolean printContinuePrompt(String msg);

	String readCommand();

	String promptInput(String msg);

	void listenToCommands();

	void parseCommand(String command);

	String promptInput(String msg, String defaultValue);
}
