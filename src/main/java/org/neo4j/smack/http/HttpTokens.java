package org.neo4j.smack.http;

import java.nio.charset.Charset;

import org.jboss.netty.util.CharsetUtil;

public class HttpTokens {

    static final byte SP = 32;

    //tab ' '
    static final byte HT = 9;

    /**
     * Carriage return
     */
    static final byte CR = 13;

    /**
     * Equals '='
     */
    static final byte EQUALS = 61;

    /**
     * Line feed character
     */
    static final byte LF = 10;

    /**
     * carriage return line feed
     */
    static final byte[] CRLF = new byte[] { CR, LF };

    /**
    * Colon ':'
    */
    static final byte COLON = 58;

    /**
    * Semicolon ';'
    */
    static final byte SEMICOLON = 59;

     /**
    * comma ','
    */
    static final byte COMMA = 44;

    static final byte DOUBLE_QUOTE = '"';

    static final Charset DEFAULT_CHARSET = CharsetUtil.UTF_8;
    
}
