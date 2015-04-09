
package com.tc.trinity.core;

import java.io.IOException;
import java.io.InputStream;

/**
 * 抛弃行末空格(0x20)和制表符(0x09)的InputStream。Decorator模式实现
 * key = value \t b\n ==> key = value\n
 * TODO 制表符支持
 * 
 * @author gaofeng
 * @date Jun 12, 2014 11:23:08 AM
 * @id $Id$
 */
public class LineTrimInputStream extends InputStream {
    
    private int count = 0;
    
    private int lastChar = 0x00;
    
    private InputStream inputStream;
    
    public LineTrimInputStream(InputStream inputStream) {
    
        this.inputStream = inputStream;
    }
    
    @Override
    public int read() throws IOException {
    
        if (inputStream == null) {
            return -1;
        }
        if (count > 0) {
            count--;
            return 0x20;
        }
        if (lastChar != 0x00) {
            int t = lastChar;
            lastChar = 0x00;
            return t;
        }
        int c;
        for (c = inputStream.read(); c == 0x20; c = inputStream.read()) {
            count++;
        }
        if (c == 0x0a || c == 0x0d || c == -1 || count == 0) {
            count = 0;
            return c;
        } else {
            count--;
            lastChar = c;
            return 0x20;
        }
    }
    
}
