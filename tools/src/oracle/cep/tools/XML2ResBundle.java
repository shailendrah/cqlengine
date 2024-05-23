/* $Header: cep/wlevs_cql/modules/cqlengine/tools/src/oracle/cep/tools/XML2ResBundle.java /main/2 2012/06/21 16:10:02 apiper Exp $ */

/* Copyright (c) 2002, 2012, Oracle and/or its affiliates. 
All rights reserved. */

/*
   DESCRIPTION
     A converter to generate Java resource bundle given an XML message file.
     
   PRIVATE CLASSES
     <list of private classes defined - with one-line descriptions>
     
   NOTES
     1. 
     
   MODIFIED    (MM/DD/YY)
    sguan       12/07/06 - Updated comments
    sguan       11/29/06 - Creation
*/

/**
 *  @version $Header: cep/wlevs_cql/modules/cqlengine/tools/src/oracle/cep/tools/XML2ResBundle.java /main/2 2012/06/21 16:10:02 apiper Exp $
 *  @author  sguan
 *  @since   AS11
 */
package oracle.cep.tools;

import java.io.File;

import java.util.StringTokenizer;

import javax.xml.transform.*;

import javax.xml.transform.stream.*;

public class XML2ResBundle {

    /**
     * Constructor.
     */
    public XML2ResBundle() {
    }

  /**
   * Uses a stylesheet to perform the conversion.  Component name is used to 
   * retrieve messages belonging to the same component into a resource bundle
   * file named as &lt;component_name&gt;.java in the specified directory.
   * 
   * @param xmlFile XML input message file
   * @param xsltFile stylesheet
   * @param compname component name 
   * @param dir output directory
   * @throws TransformerException
   */
    public static void Transform(File xmlFile, File xsltFile,
                                  String compname, String dir)
          throws TransformerException {

        //resource bundle name
        int iLastDotPos = compname.lastIndexOf(".");
        String subDir = "";
        String className = compname;
        String packagename = "";
        if (iLastDotPos > 0)
        {
          StringBuffer strWork = new StringBuffer(20);
          className = compname.substring(iLastDotPos + 1);
          packagename = compname.substring(0, iLastDotPos);
          strWork.append(".");
          strWork.append(packagename);
          packagename = strWork.toString();
          subDir = compname.substring(0, iLastDotPos) + "/";
        }
        else if (iLastDotPos == 0)
        {
          System.out.println("Bad component name: " + compname);
          System.exit(1);
        }
        
        String bundleName = className + ".java";

        File outputDir = new File(dir + "/" + subDir);
        File outputFile = new File(dir + "/" + subDir + bundleName);
        if (!outputDir.exists())
        {
          System.out.println("Create the path " + outputDir + " first.");
          System.exit(1);
        }


        Source xmlSource = new StreamSource(xmlFile);
        Source xsltSource = new StreamSource(xsltFile);
        Result result = new StreamResult(outputFile);

        //get the transformer instance

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xsltSource);

        transformer.setParameter("classname", className);
        transformer.setParameter("compname", compname);
        transformer.setParameter("packagename", packagename);
          
        transformer.transform(xmlSource, result);

    }

  /**
   * Main driver.
   * 
   * @param args  4 arguments are required
   * @throws TransformerException
   */
    public static void main(String [] args )
       throws javax.xml.transform.TransformerException {

        if (args.length != 4) {
              System.err.println("Usage:");
              System.err.println(" java " + "XML2ResBundle "
                  + "  xsltFileName xmlFileName outputDir componentName\n" +
                  "   where \n" +
                  "       all file/directory must be in absolute path and\n" +
                  "       componentName is something like \"Exceptions\" \n");

              System.exit(1);
            }

        XML2ResBundle res = new XML2ResBundle();
        File xslFile = new File(args[0]);
        File xmlFile = new File(args[1]);
        String outputDir = args[2];

        res.Transform(xmlFile, xslFile, args[3], outputDir);


    }
}

