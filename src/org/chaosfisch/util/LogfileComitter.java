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

package org.chaosfisch.util;

import java.util.Map;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

public class LogfileComitter
{
	/**
	 * Sends the generated logfiles
	 * 
	 * @param username
	 *            the user which sends the files
	 * @throws EmailException
	 *             if email fails to send
	 */
	public static void sendMail(final String username) throws EmailException
	{
		final EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(String.format("%s/SimpleJavaYoutubeUploader/logs/applog.log", System.getProperty("user.home")));
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("Logfiles from " + username);
		attachment.setName("Logfile.txt");

		// Create the email message
		final MultiPartEmail email = new MultiPartEmail();
		email.addTo("dennis0912@live.de", "Dennis Fischer");

		email.setHostName("mail.gmx.net");
		email.setAuthentication("simplejavayoutubeuploader@gmx.de", "simplejavayoutubeuploader");
		email.setFrom("simplejavayoutubeuploader@gmx.de", "Me");

		email.setSubject("Logfiles from " + username);

		final StringBuilder message = new StringBuilder();

		for (final Map.Entry<Object, Object> property : System.getProperties().entrySet())
		{
			message.append(property.getKey()).append("=>").append(property.getValue()).append("\r\n");
		}
		email.setMsg(message.toString());

		// add the attachment
		email.attach(attachment);

		// send the email
		email.send();
	}
}
