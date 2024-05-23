package oracle.cep.test.linearroad;
/* $Header: TkAllSegGen.java 16-oct-2007.11:55:47 mthatte Exp $ */

/* Copyright (c) 2007, Oracle. All rights reserved.  */

/*
   DESCRIPTION
    <short description of component this file declares/defines>

   PRIVATE CLASSES
    <list of private classes defined - with one-line descriptions>

   NOTES
    <other useful comments, qualifications, etc.>

   MODIFIED    (MM/DD/YY)
    mthatte     10/16/07 - 
    anasrini    07/15/07 - 
    najain      05/18/07 - 
    dlenkov     04/02/07 - Creation
 */

/**
 *  @version $Header: TkAllSegGen.java 16-oct-2007.11:55:47 mthatte Exp $
 *  @author  dlenkov 
 *  @since   release specific (what release of product did this appear in)
 */

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class TkAllSegGen {

  private static int way_col = 3;
  private static int seg_col = 6;
  private static int way_max = 10;
  private static int seg_len = 52800;

  public static void main( String[] args) throws IOException {

    String inpfile = args[0];
    String outfile = args[1];

    int segments[] = new int[way_max+1]; // for each exp_way

    FileReader reader;
    BufferedReader bufReader;
    StreamTokenizer tokenizer;

    try {
      // Setup the file for reading
      reader = new FileReader( inpfile);
      bufReader = new BufferedReader( reader);
      tokenizer = new StreamTokenizer( bufReader);
      tokenizer.parseNumbers();
      tokenizer.eolIsSignificant(true);

      // Parse schema line
      int t;
      int num_attrs = 0;
      boolean done = false;
      while ((t = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {

        switch (t) {
        case StreamTokenizer.TT_WORD:
	    num_attrs++;
	    continue;
	case ',':
	    continue;
        case StreamTokenizer.TT_EOL:
	    // Finished parsing the schema line
	    done = true;
	    break;
        default:
	    System.out.println("wrong schema in the input file");
	    return;
	}
	if (done)
	  break;
      }
      if (t == StreamTokenizer.TT_EOF) {
	System.out.println("wrong input file - no data");
	return;
      }
      if (num_attrs < seg_col) {
	System.out.println("wrong schema in the input file");
	return;
      }

      for (int i = 0; i < way_max+1; i++)
	segments[i] = -1;

      int exp_way = -1;
      int segment = -1;
      num_attrs = 0;
      while ((t = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {

	switch (t) {
        case ',':
	    continue;
	case StreamTokenizer.TT_NUMBER:
	    num_attrs++;
	    int val = (int) tokenizer.nval;

	    if (num_attrs == way_col) {
	      if (val > way_max) {
		System.out.println("wrong exp_way value");
		return;
	      }
	      exp_way = val;
	    }
	    else if (num_attrs == seg_col) {
	      if (val < 0) {
		System.out.println("wrong position value");
		return;
	      }
	      segment = val / seg_len;
	    }
	    continue;
	case StreamTokenizer.TT_EOL:
	    if (exp_way < 0 || segment < 0) {
	      System.out.println("wrong data raw");
	      return;
	    }
	    if (segments[exp_way] < segment)
	      segments[exp_way] = segment;

	    exp_way = -1;
	    segment = -1;
	    num_attrs = 0;
	    continue;
	} // switch
      } // while
      if (num_attrs > 0) {
	if (exp_way < 0 || segment < 0) {
	  System.out.println("wrong data raw");
	  return;
	}
	if (segments[exp_way] < segment)
	  segments[exp_way] = segment;
      }
      bufReader.close();
      reader.close();

    } catch (IOException ioe) {
        ioe.printStackTrace();
    }

    BufferedWriter writer = null;

    try {

      writer = new BufferedWriter( new FileWriter( outfile));

      for (int j = 0; j < way_max+1; j++)
	if (segments[j] >= 0) {

	  for (int k = 0; k < segments[j]+1; k++) {
	    writer.write( String.valueOf( j));
	    writer.write( ", ");
	    writer.write( String.valueOf( 0));
	    writer.write( ", ");
	    writer.write( String.valueOf( k));
	    writer.write( '\n');
	    writer.write( String.valueOf( j));
	    writer.write( ", ");
	    writer.write( String.valueOf( 1));
	    writer.write( ", ");
	    writer.write( String.valueOf( k));
	    writer.write( '\n');
	  } // for k
	} // for j if j >= 0
      writer.flush();
      writer.close();

    } catch (IOException ioe) {
        ioe.printStackTrace();
    }

  }

}
