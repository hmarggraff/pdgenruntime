// This software may be used as allowed by the Gnu Affero General Public License. Details are in the file LICENSE, that must be included in the distribution of ths software.
package org.pdgen.util;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Send
{
	String smtpServer;
	String receiver;
	String sender;
	String subject;
	ArrayList<Object> parts = new ArrayList<Object>();
	BufferedReader in;
	PrintStream out;
	ZipOutputStream zip;
	ByteArrayOutputStream zipBuffer;
	int byteNumbering = 1;
	static final String boundary = "-------------------------- Pdgen Exception Feedback Separator";

	public Send(String smtpServer, String receiver, String sender, String subject, String mainPart)
	{
		this.smtpServer = smtpServer;
		this.receiver = '<' + receiver + '>';
		this.sender = '<' + sender + '>';
		this.subject = subject;
		parts.add(mainPart);
	}

	public String getSmtpServer()
	{
		return smtpServer;
	}

	public void setSmtpServer(String smtpServer)
	{
		this.smtpServer = smtpServer;
	}

	public String getReceiver()
	{
		return receiver;
	}

	public void setReceiver(String receiver)
	{
		this.receiver = receiver;
	}

	public void send() throws Exception
	{
		Socket s = null;
		try
		{
			s = new Socket(smtpServer, 25);
			out = new PrintStream(s.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			expect("220", "greetings");
			p2("HELO ", "helohost");
			expect("250", "helo");
            p2("MAIL FROM: ", sender);
			expect("250", "mail from");
			p2("RCPT TO: ", receiver);
			expect("250", "rcpt to");
			out.println("DATA");
			expect("354", "data");
			p2("From: ", sender);
			p2("To:", receiver);
			p2("Subject: ", subject);
			if (parts.size() > 1 || !(parts.get(0) instanceof String))
			{
				out.println("Mime-Version: 1.0");
				out.println("Content-Type: multipart/mixed; boundary=\"" + boundary + "\"");
				//out.println("Content-Transfer-Encoding: 7bit");
				out.println();
				out.println("This is a multi-part message in MIME format.");
				for (ListIterator<Object> iterator = parts.listIterator(); iterator.hasNext();)
				{
					Object e = iterator.next();
					if (e instanceof String)
					{
						out.println("--" + boundary);
						String str = (String)e;
						//out.println("Content-Type: text/plain; charset=us-ascii");
						if (str.startsWith("<HTML>"))
							out.println("Content-Type: text/html; charset=ISO-8859-1");
						else
							out.println("Content-Type: text/plain; charset=ISO-8859-1");
						out.println("Content-Transfer-Encoding: 7bit");
						out.println();
						out.println((String)e);
						//out.println();
						//out.println();
					}
					else if (e instanceof BufferedImage)
					{
						out.println("--" + boundary);
						out.println("Content-Type: image/png; name=\"i" + iterator.nextIndex() + ".png\"");
						out.println("Content-Transfer-Encoding: base64");
						out.println("Content-Description: screenshot" + iterator.nextIndex());
						out.println("Content-Disposition: inline; filename=\"i" + iterator.nextIndex() + ".png\"");
						out.println();
						byte[] pngbytes = ConvertImageToByteArray.convertImage((BufferedImage)e);
						sendBytes(pngbytes);
						//out.println();
						//out.println();
					}
					else if (e instanceof File)
					{
						zipFile((File)e);
					}
					else if (e instanceof NamedObject)
					{
						zipBytes((NamedObject)e);
					}
                    else if (e instanceof TypedEntry)
                    {
                        TypedEntry te = (TypedEntry) e;
                        out.println("--" + boundary);
                        out.println("Content-Type: "+te.contentType+"; name=\""+te.name+"\"");
                        out.println("Content-Transfer-Encoding: base64");
                        out.println("Content-Description: "+te.name);
                        out.println("Content-Disposition: inline; filename=\"" + te.name + "\"");
                        out.println();
                        sendBytes(te.data);
                    }
				}
				if (zip != null) // files have been attached
				{
					// send attachment info...
					out.println("--" + boundary);
					out.println("Content-Type: application/x-zip-compressed; name=\"files.zip\"");
					out.println("Content-Transfer-Encoding: base64");
					out.println("Content-Description: files.zip");
					out.println("Content-Disposition: inline; filename=\"files.zip\"");
//					out.println("Content-Disposition: attachment; filename=\"files.zip\"");
					out.println();
					//zip.finish();
					zip.close();
					// Send zipped parts base64 encoded...
					sendBytes(zipBuffer.toByteArray());
				}
				//out.println();
				out.println("--" + boundary + "--");
				//out.println();
			}
			else
			{
				String str = (String)parts.get(0);
				if (str.startsWith("<HTML>"))
				{
					out.println("Content-Type: text/html; charset=ISO-8859-1");
					out.println("Content-Transfer-Encoding: 7bit");
				}
				out.println();
				out.println(str);
			}
			out.println();
			out.println(".");
			expect("250", "end of data");
			out.println("QUIT");
			expect("221", "quit");
		}
		finally
		{
			if (out != null)
				out.close();
			if (s != null)
				s.close();
		}
	}

	void zipFile(File f) throws IOException
	{
		if (zip == null)
		{
			zipBuffer = new ByteArrayOutputStream();
			zip = new ZipOutputStream(zipBuffer);
		}
		String name = f.getName();
		ZipEntry e = new ZipEntry(name);
		e.setTime(f.lastModified());
		if (f.length() == 0)
		{
			e.setMethod(ZipEntry.STORED);
			e.setSize(0);
			e.setCrc(0);
		}
		zip.putNextEntry(e);
		byte[] buf = new byte[1024];
		int len;
		InputStream is = new BufferedInputStream(new FileInputStream(f));
		while ((len = is.read(buf, 0, buf.length)) != -1)
		{
			zip.write(buf, 0, len);
		}
		is.close();
		zip.closeEntry();
	}

	void zipBytes(NamedObject f) throws IOException
	{
		if (f.obj == null)
			return;
		if (zip == null)
		{
			zipBuffer = new ByteArrayOutputStream();
			zip = new ZipOutputStream(zipBuffer);
		}
		ZipEntry e = new ZipEntry(f.name);
		zip.putNextEntry(e);
		zip.write((byte[])f.obj);
		zip.closeEntry();
	}

	public void addTextPart(String txt)
	{
		parts.add(txt);
	}

	public void attachImage(BufferedImage data)
	{
		parts.add(data);
	}

	public void attachFile(File f)
	{
		parts.add(f);
	}

	public void attachBytes(byte[] bytes, String name)
	{
		parts.add(new NamedObject(name, bytes));
	}

    public void attachContent(byte[] bytes, String name, String contentType)
    {
        parts.add(new TypedEntry(bytes, name, contentType));
    }

	void expect(String expected, String msg) throws Exception
	{
		String lastline = in.readLine();
		if (!lastline.startsWith(expected))
			throw new Exception(msg + ":" + lastline);
		while (lastline.startsWith(expected + "-"))
			lastline = in.readLine();
	}

	void p2(String tag, String txt)
	{
		out.print(tag);
		out.println(txt);
	}

	static char[] BaseTable = {
		'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P',
		'Q','R','S','T','U','V','W','X','Y','Z','a','b','c','d','e','f',
		'g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v',
		'w','x','y','z','0','1','2','3','4','5','6','7','8','9','+','/'
	};

	void sendBytes(byte[] bytes) throws Exception
	{
		byte[] buf = new byte[4];
		int n = bytes.length;
		int n3byt = n / 3;
		int k = n3byt * 3;
		int nrest = n % 3;
		int linelength = 0;
		int i = 0;
		while (i < k)
		{
			buf[0] = (byte)((bytes[i] & 0xFC) >> 2);
			buf[1] = (byte)((bytes[i] & 0x03) << 4 | (bytes[i + 1] & 0xF0) >> 4);
			buf[2] = (byte) ((bytes[i + 1] & 0x0F) << 2 | (bytes[i + 2] & 0xC0) >> 6);
			buf[3] = (byte)(bytes[i + 2] & 0x3F);
			out.print(BaseTable[buf[0]]);
			out.print(BaseTable[buf[1]]);
			out.print(BaseTable[buf[2]]);
			out.print(BaseTable[buf[3]]);
			if ((linelength += 4) >= 76)
			{
				out.println();
				linelength = 0;
			}
			i += 3;
		}
		if (nrest == 2)
		{
			buf[0] = (byte)((bytes[k] & 0xFC) >> 2);
			buf[1] = (byte)((bytes[k] & 0x03) << 4 | (bytes[k + 1] & 0xF0) >> 4);
			buf[2] = (byte)((bytes[k + 1] & 0x0F) << 2);
			out.print(BaseTable[buf[0]]);
			out.print(BaseTable[buf[1]]);
			out.print(BaseTable[buf[2]]);
			out.print('=');
		}
		else if (nrest == 1)
		{
			buf[0] = (byte)((bytes[k] & 0xFC) >> 2);
			buf[1] = (byte)((bytes[k] & 0x03) << 4);
			out.print(BaseTable[buf[0]]);
			out.print(BaseTable[buf[1]]);
			out.print('=');
			out.print('=');
		}
		out.println();
		out.flush();
	}

	public static void main(String[] args)
	{
		long t = (System.currentTimeMillis() / 1000) % 86400;
		String tt = "at " + (t / 3600) + ":" + (t % 3600) / 60 + ":" + (t % 60);
		Send s = new Send("mail.pdgen.org", "support@pdgen.org", "support@pdgen.org", "Test Send " + tt, tt);
		//s.addTextPart("Hello " + tt);
		//s.addTextPart("Part2");
		Properties p = System.getProperties();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			store(p, bos, "System Properties");
			s.attachBytes(bos.toByteArray(), "System_Properties.txt");
			s.send();
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
		}
	}
	protected static void store(Properties p, ByteArrayOutputStream out, String header)
	{
		PrintWriter awriter;
		awriter = new PrintWriter(out);
		System.out.println("got store printer");
		if (header != null)
			awriter.println("#" + header);
		awriter.println("#" + new Date());
		System.out.println("done store header");
		for (Enumeration<Object> e = p.keys(); e.hasMoreElements();)
		{
			String key = (String) e.nextElement();
			String val = p.getProperty(key);
			//key = saveConvert(key, true);

			/* No need to escape embedded and trailing spaces for value, hence
			 * pass false to flag.
			 */
			//val = saveConvert(val, false);
			awriter.println(key + "=" + val);
		}
		awriter.flush();

	}

    protected static class TypedEntry
    {
        String name;
        String contentType;
        byte[] data;
        protected TypedEntry(byte[] data, String name, String contentType)
        {
            this.data = data;
            this.name = name;
            this.contentType = contentType;
        }
    }
}
