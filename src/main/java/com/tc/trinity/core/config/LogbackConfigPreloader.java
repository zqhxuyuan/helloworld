
package com.tc.trinity.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.LocatorImpl;

import ch.qos.logback.core.joran.spi.ElementPath;

/**
 * 在获取配置之前，先扫描一遍配置文件（logback.xml）
 * 
 * @see ch.qos.logback.core.joran.event.SaxEventRecorder
 *
 * @author kozz.gaof
 * @date Jul 3, 2014 3:48:54 PM
 * @id $Id$
 */
public class LogbackConfigPreloader extends DefaultHandler {
    
    public List<SaxEvent> saxEventList = new ArrayList<SaxEvent>();
    Locator locator;
    ElementPath globalElementPath = new ElementPath();
    
    final public void recordEvents(InputStream inputStream) {
    
        recordEvents(new InputSource(inputStream));
    }
    
    public List<SaxEvent> recordEvents(InputSource inputSource) {
    
        SAXParser saxParser = buildSaxParser();
        try {
            saxParser.parse(inputSource, this);
            return saxEventList;
        } catch (IOException ie) {
            System.err.println("I/O error occurred while parsing xml file");
        } catch (SAXException se) {
            // Exception added into StatusManager via Sax error handling. No need to add it again
            System.err.println("Problem parsing XML document. See previously reported errors.");
        } catch (Exception ex) {
            System.err.println("Unexpected exception while parsing XML document.");
        }
        throw new IllegalStateException();
    }
    
    private SAXParser buildSaxParser() {
    
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(false);
            spf.setNamespaceAware(true);
            return spf.newSAXParser();
        } catch (Exception pce) {
            throw new IllegalStateException("Parser configuration error occurred");
        }
    }
    
    public void startDocument() {
    
    }
    
    public Locator getLocator() {
    
        return locator;
    }
    
    public void setDocumentLocator(Locator l) {
    
        locator = l;
    }
    
    public void startElement(String namespaceURI, String localName, String qName,
            Attributes atts) {
    
        String tagName = getTagName(localName, qName);
        globalElementPath.push(tagName);
        ElementPath current = globalElementPath.duplicate();
        saxEventList.add(new StartEvent(current, namespaceURI, localName, qName,
                atts, getLocator()));
    }
    
    public void characters(char[] ch, int start, int length) {
    
        String bodyStr = new String(ch, start, length);
        SaxEvent lastEvent = getLastEvent();
        if (lastEvent instanceof BodyEvent) {
            BodyEvent be = (BodyEvent) lastEvent;
            be.append(bodyStr);
        } else {
            // ignore space only text if the previous event is not a BodyEvent
            if (!isSpaceOnly(bodyStr)) {
                saxEventList.add(new BodyEvent(bodyStr, getLocator()));
            }
        }
    }
    
    boolean isSpaceOnly(String bodyStr) {
    
        String bodyTrimmed = bodyStr.trim();
        return (bodyTrimmed.length() == 0);
    }
    
    SaxEvent getLastEvent() {
    
        if (saxEventList.isEmpty()) {
            return null;
        }
        int size = saxEventList.size();
        return saxEventList.get(size - 1);
    }
    
    public void endElement(String namespaceURI, String localName, String qName) {
    
        saxEventList
                .add(new EndEvent(namespaceURI, localName, qName, getLocator()));
        globalElementPath.pop();
    }
    
    String getTagName(String localName, String qName) {
    
        String tagName = localName;
        if ((tagName == null) || (tagName.length() < 1)) {
            tagName = qName;
        }
        return tagName;
    }
    
    public static class SaxEvent {
        
        final public String namespaceURI;
        final public String localName;
        final public String qName;
        final public Locator locator;
        
        SaxEvent(String namespaceURI, String localName, String qName, Locator locator) {
        
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
            // locator impl is used to take a snapshot!
            this.locator = new LocatorImpl(locator);
        }
        
        public String getLocalName() {
        
            return localName;
        }
        
        public Locator getLocator() {
        
            return locator;
        }
        
        public String getNamespaceURI() {
        
            return namespaceURI;
        }
        
        public String getQName() {
        
            return qName;
        }
    }
    
    public static class StartEvent extends SaxEvent {
        
        final public Attributes attributes;
        final public ElementPath elementPath;
        
        StartEvent(ElementPath elementPath, String namespaceURI, String localName, String qName,
                Attributes attributes, Locator locator) {
        
            super(namespaceURI, localName, qName, locator);
            // locator impl is used to take a snapshot!
            this.attributes = new AttributesImpl(attributes);
            this.elementPath = elementPath;
        }
        
        public Attributes getAttributes() {
        
            return attributes;
        }
        
        @Override
        public String toString() {
        
            return "StartEvent(" + getQName() + ")  [" + locator.getLineNumber() + "," + locator.getColumnNumber() + "]";
        }
        
    }
    
    public static class EndEvent extends SaxEvent {
        
        EndEvent(String namespaceURI, String localName, String qName, Locator locator) {
        
            super(namespaceURI, localName, qName, locator);
        }
        
        @Override
        public String toString() {
        
            return "  EndEvent(" + getQName() + ")  [" + locator.getLineNumber() + "," + locator.getColumnNumber() + "]";
        }
        
    }
    
    public static class BodyEvent extends SaxEvent {
        
        private String text;
        
        BodyEvent(String text, Locator locator) {
        
            super(null, null, null, locator);
            this.text = text;
        }
        
        /**
         * Always trim trailing spaces from the body text.
         * 
         * @return
         */
        public String getText() {
        
            if (text != null) {
                return text.trim();
            }
            return text;
        }
        
        @Override
        public String toString() {
        
            return "BodyEvent(" + getText() + ")" + locator.getLineNumber() + ","
                    + locator.getColumnNumber();
        }
        
        public void append(String str) {
        
            text += str;
        }
        
    }
    
}
