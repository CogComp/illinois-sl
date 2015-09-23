/*******************************************************************************
 * University of Illinois/NCSA Open Source License
 * Copyright (c) 2010, 
 *
 * Developed by:
 * The Cognitive Computations Group
 * University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal with the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimers.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimers in the documentation and/or other materials provided with the distribution.
 * Neither the names of the Cognitive Computations Group, nor the University of Illinois at Urbana-Champaign, nor the names of its contributors may be used to endorse or promote products derived from this Software without specific prior written permission.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *     
 *******************************************************************************/
package edu.illinois.cs.cogcomp.sl.applications.depparse.base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class RelationalFeature implements Serializable {

  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

public String name;

  public String[][] values;

  public RelationalFeature(int size, String declaration, BufferedReader br) throws IOException {
    values = new String[size][size];
    String[] declist = declaration.split(" ");
    name = declist[2];
    for (int i = 0; i < size; i++) {
      values[i] = br.readLine().substring(2).split(" ");
    }
  }

  public String getFeature(int firstIndex, int secondIndex) {
    if (firstIndex == 0 || secondIndex == 0)
      return name + "=NULL";
    else
      // System.out.println(values.length + "** " + name+"="+values[firstIndex-1][secondIndex-1]);
      return name + "=" + values[firstIndex - 1][secondIndex - 1];
  }

  private void writeObject(ObjectOutputStream out) throws IOException {
    out.writeObject(name);
    out.writeObject(values);
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    name = (String) in.readObject();
    values = (String[][]) in.readObject();
  }

}
