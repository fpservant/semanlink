package net.semanlink.util.xml;

/*
	====================================================================
	Copyright (c) 1999-2000 ChannelPoint, Inc..  All rights reserved.
	====================================================================
	
	Redistribution and use in source and binary forms, with or without 
	modification, are permitted provided that the following conditions 
	are met:
	
	1. Redistribution of source code must retain the above copyright 
	notice, this list of conditions and the following disclaimer. 
	
	2. Redistribution in binary form must reproduce the above copyright
	notice, this list of conditions and the following disclaimer in the 
	documentation and/or other materials provided with the distribution.
	
	3. All advertising materials mentioning features or use of this 
	software must display the following acknowledgment:  "This product 
	includes software developed by ChannelPoint, Inc. for use in the 
	Merlot XML Editor (http://www.merlotxml.org/)."
	
	4. Any names trademarked by ChannelPoint, Inc. must not be used to 
	endorse or promote products derived from this software without prior
	written permission. For written permission, please contact
	legal@channelpoint.com.
	
	5.  Products derived from this software may not be called "Merlot"
	nor may "Merlot" appear in their names without prior written
	permission of ChannelPoint, Inc.
	
	6. Redistribution of any form whatsoever must retain the following
	acknowledgment:  "This product includes software developed by 
	ChannelPoint, Inc. for use in the Merlot XML Editor 
	(http://www.merlotxml.org/)."
	
	THIS SOFTWARE IS PROVIDED BY CHANNELPOINT, INC. "AS IS" AND ANY EXPRESSED OR 
	IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
	MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO 
	EVENT SHALL CHANNELPOINT, INC. OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
	INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
	(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
	LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
	ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
	(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
	THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
	====================================================================
	
	For more information on ChannelPoint, Inc. please see http://www.channelpoint.com.  
	For information on the Merlot project, please see 
	http://www.merlotxml.org/.
*/

import java.io.*;
import java.util.*;

public abstract class EncodingMap {
	protected static Map _java2xml = new HashMap();
	protected static Map _xml2java = new HashMap();

	static {
		addEncoding("ISO8859_1", "ISO-8859-1");
		addEncoding("UTF8", "UTF-8");
		addEncoding("UTF16", "UTF-16");
		addEncoding("CP1252", "ISO-8859-1");
		addEncoding("CP1250", "windows-1250");
		addEncoding("ISO8859_1", "ISO-8859-1");
		addEncoding("ISO8859_2", "ISO-8859-2");
		addEncoding("ISO8859_3", "ISO-8859-3");
		addEncoding("ISO8859_4", "ISO-8859-4");
		addEncoding("ISO8859_5", "ISO-8859-5");
		addEncoding("ISO8859_6", "ISO-8859-6");
		addEncoding("ISO8859_7", "ISO-8859-7");
		addEncoding("ISO8859_8", "ISO-8859-8");
		addEncoding("ISO8859_9", "ISO-8859-9");
		addEncoding("JIS", "ISO-2022-JP");
		addEncoding("SJIS", "Shift_JIS");
		addEncoding("EUCJIS", "EUC-JP");
		addEncoding("GB2312", "GB2312");
		addEncoding("BIG5", "Big5");
		addEncoding("KSC5601", "EUC-KR");
		addEncoding("ISO2022KR", "ISO-2022-KR");
		addEncoding("KOI8_R", "KOI8-R");
		addEncoding("CP037", "EBCDIC-CP-US");
		addEncoding("CP037", "EBCDIC-CP-CA");
		addEncoding("CP037", "EBCDIC-CP-NL");
		addEncoding("CP277", "EBCDIC-CP-DK");
		addEncoding("CP277", "EBCDIC-CP-NO");
		addEncoding("CP278", "EBCDIC-CP-FI");
		addEncoding("CP278", "EBCDIC-CP-SE");
		addEncoding("CP280", "EBCDIC-CP-IT");
		addEncoding("CP284", "EBCDIC-CP-ES");
		addEncoding("CP285", "EBCDIC-CP-GB");
		addEncoding("CP297", "EBCDIC-CP-FR");
		addEncoding("CP420", "EBCDIC-CP-AR1");
		addEncoding("CP424", "EBCDIC-CP-HE");
		addEncoding("CP500", "EBCDIC-CP-CH");
		addEncoding("CP870", "EBCDIC-CP-ROECE");
		addEncoding("CP870", "EBCDIC-CP-YU");
		addEncoding("CP871", "EBCDIC-CP-IS");
		addEncoding("CP918", "EBCDIC-CP-AR2");
	}

	private EncodingMap(){}
	
	public static void addEncoding(String java, String xml) {
		_java2xml.put(java, xml);
		_xml2java.put(xml, java);
	}

	public static String getJavaFromXML(String xml) {
		//   System.out.println("XML String = " + xml);
		// Default encoding is UTF-8
		if (xml == null) {
			//    System.out.println("Using Default Encoding");
			xml = "UTF-8";
		}
		xml = xml.toUpperCase();
		String s = (String) _xml2java.get(xml);
		if (s == null) {
			s = xml;
		}
		return s;
	}

	public static String getXMLFromJava(String java) {
		java = java.toUpperCase();
		String s = (String) _java2xml.get(java);
		if (s == null) {
			s = java;
		}
		return s;
	}

	/**
	 * @return the java default encoding
	 */
	public static String getJavaDefaultEncoding() {
		return new InputStreamReader(new ByteArrayInputStream(new byte[0])).getEncoding();
	}
}
