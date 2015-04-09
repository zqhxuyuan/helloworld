
package com.tc.trinity.test;

import java.io.FileNotFoundException;
import java.util.List;

import junit.framework.TestCase;

import org.xml.sax.InputSource;

import com.tc.trinity.core.config.LogbackConfigPreloader;
import com.tc.trinity.core.config.LogbackConfigPreloader.SaxEvent;

/**
 * TODO 类的功能描述。
 *
 * @author kozz.gaof
 * @date Jul 7, 2014 4:19:21 PM
 * @id $Id$
 */
public class LogbackConfigPreloaderTest extends TestCase {
    
    private LogbackConfigPreloader preloader = new LogbackConfigPreloader();
    
    public void testLoad() throws FileNotFoundException {
    
        List<SaxEvent> eventList = preloader.recordEvents(new InputSource(this.getClass().getResourceAsStream("/logback.xml")));
        assertTrue(eventList.size() > 0);
    }
    
}
