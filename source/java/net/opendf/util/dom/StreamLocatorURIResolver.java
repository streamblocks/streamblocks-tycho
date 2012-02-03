/**
 * 
 */
package net.opendf.util.dom;

import java.io.InputStream;
import java.net.URL;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.opendf.util.io.SourceStream;
import net.opendf.util.io.StreamLocator;

class StreamLocatorURIResolver implements URIResolver {

    public Source resolve(String href, String base) throws TransformerException
    {
        SourceStream s = locator.getAsStream(href);
     
        if (s == null) {
            if (resolver != null)
                return resolver.resolve(href, base);
            else {
                return null;
            }
        } else {
            return new StreamSource(s.getInputStream());
        }
    }

    public StreamLocatorURIResolver(StreamLocator locator, URIResolver resolver) {
        this.locator = locator;
        this.resolver = resolver;
    }

    private StreamLocator locator;
    private URIResolver resolver;
}