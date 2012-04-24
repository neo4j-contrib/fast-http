package org.neo4j.smack.http;

import java.nio.charset.Charset;

import org.jboss.netty.util.CharsetUtil;

public class HttpTokens {

    public static final byte SP = 32;

    //tab ' '
    public static final byte HT = 9;

    /**
     * Carriage return
     */
    public static final byte CR = 13;

    /**
     * Equals '='
     */
    public static final byte EQUALS = 61;

    /**
     * Line feed character
     */
    public static final byte LF = 10;

    /**
     * carriage return line feed
     */
    public static final byte[] CRLF = new byte[] { CR, LF };

    /**
    * Colon ':'
    */
    public static final byte COLON = 58;

    /**
    * Semicolon ';'
    */
    public static final byte SEMICOLON = 59;

     /**
    * comma ','
    */
    public static final byte COMMA = 44;

    public static final byte DOUBLE_QUOTE = '"';

    public static final Charset DEFAULT_CHARSET = CharsetUtil.UTF_8;
    
}
