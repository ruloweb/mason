package sim.app.geo.walkThisWay;

import java.io.*;

import sim.field.grid.DoubleGrid2D;

//
//*****************************************************************************
/* Raster Double Grid 2D
 *
 * Extension of the DoubleGrid2D class for use with raster GIS data. This class
 * reads raster files in ASCII form, stores the header, and can write them later
 * to a file.
 */

public class RasterDoubleGrid2D extends DoubleGrid2D
{

  private int ncols;
  private int nrows;
  private double xllcorner = 0;
  private double yllcorner = 0;
  private double cellsize = 0;
  //private int NODATA_value = -9999;    // not actually needed
  private String NODATA_string;

  private static final long serialVersionUID = 1L;

  //***************************************************************************
  /**This is a Raster Double Grid 2D class constructor.
  * @param seed
  */
  public RasterDoubleGrid2D (final int width, final int height, final int initialValue)
  {

    super(width, height, initialValue);

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**This is a Raster Double Grid 2D class constructor.*/
  public RasterDoubleGrid2D(final int width, final int height)
  {

    super(width, height);

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**This is a Raster Double Grid 2D class constructor.*/
  public RasterDoubleGrid2D(final DoubleGrid2D values)
  {

    super(values);

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**This is a Raster Double Grid 2D class constructor.*/
  public RasterDoubleGrid2D(final RasterDoubleGrid2D values)
  {

    super(values);
    setHeader(values);

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
   * Copy the values and header from another raster grid.
   * @param values
   * @return this
   */
  public final RasterDoubleGrid2D setTo( final RasterDoubleGrid2D values )
  {

    super.setTo(values);
    setHeader(values);

    return this;

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
   * Copy the header from another raster grid.
   * @param other
   */
  public void setHeader(final RasterDoubleGrid2D other)
  {

    ncols = other.ncols;
    nrows = other.nrows;
    xllcorner = other.xllcorner;
    yllcorner = other.yllcorner;
    cellsize = other.cellsize;
    NODATA_string = other.NODATA_string;

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
  * Create a new raster grid by reading data from an ASCII file.
  * @param filename
  * @return the new grid
  * @throws Exception if an exception occurs while trying to read the file
  */
  static public RasterDoubleGrid2D createFromFile ( final String filename )
                                                               throws Exception
  {
    int _ncols=0, _nrows=0;
    double _xllcorner=0, _yllcorner=0, _cellsize=0;
    String _NODATA_string=null;
    RasterDoubleGrid2D grid = null;

    try
    {
      // Open the file (note: assumes default character encoding)
      final BufferedReader fin = new BufferedReader(new FileReader(filename));

      // Read the file header
      String s;
      for (int i = 0; i < 6; i++)
      {
        s = fin.readLine();
        final String [] tokens = s.split ( " ", 2 );

        switch (i)
        {
          case 0: _ncols =         Integer.parseInt(tokens[1].trim());   break;
          case 1: _nrows =         Integer.parseInt(tokens[1].trim());   break;
          case 2: _xllcorner =     Double.parseDouble(tokens[1].trim()); break;
          case 3: _yllcorner =     Double.parseDouble(tokens[1].trim()); break;
          case 4: _cellsize =      Double.parseDouble(tokens[1].trim()); break;
          case 5: _NODATA_string = tokens[1].trim();                     break;
        }
      }

      grid = new RasterDoubleGrid2D(_ncols, _nrows);
      grid.ncols = _ncols;
      grid.nrows = _nrows;
      grid.xllcorner = _xllcorner;
      grid.yllcorner = _yllcorner;
      grid.cellsize = _cellsize;
      grid.NODATA_string = _NODATA_string;

      grid.readDataFromFile(fin);
    }
    catch (final Exception e)
    {
      throw e;
    }

    return grid;

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
  * Read the data portion of an ASCII raster file.
  * @param fin BufferedReader which has already been opened and has read
  *  the file header.
  * @throws IOException
  */
  private void readDataFromFile(final BufferedReader fin) throws IOException
  {

    int i = 0, j = 0;
    try
    {
      // read in the data from the file and store it in tiles
      String s;
      while ( (s = fin.readLine()) != null )
      {
        for (final String p : s.split(" "))
        {
          double value;
          if ( p.equalsIgnoreCase ( NODATA_string ) )
            value = Double.MIN_VALUE;
          else
            value = Double.parseDouble(p);

          field[i][j] = value;
          i++; // increase the column count
        }

        i = 0; // reset the column count
        j++; // increase the row count
      }
    }
    catch (final Exception e)
    {
      e.printStackTrace();
      System.out.format ( "Error at i: %d, j: %d\n", i, j );
    }

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
  * Write the file header in ASCII format. Function used internally.
  * @param out
  * @throws IOException
  */
  private void writeFileHeader ( final BufferedWriter out ) throws IOException
  {
    try
    {
      out.write(String.format("ncols         %d", ncols));
      out.newLine();
      out.write(String.format("nrows         %d", nrows));
      out.newLine();
      out.write(String.format("xllcorner     %f", xllcorner));
      out.newLine();
      out.write(String.format("yllcorner     %f", yllcorner));
      out.newLine();
      out.write(String.format("cellsize      %f", cellsize));
      out.newLine();
      out.write(String.format("NODATA_value  %s", NODATA_string));
      out.newLine();
    }
    catch ( final IOException ex )
    {
      throw ex;
    }

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
  * Convert this double value to a string. If the value was NODATA in the
  * original file, it will be written out the same way.
  * @param val
  * @return the value in string format.
  */
  private String doubleToString( final double val )
  {
    if (val == Double.MIN_VALUE)
      return NODATA_string;

    return String.format( "%f", val );

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
  * Write this raster grid to a file, including the header.
  * @param filename
  */
  public void writeToFile( final String filename )
  {

    BufferedWriter out = null;
    try
    {
      out = new BufferedWriter( new FileWriter ( filename ) );
      writeFileHeader ( out );

      for (int j = 0; j < nrows; j++)
      {
        for (int i = 0; i < ncols; i++)
        {
          out.write ( doubleToString ( field[i][j] ) );
          if (j < nrows) out.write ( "\t" );
        }
        out.newLine();
      }
    }
    catch (final Exception ex)
    {
      ex.printStackTrace();
    }
    finally
    {
      //Close the BufferedWriter
      try
      {
        if ( out != null )
        {
          out.flush ();
          out.close();
        }
      }
      catch (final IOException ex)
      {
        ex.printStackTrace();
      }
    }

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /**
  * @return The largest value in the grid.
  */
  public double getMax()
  {
    double max = Double.MIN_VALUE;
    for (int j = 0; j < nrows; j++)
      for (int i = 0; i < ncols; i++)
        if (field[i][j] > max)
          max = field[i][j];

    return max;

  } // End method.
  //***************************************************************************

  //***************************************************************************
  /** This method does something.*/
  public static void main(final String [] args)
  {
    final double NaN = Double.NaN;
    final double x = 1.0;

    System.out.format("x < NaN - %s\n", Boolean.toString(x < NaN));
    System.out.format("x > NaN - %s\n", Boolean.toString(x > NaN));
    System.out.format("NaN > x - %s\n", Boolean.toString(NaN > x));
    System.out.format("NaN < x - %s\n", Boolean.toString(NaN < x));
    System.out.format("NaN.equals(Double.NaN) - %s\n",
                                          Boolean.toString(NaN == Double.NaN));
    System.out.format("Double.isNan(NaN) - %s\n",
                                          Boolean.toString(Double.isNaN(NaN)));

  } // End method.
  //***************************************************************************

}
