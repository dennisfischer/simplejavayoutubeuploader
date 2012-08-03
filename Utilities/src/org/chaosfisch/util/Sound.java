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
import java.io.File;
import java.io.IOException;
import java.sql.Time;

import static java.util.TimeZone.getDefault;

/**
 * Created by IntelliJ IDEA.
 * User: Dennis
 * Date: 01.01.12
 * Time: 10:25
 * To change this template use File | Settings | File Templates.
 */
public class Sound implements Runnable
{
	private              Thread  runner      = new Thread(this); //AbspielThread
	private static final int     BufferSize  = 1024; // Anzahl der Daten, die aufeinmal an die Soundkarte geschickt werden.
	private static final byte[]  buffer      = new byte[Sound.BufferSize];
	private              int     gainPercent = 90;  //gibt die Lautstärke in Prozent an.  (0% = -80dB und 100% = 6dB)
	private              Boolean stop        = false;
	private              Boolean loopPlay    = false;
	private File song;
	private static final long timeZoneKorrektur = getDefault().getOffset(0);
	private final        Time time              = new Time(-Sound.timeZoneKorrektur);
	private final        Time songTime          = new Time(-Sound.timeZoneKorrektur);
	private boolean reset;
	private Boolean isPlaying = false;
	private boolean pause;
	private boolean mute;
	private int lautstaerke = gainPercent;

	/**
	 * damit mp3-Dateien abgespielt werden können, muss das mp3plugin von Sun im Classpath stehen oder
	 * (zB. unter Eclipse) als Bibliothek eingetragen sein.
	 * Das gleiche gilt für flac-Dateien.
	 *
	 * Die Lautstärke "gainLevel" ist logarhytmisch von -80dB bis ca. 6dB. Daher ist die
	 * prozentuale Lautstärkeregelung nicht ganz korrekt.
	 */

	/**
	 * gibt die aktuelle Zeit an
	 *
	 * @return Time
	 */
	public Time getCurrentTime()
	{
		return (Time) time.clone();
	}

	/**
	 * gibt zurück, ob das Musikstück wiederholt wird
	 *
	 * @return boolean loopPlay
	 */
	public boolean isLoopPlay()
	{
		return loopPlay;
	}

	/**
	 * startet eine Endlosschleife
	 *
	 * @param loop boolean
	 */
	public void setLoopPlay(final Boolean loop)
	{
		loopPlay = loop;
	}

	/**
	 * stoppt die Wiedergabe
	 */
	public void stop()
	{
		stop = true;
	}

	/**
	 * starten der Wiedergabe
	 */
	public void play()
	{
		stop = false;
		if (!runner.isAlive()) {
			runner = new Thread(this);
			runner.start();
		}
	}

	/**
	 * gibt die aktuelle Lautstärke zurück
	 *
	 * @return int volume
	 */
	public int getVolume()
	{
		return gainPercent;
	}

	/**
	 * Wert zwischen 0% und 100%
	 *
	 * @param volumen int
	 */
	public void setVolumen(final int volumen)
	{
		if ((volumen <= 100) || (volumen >= 0)) {
			gainPercent = volumen;
		}
	}

	/**
	 * der WiedergabeThread
	 */
	public void run()
	{
		if (!(song.exists() && song.isFile())) {
			return;
		}
		try {
			final AudioInputStream in = AudioSystem.getAudioInputStream(AudioFormat.Encoding.PCM_SIGNED, AudioSystem.getAudioInputStream(song));
			final AudioFormat audioFormat = in.getFormat();
			@SuppressWarnings("ObjectAllocationInLoop") final SourceDataLine line = (SourceDataLine) AudioSystem.getLine(new DataLine.Info(SourceDataLine.class, audioFormat));
			line.open(audioFormat);
			final FloatControl gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
			line.start();
			final long songLaenge = song.length();
			final int sampleSizeInBits = audioFormat.getSampleSizeInBits();

			if (in.getFrameLength() == -1) {
				songTime.setTime((songLaenge / sampleSizeInBits) - Sound.timeZoneKorrektur);
			} else {
				songTime.setTime(((in.getFrameLength() / (long) audioFormat.getFrameRate()) * 1000) - Sound.timeZoneKorrektur);
			}

			in.mark(in.available());
			long resetKorrektur = 0;
			while ((!stop)) {
				isPlaying = true;
				final int gainLevel = (int) ((int) gainControl.getMinimum() + (((gainControl.getMaximum() - gainControl.getMinimum()) / 100) * gainPercent));
				gainControl.setValue(gainLevel);
				if (!pause) {
					final int n = in.read(Sound.buffer, 0, Sound.buffer.length);
					if ((n < 0) || (stop)) {
						break;
					}
					if (reset) {
						resetKorrektur = line.getMicrosecondPosition() / 1000;
						in.reset();
						reset = false;
					}
					time.setTime((line.getMicrosecondPosition() / 1000) - Sound.timeZoneKorrektur - resetKorrektur);
					line.write(Sound.buffer, 0, n);
				}
			}
			line.drain();
			line.close();
			in.close();
		} catch (UnsupportedAudioFileException ignored) {
			System.out.println("nicht unterstütztes Format"); //NON-NLS
		} catch (IOException e) {
			System.out.printf("Datei nicht gefunden %s%n", e); //NON-NLS
		} catch (LineUnavailableException ignored) {
			System.out.println("Soundkartenfehler"); //NON-NLS
		}
		isPlaying = false;
	}

	/**
	 * Name und Pfad der Sounddatei
	 *
	 * @param song String
	 */

	public void setSong(final String song)
	{
		this.song = new File(song);
	}

	/**
	 * Name und Pfad der Sounddatei
	 *
	 * @return File song
	 */
	public File getSong()
	{
		return song;
	}

	/**
	 * die Gesamtlänge des Musikstücks
	 *
	 * @return Time songTime
	 */
	public Time getSongTime()
	{
		return (Time) songTime.clone();
	}

	/**
	 * setzt den aktuellen Titel zurück
	 *
	 * @param reset boolean
	 */

	public void reset(final boolean reset)
	{
		this.reset = reset;
	}

	/**
	 * gibt zurück, ob gerade ein Titel abgespielt wird
	 *
	 * @return boolean isPlaying
	 */
	public Boolean isPlaying()
	{
		return isPlaying;
	}

	/**
	 * Titel pausieren
	 *
	 * @param pause boolean
	 */
	public void setPause(final boolean pause)
	{
		this.pause = pause;
	}

	/**
	 * gibt zurück, ob der Titel pausiert
	 *
	 * @return boolean isPause
	 */
	public boolean isPause()
	{
		return pause;
	}

	/**
	 * schaltet die Wiedergabe stumm
	 *
	 * @param mute boolean
	 */
	public void setMute(final boolean mute)
	{
		if ((mute) && (!this.mute)) {
			lautstaerke = getVolume();
			setVolumen(0);
		} else {
			setVolumen(lautstaerke);
		}
		this.mute = mute;
	}

	/**
	 * gibt zurück, ob die Wiedergabe stumm geschaltet ist
	 *
	 * @return boolean isMute
	 */
	public boolean isMute()
	{
		return mute;
	}
}
