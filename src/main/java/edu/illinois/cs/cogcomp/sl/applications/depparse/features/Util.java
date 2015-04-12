package edu.illinois.cs.cogcomp.sl.applications.depparse.features;

import java.io.Closeable;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Utility methods that may be generally useful.
 * 
 * @author Jason Baldridge
 * @created August 27, 2006
 */
public class Util {

  // Assumes input is a String[] containing integers as strings.
  public static int[] stringsToInts(String[] stringreps) {
    int[] nums = new int[stringreps.length];
    for (int i = 0; i < stringreps.length; i++)
      nums[i] = Integer.parseInt(stringreps[i]);
    return nums;
  }

  // Assumes input is a String[] containing doubles as strings.
  public static double[] stringsToDoubles(String[] stringreps) {
    double[] nums = new double[stringreps.length];
    for (int i = 0; i < stringreps.length; i++)
      nums[i] = Double.parseDouble(stringreps[i]);
    return nums;
  }

  public static String join(String[] a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a[0]);
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(a[i]);
    return sb.toString();
  }

  public static String join(int[] a, char sep) {
    StringBuffer sb = new StringBuffer();
    sb.append(a[0]);
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(a[i]);
    return sb.toString();
  }

  public static String join(double[] a, char sep, int fractionDigits) {
    StringBuffer sb = new StringBuffer();
    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(fractionDigits);
    sb.append(df.format(a[0]));
    for (int i = 1; i < a.length; i++)
      sb.append(sep).append(df.format(a[i]));
    return sb.toString();
  }

  public static void closeQuietly(Closeable closeable) {
    if (closeable != null) {
      try {
        closeable.close();
      } catch (IOException e) {
        // Ignore
      }
    }
  }
}
