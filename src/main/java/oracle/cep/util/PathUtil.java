/* $Header: PathUtil.java 14-mar-2008.10:51:20 hopark   Exp $ */

/* Copyright (c) 2008, Oracle. All rights reserved. */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    hopark      03/14/08 - Creation
 */

/**
 *  @version $Header: PathUtil.java 14-mar-2008.10:51:20 hopark   Exp $
 *  @author  hopark  
 *  @since   release specific (what release of product did this appear in)
 */
package oracle.cep.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;

public class PathUtil 
{
    private String m_path;
    private String m_filename;
    
    public static String getFilePath(String path) {
        PathUtil p = new PathUtil();
        p.splitPath(path);
        return p.getPath();
    }

    public static String getFileName(String path) {
        PathUtil p = new PathUtil();
        p.splitPath(path);
        return p.getName();
    }

    public static void ensureFolder(String path)
    {
    	try
    	{
	    	File f = new File(path);
	    	if (!f.exists())
	    	{
	    		f.mkdirs();
	    	}
    	} catch(Exception e)
    	{
    		//eats up the exception
    	}
    }
  
    /**
     * Convert path to unix style path (e.g not having backslah)
     * This is mainly used by the path string for EPRXML.
     * @param path
     * @return
     */
    public static String getUnixPath(String path) {
        File f = new File(path);
        try {
            String cpath = f.getCanonicalPath();
            return cpath.replace("\\", "/");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }
    
    public PathUtil() {
    }
    

    public void splitPath(String path) 
    {
        if (path == null || path.length()==0) {
            m_path = "";
            m_filename = "";
            return;
        }
        int i = path.length();
        while (i > 0) {
            char ch = path.charAt(i-1);
            if (ch == '\\' || ch == '/' || ch == ':') break;
            i--;
        }
        if (i == 0) {
            m_path = "";
            m_filename = path;
        } else {
            // do not include trailing '/'
            m_path = path.substring(0, i-1);
            m_filename = path.substring(i);
        }
    }

    public static String getExtension( String path )
    {
        if( path == null ) return null;
        StringBuffer reversed = new StringBuffer(path).reverse();
        String revpath = reversed.toString();
        int posDot = revpath.indexOf('.');
        if( posDot == -1 ) return null;
        String ext = path.substring(path.length() - posDot);
        if( ext.indexOf('/') == -1 && ext.indexOf('\\') == -1)
        {
          return ext;
        } else {
          // if a slash is in there it means the dot was upstream from the file name
          return null; 
        }
    }    
	  
    public String getPath() {return m_path;}
    public String getName() {return m_filename;}


}


