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

package org.chaosfisch.youtubeuploader.plugins.socializeplugin.services;/*
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

import org.chaosfisch.google.request.HTTP_STATUS;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings({"CallToStringEquals", "StringToUpperCaseOrToLowerCaseWithoutLocale", "HardCodedStringLiteral", "MagicCharacter", "UseOfStringTokenizer", "IOResourceOpenedButNotSafelyClosed"})
public class NanoHTTPD
{
	// ==================================================
	// API parts
	// ==================================================

	/**
	 * Override this to customize the server.<p>
	 * <p/>
	 * (By default, this delegates to serveFile() and allows directory listing.)
	 *
	 * @param uri    Percent-decoded URI without parameters, for example "/index.cgi"
	 * @param method "GET", "POST" etc.
	 * @param parms  Parsed, percent decoded parameters from URI and, in case of POST, data.
	 * @param header Header entries, percent decoded
	 * @return HTTP response, see class Response for details
	 */
	public Response serve(final String uri, final String method, final Properties header, final Properties parms, final Properties files)
	{
		NanoHTTPD.myOut.printf("%s '%s' %n", method, uri);

		Enumeration<?> e = header.propertyNames();
		while (e.hasMoreElements()) {
			final String value = (String) e.nextElement();
			NanoHTTPD.myOut.printf("  HDR: '%s' = '%s'%n", value, header.getProperty(value));
		}
		e = parms.propertyNames();
		while (e.hasMoreElements()) {
			final String value = (String) e.nextElement();
			NanoHTTPD.myOut.printf("  PRM: '%s' = '%s'%n", value, parms.getProperty(value));
		}
		e = files.propertyNames();
		while (e.hasMoreElements()) {
			final String value = (String) e.nextElement();
			NanoHTTPD.myOut.printf("  UPLOADED: '%s' = '%s'%n", value, files.getProperty(value));
		}

		return serveFile(uri, header, myRootDir, true);
	}

	/**
	 * Common mime types for dynamic content
	 */
	public static final String MIME_PLAINTEXT      = "text/plain";
	public static final String MIME_HTML           = "text/html";
	public static final String MIME_DEFAULT_BINARY = "application/octet-stream";
	public static final String MIME_XML            = "text/xml";

	// ==================================================
	// Socket & server code
	// ==================================================

	/**
	 * Starts a HTTP server to given port.<p>
	 * Throws an IOException if the socket is already in use
	 */
	public NanoHTTPD(final int port, final File wwwroot)
	{
		myRootDir = wwwroot;
		myThread = new Thread(new Runnable()
		{
			public void run()
			{

				try {
					//noinspection SocketOpenedButNotSafelyClosed
					myServerSocket = new ServerSocket(port);
					final Socket socket = myServerSocket.accept();

					try {
						do {
							//noinspection ObjectAllocationInLoop
							new HTTPSession(socket);
						} while (socket.isBound());
					} finally {
						try {
							myServerSocket.close();
							socket.close();
						} catch (IOException ignored) {
						}
					}
				} catch (IOException ignored) {
				}
			}
		});
		myThread.setDaemon(true);
		myThread.start();
	}

	/**
	 * Stops the server.
	 */

	public void stop()
	{
		try {
			myServerSocket.close();
			myThread.join();
		} catch (IOException ignored) {
			throw new RuntimeException("This shouldn't happen");
		} catch (InterruptedException ignored) {
			throw new RuntimeException("This shouldn't happen");
		}
	}

	/**
	 * Handles one session, i.e. parses the HTTP request
	 * and returns the response.
	 */
	@SuppressWarnings({"CallToStringEqualsIgnoreCase", "StringToUpperCaseOrToLowerCaseWithoutLocale", "HardCodedStringLiteral", "MagicCharacter", "UseOfStringTokenizer", "IOResourceOpenedButNotSafelyClosed"})
	private class HTTPSession implements Runnable
	{
		public HTTPSession(final Socket s)
		{
			mySocket = s;
			final Thread t = new Thread(this);
			t.setDaemon(true);
			t.start();
		}

		public void run()
		{
			try {
				final InputStream is = mySocket.getInputStream();
				if (is == null) {
					return;
				}

				// Read the first 8192 bytes.
				// The full header should fit in here.
				// Apache's default header limit is 8KB.
				final int bufsize = 8192;
				byte[] buf = new byte[bufsize];
				int rlen = is.read(buf, 0, bufsize);
				if (rlen <= 0) {
					return;
				}

				// Create a BufferedReader for parsing the header.
				final ByteArrayInputStream hbis = new ByteArrayInputStream(buf, 0, rlen);
				final BufferedReader hin = new BufferedReader(new InputStreamReader(hbis));
				final Properties pre = new Properties();
				final Properties parms = new Properties();
				final Properties header = new Properties();
				final Properties files = new Properties();

				// Decode the header into parms and header java properties
				decodeHeader(hin, pre, parms, header);
				final String method = pre.getProperty("method");
				final String uri = pre.getProperty("uri");

				long size = 0x7fffffffffffffffL;
				final String contentLength = header.getProperty("content-length");
				if (contentLength != null) {
					try {
						size = Integer.parseInt(contentLength);
					} catch (NumberFormatException ignored) {
						throw new RuntimeException("This shouldn't happen");
					}
				}

				// We are looking for the byte separating header from body.
				// It must be the last byte of the first two sequential new lines.
				int splitbyte = 0;
				boolean sbfound = false;
				while (splitbyte < rlen) {
					if ((buf[splitbyte] == '\r') && (buf[++splitbyte] == '\n') && (buf[++splitbyte] == '\r') && (buf[++splitbyte] == '\n')) {
						sbfound = true;
						break;
					}
					splitbyte++;
				}
				splitbyte++;

				// Write the part of body already read to ByteArrayOutputStream f
				final ByteArrayOutputStream f = new ByteArrayOutputStream();
				if (splitbyte < rlen) {
					f.write(buf, splitbyte, rlen - splitbyte);
				}

				// While Firefox sends on the first read all the data fitting
				// our buffer, Chrome and Opera sends only the headers even if
				// there is data for the body. So we do some magic here to find
				// out whether we have already consumed part of body, if we
				// have reached the end of the data to be sent or we should
				// expect the first byte of the body at the next read.
				if (splitbyte < rlen) {
					size -= (rlen - splitbyte) + 1;
				} else if (!sbfound || (size == 0x7FFFFFFFFFFFFFFFl)) {
					size = 0;
				}

				// Now read all the body and write it to f
				buf = new byte[512];
				while ((rlen >= 0) && (size > 0)) {
					rlen = is.read(buf, 0, 512);
					size -= rlen;
					if (rlen > 0) {
						f.write(buf, 0, rlen);
					}
				}

				// Get the raw body as a byte []
				final byte[] fbuf = f.toByteArray();

				// Create a BufferedReader for easily reading it as string.
				final ByteArrayInputStream bin = new ByteArrayInputStream(fbuf);
				final BufferedReader in = new BufferedReader(new InputStreamReader(bin));

				// If the method is POST, there may be parameters
				// in data section, too, read it:
				if (method.equalsIgnoreCase("POST")) {
					String contentType = "";
					final String contentTypeHeader = header.getProperty("content-type");
					StringTokenizer st = new StringTokenizer(contentTypeHeader, "; ");
					if (st.hasMoreTokens()) {
						contentType = st.nextToken();
					}

					if (contentType.equalsIgnoreCase("multipart/form-data")) {
						// Handle multipart/form-data
						if (!st.hasMoreTokens()) {
							sendError(HTTP_STATUS.BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but boundary missing. Usage: GET /example/file.html");
						}
						final String boundaryExp = st.nextToken();
						st = new StringTokenizer(boundaryExp, "=");
						if (st.countTokens() != 2) {
							sendError(HTTP_STATUS.BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but boundary syntax error. Usage: GET /example/file.html");
						}
						st.nextToken();
						final String boundary = st.nextToken();

						decodeMultipartData(boundary, fbuf, in, parms, files);
					} else {
						// Handle application/x-www-form-urlencoded
						String postLine = "";
						final char[] pbuf = new char[512];
						int read = in.read(pbuf);
						while ((read >= 0) && !postLine.endsWith("\r\n")) {
							postLine += String.valueOf(pbuf, 0, read);
							read = in.read(pbuf);
						}
						postLine = postLine.trim();
						decodeParms(postLine, parms);
					}
				}

				if (method.equalsIgnoreCase("PUT")) {
					files.put("content", saveTmpFile(fbuf, 0, f.size()));
				}

				// Ok, now do the serve()
				final Response r = serve(uri, method, header, parms, files);
				sendResponse(r.status, r.mimeType, r.header, r.data);

				in.close();
				is.close();
			} catch (IOException ioe) {
				try {
					sendError(HTTP_STATUS.INTERNALERROR.toString(), String.format("SERVER INTERNAL ERROR: IOException: %s", ioe.getMessage()));
				} catch (InterruptedException ignored) {
				}
			} catch (InterruptedException ignored) {
				// Thrown by sendError, ignore and exit the thread.
			}
		}

		/**
		 * Decodes the sent headers and loads the data into
		 * java Properties' key - value pairs
		 */
		private void decodeHeader(final BufferedReader in, final Properties pre, final Properties parms, final Properties header) throws InterruptedException
		{
			try {
				// Read the request line
				final String inLine = in.readLine();
				if (inLine == null) {
					return;
				}
				final StringTokenizer st = new StringTokenizer(inLine);
				if (!st.hasMoreTokens()) {
					sendError(HTTP_STATUS.BADREQUEST.toString(), "BAD REQUEST: Syntax error. Usage: GET /example/file.html");
				}

				final String method = st.nextToken();
				pre.put("method", method);

				if (!st.hasMoreTokens()) {
					sendError(HTTP_STATUS.BADREQUEST.toString(), "BAD REQUEST: Missing URI. Usage: GET /example/file.html");
				}

				String uri = st.nextToken();

				// Decode parameters from the URI
				final int qmi = uri.indexOf('?');
				if (qmi >= 0) {
					decodeParms(uri.substring(qmi + 1), parms);
					uri = decodePercent(uri.substring(0, qmi));
				} else {
					uri = decodePercent(uri);
				}

				// If there's another token, it's protocol version,
				// followed by HTTP headers. Ignore version but parse headers.
				// NOTE: this now forces header names lowercase since they are
				// case insensitive and vary by client.
				if (st.hasMoreTokens()) {
					String line = in.readLine();
					while ((line != null) && !line.trim().isEmpty()) {
						final int p = line.indexOf(':');
						if (p >= 0) {
							header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
						}
						line = in.readLine();
					}
				}

				pre.put("uri", uri);
			} catch (IOException ioe) {
				sendError(HTTP_STATUS.INTERNALERROR.toString(), String.format("SERVER INTERNAL ERROR: IOException: %s", ioe.getMessage()));
			}
		}

		/**
		 * Decodes the Multipart Body data and put it
		 * into java Properties' key - value pairs.
		 */
		private void decodeMultipartData(final String boundary, final byte[] fbuf, final BufferedReader in, final Properties parms, final Properties files) throws InterruptedException
		{
			try {
				final int[] bpositions = getBoundaryPositions(fbuf, boundary.getBytes());
				int boundarycount = 1;
				String mpline = in.readLine();
				while (mpline != null) {
					if (!mpline.contains(boundary)) {
						sendError(HTTP_STATUS.BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but next chunk does not start with boundary. Usage: GET /example/file.html");
					}
					boundarycount++;
					@SuppressWarnings("ObjectAllocationInLoop") final Properties item = new Properties();
					mpline = in.readLine();
					while ((mpline != null) && !mpline.trim().isEmpty()) {
						final int p = mpline.indexOf(':');
						if (p != -1) {
							item.put(mpline.substring(0, p).trim().toLowerCase(), mpline.substring(p + 1).trim());
						}
						mpline = in.readLine();
					}
					if (mpline != null) {
						final String contentDisposition = item.getProperty("content-disposition");
						if (contentDisposition == null) {
							sendError(HTTP_STATUS.BADREQUEST.toString(), "BAD REQUEST: Content type is multipart/form-data but no content-disposition info found. Usage: GET /example/file.html");
						}
						@SuppressWarnings("ObjectAllocationInLoop") final StringTokenizer st = new StringTokenizer(contentDisposition, "; ");
						@SuppressWarnings("ObjectAllocationInLoop") final Properties disposition = new Properties();
						while (st.hasMoreTokens()) {
							final String token = st.nextToken();
							final int p = token.indexOf('=');
							if (p != -1) {
								disposition.put(token.substring(0, p).trim().toLowerCase(), token.substring(p + 1).trim());
							}
						}
						String pname = disposition.getProperty("title");
						pname = pname.substring(1, pname.length() - 1);

						String value = "";
						if (item.getProperty("content-type") == null) {
							while ((mpline != null) && !mpline.contains(boundary)) {
								mpline = in.readLine();
								if (mpline != null) {
									final int d = mpline.indexOf(boundary);
									if (d == -1) {
										value += mpline;
									} else {
										value += mpline.substring(0, d - 2);
									}
								}
							}
						} else {
							if (boundarycount > bpositions.length) {
								sendError(HTTP_STATUS.INTERNALERROR.toString(), "Error processing request");
							}
							final int offset = stripMultipartHeaders(fbuf, bpositions[boundarycount - 2]);
							final String path = saveTmpFile(fbuf, offset, bpositions[boundarycount - 1] - offset - 4);
							files.put(pname, path);
							value = disposition.getProperty("filename");
							value = value.substring(1, value.length() - 1);
							do {
								mpline = in.readLine();
							} while ((mpline != null) && !mpline.contains(boundary));
						}
						parms.put(pname, value);
					}
				}
			} catch (IOException ioe) {
				sendError(HTTP_STATUS.INTERNALERROR.toString(), String.format("SERVER INTERNAL ERROR: IOException: %s", ioe.getMessage()));
			}
		}

		/**
		 * Find the byte positions where multipart boundaries start.
		 */
		public int[] getBoundaryPositions(final byte[] b, final byte... boundary)
		{
			int matchcount = 0;
			int matchbyte = -1;
			final Vector<Integer> matchbytes = new Vector<Integer>(b.length);
			for (int i = 0; i < b.length; i++) {
				if (b[i] == boundary[matchcount]) {
					if (matchcount == 0) {
						matchbyte = i;
					}
					matchcount++;
					if (matchcount == boundary.length) {
						//noinspection unchecked
						matchbytes.addElement(matchbyte);
						matchcount = 0;
						matchbyte = -1;
					}
				} else {
					matchcount = 0;
					matchbyte = -1;
				}
			}
			final int[] ret = new int[matchbytes.size()];
			for (int i = 0; i < ret.length; i++) {
				ret[i] = matchbytes.elementAt(i);
			}
			return ret;
		}

		/**
		 * Retrieves the content of a sent file and saves it
		 * to a temporary file.
		 * The full path to the saved file is returned.
		 */
		private String saveTmpFile(final byte[] b, final int offset, final int len)
		{
			String path = "";
			if (len > 0) {
				final String tmpdir = System.getProperty("java.io.tmpdir");
				try {
					final File temp = File.createTempFile("org.chaosfisch.youtubeuploader.plugins.socializeplugin.services.NanoHTTPD", "", new File(tmpdir));
					final FileOutputStream fileOutputStream = new FileOutputStream(temp);
					final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
					try {
						bufferedOutputStream.write(b, offset, len);
						path = temp.getAbsolutePath();
						return path;
					} catch (IOException e) {
						System.err.printf("Error: %s%n", e.getMessage());
					} finally {
						try {
							bufferedOutputStream.close();
							fileOutputStream.close();
						} catch (IOException ignored) {
							throw new RuntimeException("This shouldn't happen");
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				} catch (IOException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
			return path;
		}

		/**
		 * It returns the offset separating multipart file headers
		 * from the file's data.
		 */
		private int stripMultipartHeaders(final byte[] b, final int offset)
		{
			int i;
			for (i = offset; i < b.length; i++) {
				if ((b[i] == '\r') && (b[++i] == '\n') && (b[++i] == '\r') && (b[++i] == '\n')) {
					break;
				}
			}
			return i + 1;
		}

		/**
		 * Decodes the percent encoding scheme. <br/>
		 * For example: "an+example%20string" -> "an example string"
		 */
		private String decodePercent(final String str) throws InterruptedException
		{
			try {
				@SuppressWarnings("StringBufferWithoutInitialCapacity") final StringBuilder sb = new StringBuilder();
				for (int i = 0; i < str.length(); i++) {
					final char c = str.charAt(i);
					switch (c) {
						case '+':
							sb.append(' ');
							break;
						case '%':
							sb.append((char) Integer.parseInt(str.substring(i + 1, i + 3), 16));
							i += 2;
							break;
						default:
							sb.append(c);
							break;
					}
				}
				return sb.toString();
			} catch (NumberFormatException ignored) {
				sendError(HTTP_STATUS.BADREQUEST.toString(), "BAD REQUEST: Bad percent-encoding.");
				return null;
			}
		}

		/**
		 * Decodes parameters in percent-encoded URI-format
		 * ( e.g. "title=Jack%20Daniels&pass=Single%20Malt" ) and
		 * adds them to given Properties. NOTE: this doesn't support multiple
		 * identical keys due to the simplicity of Properties -- if you need multiples,
		 * you might want to replace the Properties with a Hashtable of Vectors or such.
		 */
		private void decodeParms(final String parms, final Properties p) throws InterruptedException
		{
			if (parms == null) {
				return;
			}

			final StringTokenizer st = new StringTokenizer(parms, "&");
			while (st.hasMoreTokens()) {
				final String e = st.nextToken();
				final int sep = e.indexOf('=');
				if (sep >= 0) {
					p.put(decodePercent(e.substring(0, sep)).trim(), decodePercent(e.substring(sep + 1)));
				}
			}
		}

		/**
		 * Returns an error message as a HTTP response and
		 * throws InterruptedException to stop further request processing.
		 */
		private void sendError(final String status, final String msg) throws InterruptedException
		{
			sendResponse(status, NanoHTTPD.MIME_PLAINTEXT, null, new ByteArrayInputStream(msg.getBytes()));
			throw new InterruptedException("InterruptedException");
		}

		/**
		 * Sends given response to the socket.
		 */
		private void sendResponse(final String status, final String mime, final Properties header, final InputStream data)
		{
			try {
				if (status == null) {
					throw new Error("sendResponse(): Status can't be null.");
				}

				final OutputStream out = mySocket.getOutputStream();
				final PrintWriter pw = new PrintWriter(out);
				pw.print(String.format("HTTP/1.0 %s \r\n", status));

				if (mime != null) {
					pw.print(String.format("Content-Type: %s\r\n", mime));
				}

				if ((header == null) || (header.getProperty("Date") == null)) {
					pw.print(String.format("Date: %s\r\n", NanoHTTPD.gmtFrmt.format(Calendar.getInstance().getTime())));
				}

				if (header != null) {
					for (final Object o : header.keySet()) {
						final String key = (String) o;
						final String value = header.getProperty(key);
						pw.print(String.format("%s: %s\r\n", key, value));
					}
				}

				pw.print("\r\n");
				pw.flush();

				if (data != null) {
					int pending = data.available();    // This is to support partial sends, see serveFile()
					final byte[] buff = new byte[NanoHTTPD.theBufferSize];
					while (pending > 0) {
						final int read = data.read(buff, 0, ((pending > NanoHTTPD.theBufferSize) ? NanoHTTPD.theBufferSize : pending));
						if (read <= 0) {
							break;
						}
						out.write(buff, 0, read);
						pending -= read;
					}
				}
				out.flush();
				out.close();
				if (data != null) {
					data.close();
				}
			} catch (IOException ignored1) {
				// Couldn't write? No can do.
				try {
					mySocket.close();
				} catch (IOException ignored) {
					throw new RuntimeException("This shouldn't happen");
				}
			}
		}

		private final Socket mySocket;
	}

	/**
	 * URL-encodes everything between "/"-characters.
	 * Encodes spaces as '%20' instead of '+'.
	 */
	private String encodeUri(final String uri)
	{
		String newUri = "";
		final StringTokenizer st = new StringTokenizer(uri, "/ ", true);
		while (st.hasMoreTokens()) {
			final String tok = st.nextToken();
			if (tok.equals("/")) {
				newUri += "/";
			} else if (tok.equals(" ")) {
				newUri += "%20";
			} else {
				try {
					newUri += URLEncoder.encode(tok, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
				}
			}
		}
		return newUri;
	}

	private       ServerSocket myServerSocket;
	private final Thread       myThread;
	private final File         myRootDir;

	// ==================================================
	// File server code
	// ==================================================

	/**
	 * Serves file from homeDir and its' subdirectories (only).
	 * Uses only URI, ignores all headers and HTTP parameters.
	 */
	public Response serveFile(String uri, final Properties header, final File homeDir, final boolean allowDirectoryListing)
	{
		Response res = null;

		// Make sure we won't die of an exception later
		if (!homeDir.isDirectory()) {
			res = new Response(HTTP_STATUS.INTERNALERROR.toString(), NanoHTTPD.MIME_PLAINTEXT, "INTERNAL ERRROR: serveFile(): given homeDir is not a directory.");
		}

		if (res == null) {
			// Remove URL arguments
			uri = uri.trim().replace(File.separatorChar, '/');
			if (uri.indexOf('?') >= 0) {
				uri = uri.substring(0, uri.indexOf('?'));
			}

			// Prohibit getting out of current directory
			if (uri.startsWith("..") || uri.endsWith("..") || uri.contains("../")) {
				res = new Response(HTTP_STATUS.FORBIDDEN.toString(), NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Won't serve ../ for security reasons.");
			}
		}

		File f = new File(homeDir, uri);
		if ((res == null) && !f.exists()) {
			res = new Response(HTTP_STATUS.NOTFOUND.toString(), NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
		}

		// List the directory, if necessary
		if ((res == null) && f.isDirectory()) {
			// Browsers get confused without '/' after the
			// directory, send a redirect.
			if (!uri.endsWith("/")) {
				uri += "/";
				res = new Response(HTTP_STATUS.REDIRECT.toString(), NanoHTTPD.MIME_HTML, String.format("<html><body>Redirected: <a href=\"%s\">%s</a></body></html>", uri, uri));
				res.addHeader("Location", uri);
			}

			if (res == null) {
				// First try index.html and index.htm
				if (new File(f, "index.html").exists()) {
					f = new File(homeDir, uri + "/index.html");
				} else if (new File(f, "index.htm").exists()) {
					f = new File(homeDir, uri + "/index.htm");
				}
				// No index file, list the directory if it is readable
				else if (allowDirectoryListing && f.canRead()) {
					final String[] files = f.list();
					String msg = String.format("<html><body><h1>Directory %s</h1><br/>", uri);

					if (uri.length() > 1) {
						final String u = uri.substring(0, uri.length() - 1);
						final int slash = u.lastIndexOf('/');
						if ((slash >= 0) && (slash < u.length())) {
							msg += String.format("<b><a href=\"%s\">..</a></b><br/>", uri.substring(0, slash + 1));
						}
					}

					if (files != null) {
						for (int i = 0; i < files.length; ++i) {
							@SuppressWarnings("ObjectAllocationInLoop") final File curFile = new File(f, files[i]);
							final boolean dir = curFile.isDirectory();
							if (dir) {
								msg += "<b>";
								files[i] += "/";
							}

							msg += String.format("<a href=\"%s\">%s</a>", encodeUri(String.format("%s%s", uri, files[i])), files[i]);

							// Show file size
							if (curFile.isFile()) {
								final long len = curFile.length();
								msg += " &nbsp;<font size=2>(";
								if (len < 1024) {
									msg += len + " bytes";
								} else if (len < (1024 * 1024)) {
									msg += String.format("%d.%d KB", len / 1024, ((len % 1024) / 10) % 100);
								} else {
									msg += String.format("%d.%d MB", len / (1024 * 1024), ((len % (1024 * 1024)) / 10) % 100);
								}

								msg += ")</font>";
							}
							msg += "<br/>";
							if (dir) {
								msg += "</b>";
							}
						}
					}
					msg += "</body></html>";
					res = new Response(HTTP_STATUS.OK.toString(), NanoHTTPD.MIME_HTML, msg);
				} else {
					res = new Response(HTTP_STATUS.FORBIDDEN.toString(), NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: No directory listing.");
				}
			}
		}

		try {
			if (res == null) {
				// Get MIME type from file title extension, if possible
				String mime = null;
				final int dot = f.getCanonicalPath().lastIndexOf('.');
				if (dot >= 0) {
					mime = NanoHTTPD.theMimeTypes.get(f.getCanonicalPath().substring(dot + 1).toLowerCase());
				}
				if (mime == null) {
					mime = NanoHTTPD.MIME_DEFAULT_BINARY;
				}

				// Calculate etag
				final String etag = Integer.toHexString((String.format("%s%d%d", f.getAbsolutePath(), f.lastModified(), f.length())).hashCode());

				// Support (simple) skipping:
				long startFrom = 0;
				long endAt = -1;
				String range = header.getProperty("range");
				if (range != null) {
					if (range.startsWith("bytes=")) {
						range = range.substring("bytes=".length());
						final int minus = range.indexOf('-');
						try {
							if (minus > 0) {
								startFrom = Long.parseLong(range.substring(0, minus));
								endAt = Long.parseLong(range.substring(minus + 1));
							}
						} catch (NumberFormatException ignored) {
							throw new RuntimeException("This shouldn't happen");
						}
					}
				}

				// Change return code and add Content-Range header when skipping is requested
				final long fileLen = f.length();
				if ((range != null) && (startFrom >= 0)) {
					if (startFrom >= fileLen) {
						res = new Response(HTTP_STATUS.RANGE_NOT_SATISFIABLE.toString(), NanoHTTPD.MIME_PLAINTEXT, "");
						res.addHeader("Content-Range", String.format("bytes 0-0/%d", fileLen));
						res.addHeader("ETag", etag);
					} else {
						if (endAt < 0) {
							endAt = fileLen - 1;
						}
						long newLen = (endAt - startFrom) + 1;
						if (newLen < 0) {
							newLen = 0;
						}

						final long dataLen = newLen;
						final FileInputStream fis = new FileInputStream(f)
						{
							public int available() { return (int) dataLen; }
						};
						//noinspection ResultOfMethodCallIgnored
						fis.skip(startFrom);

						res = new Response(HTTP_STATUS.PARTIALCONTENT.toString(), mime, fis);
						res.addHeader("Content-Length", String.format("%d", dataLen));
						res.addHeader("Content-Range", String.format("bytes %d-%d/%d", startFrom, endAt, fileLen));
						res.addHeader("ETag", etag);
					}
				} else {
					if (etag.equals(header.getProperty("if-none-match"))) {
						res = new Response(HTTP_STATUS.NOTMODIFIED.toString(), mime, "");
					} else {
						res = new Response(HTTP_STATUS.OK.toString(), mime, new FileInputStream(f));
						res.addHeader("Content-Length", String.format("%d", fileLen));
						res.addHeader("ETag", etag);
					}
				}
			}
		} catch (FileNotFoundException ignored) {
			res = new Response(HTTP_STATUS.NOTFOUND.toString(), NanoHTTPD.MIME_PLAINTEXT, "NOT FOUND: File not found.");
		} catch (IOException ignored) {
			res = new Response(HTTP_STATUS.FORBIDDEN.toString(), NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}

		res.addHeader("Accept-Ranges", "bytes"); // Announce that the file server accepts partial content requestes
		return res;
	}

	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	@SuppressWarnings("CollectionWithoutInitialCapacity") private static final Hashtable<String, String> theMimeTypes = new Hashtable<String, String>();

	static {
		@SuppressWarnings("StringConcatenation") final StringTokenizer st = new StringTokenizer("css		text/css " +
				                                                                                        "htm		text/html " +
				                                                                                        "html		text/html " +
				                                                                                        "xml		text/xml " +
				                                                                                        "txt		text/plain " +
				                                                                                        "asc		text/plain " +
				                                                                                        "gif		image/gif " +
				                                                                                        "jpg		image/jpeg " +
				                                                                                        "jpeg		image/jpeg " +
				                                                                                        "png		image/png " +
				                                                                                        "mp3		audio/mpeg " +
				                                                                                        "m3u		audio/mpeg-url " +
				                                                                                        "mp4		video/mp4 " +
				                                                                                        "ogv		video/ogg " +
				                                                                                        "flv		video/x-flv " +
				                                                                                        "mov		video/quicktime " +
				                                                                                        "swf		application/x-shockwave-flash " +
				                                                                                        "js			application/javascript " +
				                                                                                        "pdf		application/pdf " +
				                                                                                        "doc		application/msword " +
				                                                                                        "ogg		application/x-ogg " +
				                                                                                        "zip		application/octet-stream " +
				                                                                                        "exe		application/octet-stream " +
				                                                                                        "class		application/octet-stream ");
		while (st.hasMoreTokens()) {
			//noinspection unchecked
			NanoHTTPD.theMimeTypes.put(st.nextToken(), st.nextToken());
		}
	}

	private static final int theBufferSize = 16 * 1024;

	// Change this if you want to log to somewhere else than stdout
	protected static final PrintStream myOut = System.out;

	/**
	 * GMT date formatter
	 */
	private static final SimpleDateFormat gmtFrmt;

	static {
		gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		NanoHTTPD.gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}
}

