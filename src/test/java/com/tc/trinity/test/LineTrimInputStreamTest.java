package com.tc.trinity.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import junit.framework.TestCase;

import com.tc.trinity.core.LineTrimInputStream;


/**
 * TODO 类的功能描述。
 *
 * @author gaofeng
 * @date Jun 12, 2014 3:24:58 PM
 * @id $Id$
 */
public class LineTrimInputStreamTest extends TestCase {
    
    String tmpFile = "/tmp/PropertiesLoadTest.properties";
    
    @Override
    public void setUp() throws IOException{
        PrintWriter w = new PrintWriter(new FileWriter(tmpFile));
        w.println("a.b.c=Hello    ");
        w.println("a.b.b =  Hello    ");
        w.println("a.b.d= Hello    t");
        w.close();
    }
    
    public void testPropertiesLoad() throws FileNotFoundException, IOException{
        Properties p = new Properties();
        p.load(new LineTrimInputStream(new FileInputStream(tmpFile)));
        assertTrue(p.getProperty("a.b.c").equals("Hello"));
        assertTrue(p.getProperty("a.b.b").equals("Hello"));
        assertTrue(p.getProperty("a.b.d").equals("Hello    t"));
    }
    
    @Override
    public void tearDown(){
        new File(tmpFile).delete();
    }
    
}
