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

import javax.sound.sampled.*;
import javax.sound.sampled.DataLine.Info;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 01.01.12
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public class Sound
{

	public static void playFile(final String file)
	{
		final File fh = new File(file);
		if (!fh.canExecute()) {
			return;
		}

		AudioInputStream audioInputStream = null;
		try {
			audioInputStream = AudioSystem.getAudioInputStream(fh);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		AudioFormat af = null;
		if (audioInputStream != null) {
			af = audioInputStream.getFormat();
		}
		int size = 0;
		if (af != null) {
			size = (int) (af.getFrameSize() * audioInputStream.getFrameLength());
		}
		final byte[] audio = new byte[size];
		final Info info = new Info(Clip.class, af, size);
		try {
			if (audioInputStream != null) {
				//noinspection ResultOfMethodCallIgnored
				audioInputStream.read(audio, 0, size);
			}
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		try {
			final Clip clip = (Clip) AudioSystem.getLine(info);
			if (clip != null) {
				clip.open(af, audio, 0, size);
				clip.start();
			}
		} catch (LineUnavailableException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
