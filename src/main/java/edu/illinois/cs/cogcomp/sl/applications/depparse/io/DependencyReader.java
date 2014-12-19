///////////////////////////////////////////////////////////////////////////////
// Copyright (C) 2007 University of Texas at Austin and (C) 2005
// University of Pennsylvania and Copyright (C) 2002, 2003 University
// of Massachusetts Amherst, Department of Computer Science.
//
// This software is licensed under the terms of the Common Public
// License, Version 1.0 or (at your option) any subsequent version.
//
// The license is approved by the Open Source Initiative, and is
// available from their website at http://www.opensource.org.
///////////////////////////////////////////////////////////////////////////////

package edu.illinois.cs.cogcomp.sl.applications.depparse.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * A class that defines common behavior and abstract methods for readers for different formats.
 * 
 * <p>
 * Created: Sat Nov 10 15:25:10 2001
 * </p>
 * 
 * @author Jason Baldridge
 * @version $Id: DependencyReader.java 137 2013-09-10 09:33:47Z wyldfire $
 */
public abstract class DependencyReader {

  protected BufferedReader inputReader;

  protected boolean labeled = true;

  protected boolean confScores = false;

  public static DependencyReader createDependencyReader(String format, boolean discourseMode)
          throws IOException {

    if (format.equals("MST")) {
      return new MSTReader();
    } else if (format.equals("CONLL")) {
      return new CONLLReader(discourseMode);
    } else {
      System.out.println("!!!!!!!  Not a supported format: " + format);
      System.out.println("********* Assuming CONLL format. **********");
      return new CONLLReader(discourseMode);
    }
  }

  public static DependencyReader createDependencyReader(String format) throws IOException {

    return createDependencyReader(format, false);
  }

  public static DependencyReader createDependencyReaderWithConfidenceScores(String format)
          throws IOException {
    DependencyReader reader = createDependencyReader(format);
    reader.confScores = true;
    return reader;
  }

  public boolean startReading(String file) throws IOException {
    labeled = fileContainsLabels(file);
    inputReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
    return labeled;
  }

  public boolean isLabeled() {
    return labeled;
  }

  public abstract DependencyInstance getNext() throws IOException;

  protected abstract boolean fileContainsLabels(String filename) throws IOException;

  protected String normalize(String s) {
    if (s.matches("[0-9]+|[0-9]+\\.[0-9]+|[0-9]+[0-9,]+"))
      return "<num>";

    return s;
  }
}
