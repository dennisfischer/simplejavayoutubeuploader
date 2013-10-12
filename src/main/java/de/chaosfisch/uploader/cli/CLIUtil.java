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

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CLIUtil implements ICLIUtil {
	private static final String  INFO_LINE       = "# %-55s #%n";
	private static final String  PROMPT_LINE     = "$> %s%n";
	private static final String  INPUT_LINE      = "$> ";
	private static final Pattern COMMAND_PATTERN = Pattern.compile("^(\\w+) (\\w+)$", Pattern.MULTILINE);

	private final Scanner scanner = new Scanner(System.in);
	private final EventBus eventBus;

	@Inject
	public CLIUtil(final EventBus eventBus) {
		this.eventBus = eventBus;
	}

	@Override
	public void printInfo(final String msg) {
		System.out.printf(INFO_LINE, msg);
	}

	@Override
	public void printPrompt(final String msg) {
		System.out.printf(PROMPT_LINE, msg);
	}

	@Override
	public void printInput() {
		System.out.printf(INPUT_LINE);
	}

	@Override
	public boolean printContinuePrompt(final String msg) {
		printPrompt(msg);
		printInput();
		final String command = readCommand();
		return "y".equals(command) || !"n".equals(command) && printContinuePrompt(msg);
	}

	@Override
	public String readCommand() {
		return scanner.hasNextLine() ? scanner.nextLine() : "app quitnow";
	}

	@Override
	public String promptInput(final String msg) {
		printPrompt(msg);
		printInput();
		return readCommand();
	}

	@Override
	public void listenToCommands() {
		printInput();
		parseCommand(readCommand());
	}

	@Override
	public void parseCommand(final String command) {
		final Matcher m = COMMAND_PATTERN.matcher(command);
		if (!m.find()) {
			printPrompt(String.format("Invalid command: %s", command));
		} else {
			eventBus.post(new CLIEvent(m.group(1), m.group(2)));
		}
	}
}
