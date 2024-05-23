package oracle.cep.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

public class Resource {
    public static final String RESOURCE_PREFIX = "res://";

    public static String resourceUrl(String path) { return RESOURCE_PREFIX + path; }
    public static boolean isResourceUrl(String url)
    {
    	return (url.startsWith(RESOURCE_PREFIX));
    }

    //rsc://class:resource
    //rsc://path
    public static InputStream getResource(String rscurl)
    {
    	String rsc = rscurl.trim().substring(RESOURCE_PREFIX.length());
    	int idx = rsc.indexOf(':');
    	String rscClass = null;
    	String rscName = rsc;
    	if (idx >= 0)
    	{
    		rscClass = rsc.substring(0, idx);
    		rscName = rsc.substring(idx+1);
    	}
    	Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        InputStream is = null;
        if (rscClass != null)
        {
	        try
	        {
	          Class<?> cls = Class.forName(rscClass, true, loader);
	  		  is = cls.getResourceAsStream(rscName);
	        } catch (Exception e)
	        {
	            throw new RuntimeException("Failed to find class: " + rscClass);    
	        }		
        } else {
        	is = Resource.class.getResourceAsStream(rscName);
        }
        if (is == null)
		{
			is = loader.getResourceAsStream(rscName);
		}
		if (is == null)
		{
			throw new RuntimeException("Failed to find resource : "+rscurl);
		}
		return is;
    }

    public static InputStream getStream(String urlstr)
    {
        if (Resource.isResourceUrl(urlstr))
        {
            return Resource.getResource(urlstr);
        }
        else
        {
            try {
                URL url = new URL(urlstr);
                return url.openStream();
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid url: "+ urlstr);
            } catch (IOException e) {
                throw new RuntimeException("Failed to open : "+ urlstr);
            }
        }
    }

    public static String getResourceAsString(String rscurl) throws IOException {
        InputStream stream = getStream(rscurl);
        final int bufferSize = 1024;
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        Reader in = new InputStreamReader(stream, "UTF-8");
        for (; ; ) {
            int rsz = in.read(buffer, 0, buffer.length);
            if (rsz < 0)
                break;
            out.append(buffer, 0, rsz);
        }
        return out.toString();
    }
}
