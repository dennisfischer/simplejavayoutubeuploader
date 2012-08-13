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

package org.chaosfisch.youtubeuploader.util;

import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: Dennis
 * Date: 07.08.12
 * Time: 20:44
 * To change this template use File | Settings | File Templates.
 */
public class LogfileComitter
{
	public static void sendMail(final String username) throws EmailException
	{
		final EmailAttachment attachment = new EmailAttachment();
		attachment.setPath(String.format("%s/SimpleJavaYoutubeUploader/logs/applog.log", System.getProperty("user.home"))); //NON-NLS
		attachment.setDisposition(EmailAttachment.ATTACHMENT);
		attachment.setDescription("Logfiles from " + username); //NON-NLS
		attachment.setName("Logfile.txt"); //NON-NLS

		// Create the email message
		final MultiPartEmail email = new MultiPartEmail();
		email.addTo("dennis0912@live.de", "Dennis Fischer"); //NON-NLS

		email.setHostName("mail.gmx.net"); //NON-NLS
		email.setAuthentication("simplejavayoutubeuploader@gmx.de", "simplejavayoutubeuploader"); //NON-NLS
		email.setFrom("simplejavayoutubeuploader@gmx.de", "Me"); //NON-NLS

		email.setSubject("Logfiles from " + username); //NON-NLS

		final StringBuilder message = new StringBuilder();

		for (final Map.Entry<Object, Object> property : System.getProperties().entrySet()) {
			message.append(property.getKey()).append("=>").append(property.getValue()).append("\r\n");
		}
		email.setMsg(message.toString());//NON-NLS

		// add the attachment
		email.attach(attachment);

		// send the email
		email.send();
	}
}
