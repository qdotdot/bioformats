//
// ICSReader.java
//

/*
OME Bio-Formats package for reading and converting biological file formats.
Copyright (C) 2005-@year@ UW-Madison LOCI and Glencoe Software, Inc.

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package loci.formats.in;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;

import loci.common.DateTools;
import loci.common.Location;
import loci.common.RandomAccessInputStream;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.MetadataTools;
import loci.formats.meta.MetadataStore;

import ome.xml.model.primitives.PositiveFloat;
import ome.xml.model.primitives.PositiveInteger;

/**
 * ICSReader is the file format reader for ICS (Image Cytometry Standard)
 * files. More information on ICS can be found at http://libics.sourceforge.net
 *
 * TODO : remove sub-C logic once N-dimensional support is in place
 *        see http://dev.loci.wisc.edu/trac/java/ticket/398
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/src/loci/formats/in/ICSReader.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/src/loci/formats/in/ICSReader.java;hb=HEAD">Gitweb</a></dd></dl>
 *
 * @author Melissa Linkert melissa at glencoesoftware.com
 */
public class ICSReader extends FormatReader {

  // -- Constants --

  /** Newline characters. */
  public static final String NL = "\r\n";

  public static final String[] DATE_FORMATS = {
    "EEEE, MMMM dd, yyyy HH:mm:ss",
    "EEE dd MMMM yyyy HH:mm:ss",
    "EEE MMM dd HH:mm:ss yyyy",
    "EE dd MMM yyyy HH:mm:ss z",
    "HH:mm:ss dd\\MM\\yy"
  };

  // key token value matching regexes within the "document" category.
  //
  // this table is alphabetized for legibility only.
  //
  // however it is important that the most qualified regex list goes first,
  // e.g. { "a", "b" } must precede { "a" }.
  private static final String[][] DOCUMENT_KEYS = {
    { "date" },  // the full key is "document date"
    { "document", "average" },
    { "document" },
    { "gmtdate" },
    { "label" }
  };

  // key token value matching regexes within the "history" category.
  //
  // this table is alphabetized for legibility only.
  //
  // however it is important that the most qualified regex list goes first,
  // e.g. { "a", "b" } must precede { "a" }.
  private static final String[][] HISTORY_KEYS = {
    { "a\\d" }, // the full key is "history a1", etc.
    { "acquisition", "acquire\\..*." },
    { "acquisition", "laserbox\\..*." },
    { "acquisition", "modules\\(.*." },
    { "acquisition", "objective", "position" },
    { "adc", "resolution" },
    { "atd_hardware", "ver" },
    { "atd_libraries", "ver" },
    { "atd_microscopy", "ver" },
    { "author" },
    { "averagecount" },
    { "averagequality" },
    { "beam", "zoom" },
    { "binning" },
    { "bits/pixel" },
    { "black", "level" },
    { "black", "level\\*" },
    { "black_level" },
    { "camera", "manufacturer" },
    { "camera", "model" },
    { "camera" },
    { "cfd", "holdoff" },
    { "cfd", "limit", "high" },
    { "cfd", "limit", "low" },
    { "cfd", "zc", "level" },
    { "channel\\*" },
    { "collection", "time" },
    { "cols" },
    { "company" },
    { "count", "increment" },
    { "created", "on" },
    { "creation", "date" },
    { "cube", "descriptio" }, // sic; not found in sample files
    { "cube", "description" }, // correction; not found in sample files
    { "cube", "emm", "nm" },
    { "cube", "exc", "nm" },
    { "cube" },
    { "date" },
    { "dategmt" },
    { "dead", "time", "comp" },
    { "desc", "exc", "turret" },
    { "desc", "emm", "turret" },
    { "detector", "type" },
    { "detector" },
    { "dimensions" },
    { "direct", "turret" },
    { "dither", "range" },
    { "dwell" },
    { "excitationfwhm" },
    { "experiment" },
    { "experimenter" },
    { "expon.", "order" },
    { "exposure" },
    { "exposure_time" },
    { "ext", "latch", "delay" },
    { "extents" },
    { "filterset", "dichroic", "name" },
    { "filterset", "dichroic", "nm" },
    { "filterset", "emm", "name" },
    { "filterset", "emm", "nm" },
    { "filterset", "exc", "name" },
    { "filterset", "exc", "nm" },
    { "filterset" },
    { "filter\\*" },
    { "firmware" },
    { "fret", "backgr\\d"},
    { "frametime" },
    { "gain" },
    { "gain\\d" },
    { "gain\\*" },
    { "gamma" },
    { "icsviewer", "ver" },
    { "ht\\*" },
    { "id" },
    { "illumination", "mode", "laser" },
    { "illumination", "mode" },
    { "image", "bigendian" },
    { "image", "bpp" },
    { "image", "form" }, // not found in sample files
    { "image", "physical_sizex" },
    { "image", "physical_sizey" },
    { "image", "sizex" },
    { "image", "sizey" },
    { "labels" },
    { "lamp", "manufacturer" },
    { "lamp", "model" },
    { "laser", "firmware" },
    { "laser", "manufacturer" },
    { "laser", "model" },
    { "laser", "power" },
    { "laser", "rep", "rate" },
    { "laser", "type" },
    { "laser\\d", "intensity" },
    { "laser\\d", "name" },
    { "laser\\d", "wavelength" },
    { "left" },
    { "length" },
    { "line", "compressio" }, // sic
    { "line", "compression" }, // correction; not found in sample files
    { "linetime" },
    { "magnification" },
    { "manufacturer" },
    { "max", "photon", "coun" }, // sic
    { "max", "photon", "count" }, // correction; not found in sample files
    { "memory", "bank" },
    { "metadata", "format", "ver" },
    { "microscope", "built", "on" },
    { "microscope", "name" },
    { "microscope" },
    { "mirror", "\\d" },
    { "mode" },
    { "noiseval" },
    { "no.", "frames" },
    { "objective", "detail" },
    { "objective", "immersion" },
    { "objective", "mag" },
    { "objective", "magnification" },
    { "objective", "na" },
    { "objective", "type" },
    { "objective", "workingdistance" },
    { "objective" },
    { "offsets" },
    { "other", "text" },
    { "passcount" },
    { "pinhole" },
    { "pixel", "clock" },
    { "pixel", "time" },
    { "pmt" },
    { "polarity" },
    { "region" },
    { "rep", "period" },
    { "repeat", "time" },
    { "revision" },
    { "routing", "chan", "x" },
    { "routing", "chan", "y" },
    { "rows" },
    { "scan", "borders" },
    { "scan", "flyback" },
    { "scan", "pattern" },
    { "scan", "pixels", "x" },
    { "scan", "pixels", "y" },
    { "scan", "pos", "x" },
    { "scan", "pos", "y" },
    { "scan", "resolution" },
    { "scan", "speed" },
    { "scan", "zoom" },
    { "scanner", "lag" },
    { "scanner", "pixel", "time" },
    { "scanner", "resolution" },
    { "scanner", "speed" },
    { "scanner", "xshift" },
    { "scanner", "yshift" },
    { "scanner", "zoom" },
    { "shutter\\d" },
    { "shutter", "type" },
    { "software" },
    { "spectral", "bin_definition" },
    { "spectral", "calibration", "gain", "data" },
    { "spectral", "calibration", "gain", "mode" },
    { "spectral", "calibration", "offset", "data" },
    { "spectral", "calibration", "offset", "mode" },
    { "spectral", "calibration", "sensitivity", "mode" },
    { "spectral", "central_wavelength" },
    { "spectral", "laser_shield" },
    { "spectral", "laser_shield_width" },
    { "spectral", "resolution" },
    { "stage", "controller" },
    { "stage", "firmware" },
    { "stage", "manufacturer" },
    { "stage", "model" },
    { "stage", "pos" },
    { "stage", "positionx" },
    { "stage", "positiony" },
    { "stage", "positionz" },
    { "stage_xyzum" },
    { "step\\d", "channel", "\\d" },
    { "step\\d", "gain", "\\d" },
    { "step\\d", "laser" },
    { "step\\d", "name" },
    { "step\\d", "pinhole" },
    { "step\\d", "pmt", "ch", "\\d" },
    { "step\\d", "shutter", "\\d" },
    { "step\\d" },
    { "stop", "on", "o'flow" },
    { "stop", "on", "time" },
    { "study" },
    { "sync", "freq", "div" },
    { "sync", "holdoff" },
    { "sync" },
    { "tac", "gain" },
    { "tac", "limit", "low" },
    { "tac", "offset" },
    { "tac", "range" },
    { "tau\\d" },
    { "tcspc", "adc", "res" },
    { "tcspc", "adc", "resolution" },
    { "tcspc", "approx", "adc", "rate" },
    { "tcspc", "approx", "cfd", "rate" },
    { "tcspc", "approx", "tac", "rate" },
    { "tcspc", "bh" },
    { "tcspc", "cfd", "holdoff" },
    { "tcspc", "cfd", "limit", "high" },
    { "tcspc", "cfd", "limit", "low" },
    { "tcspc", "cfd", "zc", "level" },
    { "tcspc", "clock", "polarity" },
    { "tcspc", "collection", "time" },
    { "tcspc", "count", "increment" },
    { "tcspc", "dead", "time", "enabled" },
    { "tcspc", "delay" },
    { "tcspc", "dither", "range" },
    { "tcspc", "left", "border" },
    { "tcspc", "line", "compression" },
    { "tcspc", "mem", "offset" },
    { "tcspc", "operation", "mode" },
    { "tcspc", "overflow" },
    { "tcspc", "pixel", "clk", "divider" },
    { "tcspc", "pixel", "clock" },
    { "tcspc", "routing", "x" },
    { "tcspc", "routing", "y" },
    { "tcspc", "scan", "x" },
    { "tcspc", "scan", "y" },
    { "tcspc", "sync", "divider" },
    { "tcspc", "sync", "holdoff" },
    { "tcspc", "sync", "rate" },
    { "tcspc", "sync", "threshold" },
    { "tcspc", "sync", "zc", "level" },
    { "tcspc", "tac", "gain" },
    { "tcspc", "tac", "limit", "high" },
    { "tcspc", "tac", "limit", "low" },
    { "tcspc", "tac", "offset" },
    { "tcspc", "tac", "range" },
    { "tcspc", "time", "window" },
    { "tcspc", "top", "border" },
    { "tcspc", "total", "frames" },
    { "tcspc", "total", "time" },
    { "tcspc", "trigger" },
    { "tcspc", "x", "sync", "polarity" },
    { "tcspc", "y", "sync", "polarity" },
    { "text" },
    { "time" },
    { "title" },
    { "top" },
    { "transmission" },
    { "trigger" },
    { "type" },
    { "units" },
    { "version" },
    { "wavelength\\*" },
    { "x", "amplitude" },
    { "y", "amplitude" },
    { "x", "delay" },
    { "y", "delay" },
    { "x", "offset" },
    { "y", "offset" },
    { "z", "\\(background\\)" }
  };

  // key token value matching regexes within the "layout" category.
  //
  // this table is alphabetized for legibility only.
  //
  // however it is important that the most qualified regex list goes first,
  // e.g. { "a", "b" } must precede { "a" }.
  private static final String[][] LAYOUT_KEYS = {
    { "coordinates" },  // the full key is "layout coordinates"
    { "order" },
    { "parameters" },
    { "real_significant_bits" },
    { "significant_bits" },
    { "significant_channels" },
    { "sizes" }
  };

  // key token value matching regexes within the "parameter" category.
  //
  // this table is alphabetized for legibility only.
  //
  // however it is important that the most qualified regex list goes first,
  // e.g. { "a", "b" } must precede { "a" }.
  private static final String[][] PARAMETER_KEYS = {
    { "allowedlinemodes" },  // the full key is "parameter allowedlinemodes"
    { "ch" },
    { "higher_limit" },
    { "labels" },
    { "lower_limit" },
    { "origin" },
    { "range" },
    { "sample_width", "ch" },
    { "sample_width" },
    { "scale" },
    { "units", "adc-units", "channels" },
    { "units", "adc-units", "nm" },
    { "units" }
  };

  // key token value matching regexes within the "representation" category.
  //
  // this table is alphabetized for legibility only.
  //
  // however it is important that the most qualified regex list goes first,
  // e.g. { "a", "b" } must precede { "a" }.
  private static final String[][] REPRESENTATION_KEYS = {
    { "byte_order" }, // the full key is "representation byte_order"
    { "compression" },
    { "format" },
    { "sign" }
  };

  // key token value matching regexes within the "sensor" category.
  //
  // this table is alphabetized for legibility only.
  //
  // however it is important that the most qualified regex list goes first,
  // e.g. { "a", "b" } must precede { "a" }.
  private static final String[][] SENSOR_KEYS = {
    { "model" },  // the full key is "sensor model"
    { "s_params", "channels" },
    { "s_params", "exphotoncnt" },
    { "s_params", "lambdaem" },
    { "s_params", "lambdaex" },
    { "s_params", "numaperture" },
    { "s_params", "pinholeradius" },
    { "s_params", "pinholespacing" },
    { "s_params", "refinxlensmedium" }, // sic; not found in sample files
    { "s_params", "refinxmedium" }, // sic; not found in sample files
    { "s_params", "refrinxlensmedium" },
    { "s_params", "refrinxmedium" },
    { "type" }
  };

  // key token value matching regexes within the "view" category.
  //
  // this table is alphabetized for legibility only.
  //
  // however it is important that the most qualified regex list goes first,
  // e.g. { "a", "b" } must precede { "a" }.
  private static final String[][] VIEW_KEYS = {
    { "view", "color", "lib", "lut" }, // the full key is
                                       // "view view color lib lut"
    { "view", "color", "count" },
    { "view", "color", "doc", "scale" },
    { "view", "color", "mode", "rgb", "set" },
    { "view", "color", "mode", "rgb" },
    { "view", "color", "schemes" },
    { "view", "color", "view", "active" },
    { "view", "color" },
    { "view\\d", "alpha" },
    { "view\\d", "alphastate" },
    { "view\\d", "annotation", "annellipse" },
    { "view\\d", "annotation", "annpoint" },
    { "view\\d", "autoresize" },
    { "view\\d", "axis" },
    { "view\\d", "blacklevel" },
    { "view\\d", "color" },
    { "view\\d", "cursor" },
    { "view\\d", "dimviewoption" },
    { "view\\d", "gamma" },
    { "view\\d", "ignoreaspect" },
    { "view\\d", "intzoom" },
    { "view\\d", "live" },
    { "view\\d", "order" },
    { "view\\d", "port" },
    { "view\\d", "position" },
    { "view\\d", "saturation" },
    { "view\\d", "scale" },
    { "view\\d", "showall" },
    { "view\\d", "showcursor" },
    { "view\\d", "showindex" },
    { "view\\d", "size" },
    { "view\\d", "synchronize" },
    { "view\\d", "tile" },
    { "view\\d", "useunits" },
    { "view\\d", "zoom" },
    { "view\\d" },
    { "view" }
  };

  // These strings appeared in the former metadata field categories but are not
  // found in the LOCI sample files.
  //
  // The former metadata field categories table did not save the context, i.e.
  // the first token such as "document" or "history" and other intermediate
  // tokens.  The preceding tables such as DOCUMENT_KEYS or HISTORY_KEYS use
  // this full context.
  //
  // In an effort at backward compatibility, these will be used to form key
  // value pairs if key/value pair not already assigned and they match anywhere
  // in the input line.
  //
  private static String[][] OTHER_KEYS = {
    { "cube", "descriptio" },  // sic; also listed in HISTORY_KEYS
    { "cube", "description" }, // correction; also listed in HISTORY_KEYS
    { "image", "form" },       // also listed in HISTORY_KEYS
    { "refinxlensmedium" },    // Could be a mispelling of "refrinxlensmedium";
                               // also listed in SENSOR_KEYS
    { "refinxmedium" },        // Could be a mispelling of "refinxmedium";
                               // also listed in SENSOR_KEYS
    { "scil_type" },
    { "source" }
  };

  // -- Fields --

  /** Current filename. */
  private String currentIcsId;
  private String currentIdsId;

  /** Flag indicating whether current file is v2.0. */
  private boolean versionTwo;

  /** Image data. */
  private byte[] data;

  /** Offset to pixel data. */
  private long offset;

  /** Whether or not the pixels are GZIP-compressed. */
  private boolean gzip;

  private GZIPInputStream gzipStream;

  /** Whether or not the image is inverted along the Y axis. */
  private boolean invertY;

  /** Whether or not the channels represent lifetime histogram bins. */
  private boolean lifetime;

  /** Dimensional reordering for lifetime data */
  private String labels;

  /** The length of each channel axis. */
  private Vector<Integer> channelLengths;

  /** The type of each channel axis. */
  private Vector<String> channelTypes;

  private int prevImage;
  private boolean hasInstrumentData = false;
  private boolean storedRGB = false;

  // -- Constructor --

  /** Constructs a new ICSReader. */
  public ICSReader() {
    super("Image Cytometry Standard", new String[] {"ics", "ids"});
    domains = new String[] {FormatTools.LM_DOMAIN, FormatTools.FLIM_DOMAIN,
      FormatTools.UNKNOWN_DOMAIN};
    hasCompanionFiles = true;
    datasetDescription = "One .ics and possibly one .ids with a similar name";
  }

  // -- IFormatReader API methods --

  /* @see loci.formats.IFormatReader#isSingleFile(String) */
  public boolean isSingleFile(String id) throws FormatException, IOException {
    // check if we have a v2 ICS file - means there is no companion IDS file
    RandomAccessInputStream f = new RandomAccessInputStream(id);
    boolean singleFile = f.readString(17).trim().equals("ics_version\t2.0");
    f.close();
    return singleFile;
  }

  /* @see loci.formats.IFormatReader#getDomains() */
  public String[] getDomains() {
    FormatTools.assertId(currentId, true, 1);
    String[] domain = new String[] {FormatTools.GRAPHICS_DOMAIN};
    if (getChannelDimLengths().length > 1) {
      domain[0] = FormatTools.FLIM_DOMAIN;
    }
    else if (hasInstrumentData) {
      domain[0] = FormatTools.LM_DOMAIN;
    }

    return domain;
  }

  /* @see loci.formats.IFormatReader#getChannelDimLengths() */
  public int[] getChannelDimLengths() {
    FormatTools.assertId(currentId, true, 1);
    int[] len = new int[channelLengths.size()];
    for (int i=0; i<len.length; i++) {
      len[i] = channelLengths.get(i).intValue();
    }
    return len;
  }

  /* @see loci.formats.IFormatReader#getChannelDimTypes() */
  public String[] getChannelDimTypes() {
    FormatTools.assertId(currentId, true, 1);
    return channelTypes.toArray(new String[channelTypes.size()]);
  }

  /* @see loci.formats.IFormatReader#isInterleaved(int) */
  public boolean isInterleaved(int subC) {
    FormatTools.assertId(currentId, true, 1);
    return subC == 0 && core[0].interleaved;
  }

  /* @see loci.formats.IFormatReader#fileGroupOption(String) */
  public int fileGroupOption(String id) throws FormatException, IOException {
    return FormatTools.MUST_GROUP;
  }

  /**
   * @see loci.formats.IFormatReader#openBytes(int, byte[], int, int, int, int)
   */
  public byte[] openBytes(int no, byte[] buf, int x, int y, int w, int h)
    throws FormatException, IOException
  {
    FormatTools.checkPlaneParameters(this, no, buf.length, x, y, w, h);

    int bpp = FormatTools.getBytesPerPixel(getPixelType());
    int len = FormatTools.getPlaneSize(this);
    int pixel = bpp * getRGBChannelCount();
    int rowLen = FormatTools.getPlaneSize(this, w, 1);

    int[] coordinates = getZCTCoords(no);
    int[] prevCoordinates = getZCTCoords(prevImage);

    if (!gzip) {
      in.seek(offset + no * (long) len);
    }
    else {
      long toSkip = (no - prevImage - 1) * (long) len;
      if (gzipStream == null || no <= prevImage) {
        FileInputStream fis = null;
        toSkip = no * (long) len;
        if (versionTwo) {
          fis = new FileInputStream(currentIcsId);
          fis.skip(offset);
        }
        else {
          fis = new FileInputStream(currentIdsId);
          toSkip += offset;
        }
        try {
          gzipStream = new GZIPInputStream(fis);
        }
        catch (IOException e) {
          // the 'gzip' flag is set erroneously
          gzip = false;
          in.seek(offset + no * (long) len);
          gzipStream = null;
        }
      }

      if (gzipStream != null) {
        while (toSkip > 0) {
          toSkip -= gzipStream.skip(toSkip);
        }

        data = new byte[len * (storedRGB ? getSizeC() : 1)];
        int toRead = data.length;
        while (toRead > 0) {
          toRead -= gzipStream.read(data, data.length - toRead, toRead);
        }
      }
    }

    int sizeC = lifetime ? 1 : getSizeC();

    if (!isRGB() && sizeC > 4 && channelLengths.size() == 1 && storedRGB) {
      // channels are stored interleaved, but because there are more than we
      // can display as RGB, we need to separate them
      in.seek(offset +
        (long) len * getIndex(coordinates[0], 0, coordinates[2]));
      if (!gzip && data == null) {
        data = new byte[len * getSizeC()];
        in.read(data);
      }
      else if (!gzip && (coordinates[0] != prevCoordinates[0] ||
        coordinates[2] != prevCoordinates[2]))
      {
        in.read(data);
      }

      for (int row=y; row<h + y; row++) {
        for (int col=x; col<w + x; col++) {
          System.arraycopy(data, bpp * ((no % getSizeC()) + sizeC *
            (row * getSizeX() + col)), buf, bpp * (row * w + col), bpp);
        }
      }
    }
    else if (gzip) {
      RandomAccessInputStream s = new RandomAccessInputStream(data);
      readPlane(s, x, y, w, h, buf);
      s.close();
    }
    else {
      readPlane(in, x, y, w, h, buf);
    }

    if (invertY) {
      byte[] row = new byte[rowLen];
      for (int r=0; r<h/2; r++) {
        int topOffset = r * rowLen;
        int bottomOffset = (h - r - 1) * rowLen;
        System.arraycopy(buf, topOffset, row, 0, rowLen);
        System.arraycopy(buf, bottomOffset, buf, topOffset, rowLen);
        System.arraycopy(row, 0, buf, bottomOffset, rowLen);
      }
    }

    prevImage = no;

    return buf;
  }

  /* @see loci.formats.IFormatReader#getSeriesUsedFiles(boolean) */
  public String[] getSeriesUsedFiles(boolean noPixels) {
    FormatTools.assertId(currentId, true, 1);
    if (versionTwo) {
      return noPixels ? null : new String[] {currentIcsId};
    }
    return noPixels ? new String[] {currentIcsId} :
      new String[] {currentIcsId, currentIdsId};
  }

  /* @see loci.formats.IFormatReader#close(boolean) */
  public void close(boolean fileOnly) throws IOException {
    super.close(fileOnly);
    if (!fileOnly) {
      currentIcsId = null;
      currentIdsId = null;
      data = null;
      versionTwo = false;
      gzip = false;
      invertY = false;
      lifetime = false;
      prevImage = 0;
      hasInstrumentData = false;
      storedRGB = false;
      if (gzipStream != null) {
        gzipStream.close();
      }
      gzipStream = null;
    }
  }

  // -- Internal FormatReader API methods --

  /* @see loci.formats.FormatReader#initFile(String) */
  protected void initFile(String id) throws FormatException, IOException {
    super.initFile(id);

    LOGGER.info("Finding companion file");

    String icsId = id, idsId = id;
    int dot = id.lastIndexOf(".");
    String ext = dot < 0 ? "" : id.substring(dot + 1).toLowerCase();
    if (ext.equals("ics")) {
      // convert C to D regardless of case
      char[] c = idsId.toCharArray();
      c[c.length - 2]++;
      idsId = new String(c);
    }
    else if (ext.equals("ids")) {
      // convert D to C regardless of case
      char[] c = icsId.toCharArray();
      c[c.length - 2]--;
      icsId = new String(c);
    }

    if (icsId == null) throw new FormatException("No ICS file found.");
    Location icsFile = new Location(icsId);
    if (!icsFile.exists()) throw new FormatException("ICS file not found.");

    LOGGER.info("Checking file version");

    // check if we have a v2 ICS file - means there is no companion IDS file
    RandomAccessInputStream f = new RandomAccessInputStream(icsId);
    if (f.readString(17).trim().equals("ics_version\t2.0")) {
      in = new RandomAccessInputStream(icsId);
      versionTwo = true;
    }
    else {
      if (idsId == null) throw new FormatException("No IDS file found.");
      Location idsFile = new Location(idsId);
      if (!idsFile.exists()) throw new FormatException("IDS file not found.");
      currentIdsId = idsId;
      in = new RandomAccessInputStream(currentIdsId);
    }
    f.close();

    currentIcsId = icsId;

    LOGGER.info("Reading metadata");

    Double[] pixelSizes = null;
    Double[] timestamps = null;
    String[] units = null;
    String[] axes = null;
    int[] axisLengths = null;
    String byteOrder = null, rFormat = null, compression = null;

    // parse key/value pairs from beginning of ICS file

    RandomAccessInputStream reader = new RandomAccessInputStream(icsId);
    reader.seek(0);
    reader.readString(NL);
    String line = reader.readString(NL);
    boolean signed = false;

    StringBuffer textBlock = new StringBuffer();
    double[] sizes = null;

    Integer[] emWaves = null, exWaves = null;
    Double[] stagePos = null;
    String imageName = null, date = null, description = null;
    Double magnification = null, lensNA = null, workingDistance = null;
    String objectiveModel = null, immersion = null, lastName = null;
    Hashtable<Integer, Double> gains = new Hashtable<Integer, Double>();
    Hashtable<Integer, Double> pinholes = new Hashtable<Integer, Double>();
    Hashtable<Integer, Integer> wavelengths = new Hashtable<Integer, Integer>();
    Hashtable<Integer, String> channelNames = new Hashtable<Integer, String>();

    String laserModel = null;
    String laserManufacturer = null;
    Double laserPower = null;
    Double laserRepetitionRate = null;
    String detectorManufacturer = null;
    String detectorModel = null;
    String microscopeModel = null;
    String microscopeManufacturer = null;
    String experimentType = null;
    Double exposureTime = null;

    String filterSetModel = null;
    String dichroicModel = null;
    String excitationModel = null;
    String emissionModel = null;

    while (line != null && !line.trim().equals("end") &&
      reader.getFilePointer() < reader.length() - 1)
    {
      line = line.trim();
      if (line.length() > 0) {

        // split the line into tokens
        String[] tokens = tokenize(line);

        String token0 = tokens[0].toLowerCase();
        String[] keyValue = null;

        // version category
        if (token0.equals("ics_version")) {
          String value = concatenateTokens(tokens, 1, tokens.length);
          addGlobalMeta(token0, value);
        }
        // filename category
        else if (token0.equals("filename")) {
          imageName = concatenateTokens(tokens, 1, tokens.length);
          addGlobalMeta(token0, imageName);
        }
        // layout category
        else if (token0.equals("layout")) {
          keyValue = findKeyValue(tokens, LAYOUT_KEYS);
          String key = keyValue[0];
          String value = keyValue[1];
          addGlobalMeta(key, value);

          if (key.equalsIgnoreCase("layout sizes")) {
            StringTokenizer t = new StringTokenizer(value);
            axisLengths = new int[t.countTokens()];
            for (int n=0; n<axisLengths.length; n++) {
              try {
                axisLengths[n] = Integer.parseInt(t.nextToken().trim());
              }
              catch (NumberFormatException e) {
                LOGGER.debug("Could not parse axis length", e);
              }
            }
          }
          else if (key.equalsIgnoreCase("layout order")) {
            StringTokenizer t = new StringTokenizer(value);
            axes = new String[t.countTokens()];
            for (int n=0; n<axes.length; n++) {
              axes[n] = t.nextToken().trim();
            }
          }
          else if (key.equalsIgnoreCase("layout significant_bits")) {
            core[0].bitsPerPixel = Integer.parseInt(value);
          }
        }
        // representation category
        else if (token0.equals("representation")) {
          keyValue = findKeyValue(tokens, REPRESENTATION_KEYS);
          String key = keyValue[0];
          String value = keyValue[1];
          addGlobalMeta(key, value);

          if (key.equalsIgnoreCase("representation byte_order")) {
            byteOrder = value;
          }
          else if (key.equalsIgnoreCase("representation format")) {
            rFormat = value;
          }
          else if (key.equalsIgnoreCase("representation compression")) {
            compression = value;
          }
          else if (key.equalsIgnoreCase("representation sign")) {
            signed = value.equals("signed");
          }
        }
        // parameter category
        else if (token0.equals("parameter")) {
          keyValue = findKeyValue(tokens, PARAMETER_KEYS);
          String key = keyValue[0];
          String value = keyValue[1];
          addGlobalMeta(key, value);

          if (key.equalsIgnoreCase("parameter scale")) {
            // parse physical pixel sizes and time increment
            pixelSizes = splitDoubles(value);
          }
          else if (key.equalsIgnoreCase("parameter t")) {
            // parse explicit timestamps
            timestamps = splitDoubles(value);
          }
          else if (key.equalsIgnoreCase("parameter units")) {
            // parse units for scale
            units = value.split("\\s+");
          }
          if (getMetadataOptions().getMetadataLevel() !=
              MetadataLevel.MINIMUM)
          {
            if (key.equalsIgnoreCase("parameter ch")) {
              String[] names = value.split(" ");
              for (int n=0; n<names.length; n++) {
                channelNames.put(new Integer(n), names[n].trim());
              }
            }
          }
        }
        // history category
        else if (token0.equals("history")) {
          keyValue = findKeyValue(tokens, HISTORY_KEYS);
          String key = keyValue[0];
          String value = keyValue[1];
          addGlobalMeta(key, value);

          Double doubleValue = null;
          try {
            doubleValue = new Double(value);
          }
          catch (NumberFormatException e) {
            // ARG this happens a lot; spurious error in most cases
            LOGGER.debug("Could not parse double value '{}'", value, e);
          }

          if (key.equalsIgnoreCase("history software") &&
              value.indexOf("SVI") != -1) {
            // ICS files written by SVI Huygens are inverted on the Y axis
            invertY = true;
          }
          else if (key.equalsIgnoreCase("history date") ||
                   key.equalsIgnoreCase("history created on"))
          {
            if (value.indexOf(" ") > 0) {
              date = value.substring(0, value.lastIndexOf(" "));
              date = DateTools.formatDate(date, DATE_FORMATS);
            }
          }
          else if (key.equalsIgnoreCase("history creation date")) {
            date = DateTools.formatDate(value, DATE_FORMATS);
          }
          else if (key.equalsIgnoreCase("history type")) {
            // HACK - support for Gray Institute at Oxford's ICS lifetime data
            if (value.equalsIgnoreCase("time resolved") ||
                value.equalsIgnoreCase("FluorescenceLifetime"))
            {
              lifetime = true;
            }
            experimentType = value;
          }
          else if (key.equalsIgnoreCase("history labels")) {
              // HACK - support for Gray Institute at Oxford's ICS lifetime data
              labels = value;
          }
          else if (getMetadataOptions().getMetadataLevel() !=
                     MetadataLevel.MINIMUM) {

            if (key.equalsIgnoreCase("history") ||
                key.equalsIgnoreCase("history text"))
            {
              textBlock.append(value);
              textBlock.append("\n");
              metadata.remove(key);
            }
            else if (key.startsWith("history gain")) {
              Integer n = new Integer(0);
              try {
                n = new Integer(key.substring(12).trim());
                n = new Integer(n.intValue() - 1);
              }
              catch (NumberFormatException e) { }
              if (doubleValue != null) {
                  gains.put(n, doubleValue);
              }
            }
            else if (key.startsWith("history laser") &&
                     key.endsWith("wavelength")) {
              int laser =
                Integer.parseInt(key.substring(13, key.indexOf(" ", 13))) - 1;
              value = value.replaceAll("nm", "").trim();
              try {
                wavelengths.put(new Integer(laser), new Integer(value));
              }
              catch (NumberFormatException e) {
                LOGGER.debug("Could not parse wavelength", e);
              }
            }
            else if (key.equalsIgnoreCase("history Wavelength*")) {
              String[] waves = value.split(" ");
              for (int i=0; i<waves.length; i++) {
                wavelengths.put(new Integer(i), new Integer(waves[i]));
              }
            }
            else if (key.equalsIgnoreCase("history laser manufacturer")) {
              laserManufacturer = value;
            }
            else if (key.equalsIgnoreCase("history laser model")) {
              laserModel = value;
            }
            else if (key.equalsIgnoreCase("history laser power")) {
              try {
                laserPower = new Double(value); //TODO ARG i.e. doubleValue
              }
              catch (NumberFormatException e) { }
            }
            else if (key.equalsIgnoreCase("history laser rep rate")) {
              String repRate = value;
              if (repRate.indexOf(" ") != -1) {
                repRate = repRate.substring(0, repRate.lastIndexOf(" "));
              }
              laserRepetitionRate = new Double(repRate);
            }
            else if (key.equalsIgnoreCase("history objective type") ||
                     key.equalsIgnoreCase("history objective"))
            {
              objectiveModel = value;
            }
            else if (key.equalsIgnoreCase("history objective immersion")) {
              immersion = value;
            }
            else if (key.equalsIgnoreCase("history objective NA")) {
              lensNA = doubleValue;
            }
            else if (key.equalsIgnoreCase
                       ("history objective WorkingDistance")) {
              workingDistance = doubleValue;
            }
            else if (key.equalsIgnoreCase("history objective magnification") ||
                     key.equalsIgnoreCase("history objective mag"))
            {
              magnification = doubleValue;
            }
            else if (key.equalsIgnoreCase("history camera manufacturer")) {
              detectorManufacturer = value;
            }
            else if (key.equalsIgnoreCase("history camera model")) {
              detectorModel = value;
            }
            else if (key.equalsIgnoreCase("history author") ||
                     key.equalsIgnoreCase("history experimenter"))
            {
              lastName = value;
            }
            else if (key.equalsIgnoreCase("history extents")) {
              String[] lengths = value.split(" ");
              sizes = new double[lengths.length];
              for (int n=0; n<sizes.length; n++) {
                try {
                  sizes[n] = Double.parseDouble(lengths[n].trim());
                }
                catch (NumberFormatException e) {
                  LOGGER.debug("Could not parse axis length", e);
                }
              }
            }
            else if (key.equalsIgnoreCase("history stage_xyzum")) {
              String[] positions = value.split(" ");
              stagePos = new Double[positions.length];
              for (int n=0; n<stagePos.length; n++) {
                try {
                  stagePos[n] = new Double(positions[n]);
                }
                catch (NumberFormatException e) {
                  LOGGER.debug("Could not parse stage position", e);
                }
              }
            }
            else if (key.equalsIgnoreCase("history stage positionx")) {
              if (stagePos == null) {
                stagePos = new Double[3];
              }
              stagePos[0] = new Double(value); //TODO doubleValue
            }
            else if (key.equalsIgnoreCase("history stage positiony")) {
              if (stagePos == null) {
                stagePos = new Double[3];
              }
              stagePos[1] = new Double(value);
            }
            else if (key.equalsIgnoreCase("history stage positionz")) {
              if (stagePos == null) {
                stagePos = new Double[3];
              }
              stagePos[2] = new Double(value);
            }
            else if (key.equalsIgnoreCase("history other text")) {
              description = value;
            }
            else if (key.startsWith("history step") && key.endsWith("name")) {
              Integer n = new Integer(key.substring(12, key.indexOf(" ", 12)));
              channelNames.put(n, value);
            }
            else if (key.equalsIgnoreCase("history cube")) {
              channelNames.put(new Integer(channelNames.size()), value);
            }
            else if (key.equalsIgnoreCase("history cube emm nm")) {
              if (emWaves == null) {
                emWaves = new Integer[1];
              }
              emWaves[0] = new Integer(value.split(" ")[1].trim());
            }
            else if (key.equalsIgnoreCase("history cube exc nm")) {
              if (exWaves == null) {
                exWaves = new Integer[1];
              }
              exWaves[0] = new Integer(value.split(" ")[1].trim());
            }
            else if (key.equalsIgnoreCase("history microscope")) {
              microscopeModel = value;
            }
            else if (key.equalsIgnoreCase("history manufacturer")) {
              microscopeManufacturer = value;
            }
            else if (key.equalsIgnoreCase("history Exposure")) {
              String expTime = value;
              if (expTime.indexOf(" ") != -1) {
                expTime = expTime.substring(0, expTime.indexOf(" "));
              }
              exposureTime = new Double(expTime);
            }
            else if (key.equalsIgnoreCase("history filterset")) {
              filterSetModel = value;
            }
            else if (key.equalsIgnoreCase("history filterset dichroic name")) {
              dichroicModel = value;
            }
            else if (key.equalsIgnoreCase("history filterset exc name")) {
              excitationModel = value;
            }
            else if (key.equalsIgnoreCase("history filterset emm name")) {
              emissionModel = value;
            }
          }
        }
        // document category
        else if (token0.equals("document")) {
          keyValue = findKeyValue(tokens, DOCUMENT_KEYS);
          String key = keyValue[0];
          String value = keyValue[1];
          addGlobalMeta(key, value);

        }
        // sensor category
        else if (token0.equals("sensor")) {
          keyValue = findKeyValue(tokens, SENSOR_KEYS);
          String key = keyValue[0];
          String value = keyValue[1];
          addGlobalMeta(key, value);

          if (getMetadataOptions().getMetadataLevel() !=
              MetadataLevel.MINIMUM)
          {
            if (key.equalsIgnoreCase("sensor s_params LambdaEm")) {
              String[] waves = value.split(" ");
              emWaves = new Integer[waves.length];
              for (int n=0; n<emWaves.length; n++) {
                try {
                  emWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
                }
                catch (NumberFormatException e) {
                  LOGGER.debug("Could not parse emission wavelength", e);
                }
              }
            }
            else if (key.equalsIgnoreCase("sensor s_params LambdaEx")) {
              String[] waves = value.split(" ");
              exWaves = new Integer[waves.length];
              for (int n=0; n<exWaves.length; n++) {
                try {
                  exWaves[n] = new Integer((int) Double.parseDouble(waves[n]));
                }
                catch (NumberFormatException e) {
                  LOGGER.debug("Could not parse excitation wavelength", e);
                }
              }
            }
            else if (key.equalsIgnoreCase("sensor s_params PinholeRadius")) {
              String[] pins = value.split(" ");
              int channel = 0;
              for (int n=0; n<pins.length; n++) {
                if (pins[n].trim().equals("")) continue;
                try {
                  pinholes.put(new Integer(channel++), new Double(pins[n]));
                }
                catch (NumberFormatException e) {
                  LOGGER.debug("Could not parse pinhole", e);
                }
              }
            }
          }
        }
        // view category
        else if (token0.equals("view")) {
          keyValue = findKeyValue(tokens, VIEW_KEYS);
          String key = keyValue[0];
          String value = keyValue[1];

          // handle "view view color lib lut Green Fire green", etc.
          if (key.equalsIgnoreCase("view view color lib lut")) {
            int index;
            int redIndex = value.toLowerCase().lastIndexOf("red");
            int greenIndex = value.toLowerCase().lastIndexOf("green");
            int blueIndex = value.toLowerCase().lastIndexOf("blue");
            if (redIndex > 0 && redIndex > greenIndex && redIndex > blueIndex) {
              index = redIndex + "red".length();
            }
            else if (greenIndex > 0 &&
                     greenIndex > redIndex && greenIndex > blueIndex) {
              index = greenIndex + "green".length();
            }
            else if (blueIndex > 0 &&
                     blueIndex > redIndex && blueIndex > greenIndex) {
              index = blueIndex + "blue".length();
            }
            else {
                index = value.indexOf(' ');
            }
            if (index > 0) {
              key = key + ' ' + value.substring(0, index);
              value = value.substring(index + 1);
            }
          }
          // handle "view view color mode rgb set Default Colors" and
          // "view view color mode rgb set blue-green-red", etc.
          else if (key.equalsIgnoreCase("view view color mode rgb set")) {
              int index = value.toLowerCase().lastIndexOf("colors");
              if (index > 0) {
                  index += "colors".length();
              }
              else {
                index = value.indexOf(' ');
              }
              if (index > 0) {
                key = key + ' ' + value.substring(0, index);
                value = value.substring(index + 1);
              }
          }
          addGlobalMeta(key, value);
        }
        else {
          LOGGER.debug("Unknown category " + token0);
        }
      }
      line = reader.readString(NL);
    }
    reader.close();

    hasInstrumentData = emWaves != null || exWaves != null || lensNA != null ||
      stagePos != null || magnification != null || workingDistance != null ||
      objectiveModel != null || immersion != null;

    addGlobalMeta("history text", textBlock.toString());

    LOGGER.info("Populating core metadata");

    core[0].rgb = false;
    core[0].dimensionOrder = "XY";

    // find axis sizes

    channelLengths = new Vector<Integer>();
    channelTypes = new Vector<String>();

    int bitsPerPixel = 0;
    for (int i=0; i<axes.length; i++) {
      if (i >= axisLengths.length) break;
      if (axes[i].equals("bits")) {
        bitsPerPixel = axisLengths[i];
        while (bitsPerPixel % 8 != 0) bitsPerPixel++;
        if (bitsPerPixel == 24 || bitsPerPixel == 48) bitsPerPixel /= 3;
      }
      else if (axes[i].equals("x")) {
        core[0].sizeX = axisLengths[i];
      }
      else if (axes[i].equals("y")) {
        core[0].sizeY = axisLengths[i];
      }
      else if (axes[i].equals("z")) {
        core[0].sizeZ = axisLengths[i];
        if (getDimensionOrder().indexOf("Z") == -1) {
          core[0].dimensionOrder += "Z";
        }
      }
      else if (axes[i].equals("t")) {
        if (getSizeT() == 0) core[0].sizeT = axisLengths[i];
        else core[0].sizeT *= axisLengths[i];
        if (getDimensionOrder().indexOf("T") == -1) {
          core[0].dimensionOrder += "T";
        }
      }
      else {
        if (core[0].sizeC == 0) core[0].sizeC = axisLengths[i];
        else core[0].sizeC *= axisLengths[i];
        channelLengths.add(new Integer(axisLengths[i]));
        storedRGB = getSizeX() == 0;
        core[0].rgb = getSizeX() == 0 && getSizeC() <= 4 && getSizeC() > 1;
        if (getDimensionOrder().indexOf("C") == -1) {
          core[0].dimensionOrder += "C";
        }

        if (axes[i].startsWith("c")) {
          channelTypes.add(FormatTools.CHANNEL);
        }
        else if (axes[i].equals("p")) {
          channelTypes.add(FormatTools.PHASE);
        }
        else if (axes[i].equals("f")) {
          channelTypes.add(FormatTools.FREQUENCY);
        }
        else channelTypes.add("");
      }
    }

    if (channelLengths.size() == 0) {
      channelLengths.add(new Integer(1));
      channelTypes.add(FormatTools.CHANNEL);
    }

    core[0].dimensionOrder =
      MetadataTools.makeSaneDimensionOrder(getDimensionOrder());

    if (getSizeZ() == 0) core[0].sizeZ = 1;
    if (getSizeC() == 0) core[0].sizeC = 1;
    if (getSizeT() == 0) core[0].sizeT = 1;

    core[0].interleaved = isRGB();
    core[0].indexed = false;
    core[0].falseColor = false;
    core[0].metadataComplete = true;
    core[0].littleEndian = true;

    // HACK - support for Gray Institute at Oxford's ICS lifetime data
    if (lifetime && labels != null) {
      int binCount = 0;
      String newOrder = null;

      if (labels.equalsIgnoreCase("t x y")) {
        // nominal X Y Z is actually C X Y (which is X Y C interleaved)
        newOrder = "XYCZT";
        core[0].interleaved = true;
        binCount = core[0].sizeX;
        core[0].sizeX = core[0].sizeY;
        core[0].sizeY = core[0].sizeZ;
        core[0].sizeZ = 1;
      }
      else if (labels.equalsIgnoreCase("x y t")) {
        // nominal X Y Z is actually X Y C
        newOrder = "XYCZT";
        binCount = core[0].sizeZ;
        core[0].sizeZ = 1;
      }
      else {
        LOGGER.debug("Lifetime data, unexpected 'history labels' " + labels);
      }

      if (newOrder != null) {
        core[0].dimensionOrder = newOrder;
        core[0].sizeC = binCount;
        core[0].cLengths = new int[] {binCount};
        core[0].cTypes = new String[] {FormatTools.LIFETIME};
      }
    }

    // do not modify the Z, T, or channel counts after this point
    core[0].imageCount = getSizeZ() * getSizeT();
    if (!isRGB()) core[0].imageCount *= getSizeC();

    if (byteOrder != null) {
      String firstByte = byteOrder.split(" ")[0];
      int first = Integer.parseInt(firstByte);
      core[0].littleEndian = rFormat.equals("real") ? first == 1 : first != 1;
    }

    gzip = (compression == null) ? false : compression.equals("gzip");

    if (versionTwo) {
      String s = in.readString(NL);
      while (!s.trim().equals("end")) s = in.readString(NL);
    }

    offset = in.getFilePointer();

    int bytes = bitsPerPixel / 8;

    if (bitsPerPixel < 32) core[0].littleEndian = !isLittleEndian();

    boolean fp = rFormat.equals("real");
    core[0].pixelType = FormatTools.pixelTypeFromBytes(bytes, signed, fp);

    LOGGER.info("Populating OME metadata");

    MetadataStore store = makeFilterMetadata();
    MetadataTools.populatePixels(store, this, true);

    // populate Image data

    store.setImageName(imageName, 0);

    if (date != null) store.setImageAcquiredDate(date, 0);

    if (getMetadataOptions().getMetadataLevel() != MetadataLevel.MINIMUM) {
      store.setImageDescription(description, 0);

      // link Instrument and Image
      String instrumentID = MetadataTools.createLSID("Instrument", 0);
      store.setInstrumentID(instrumentID, 0);
      store.setMicroscopeModel(microscopeModel, 0);
      store.setMicroscopeManufacturer(microscopeManufacturer, 0);

      store.setImageInstrumentRef(instrumentID, 0);

      store.setExperimentID(MetadataTools.createLSID("Experiment", 0), 0);
      store.setExperimentType(getExperimentType(experimentType), 0);

      // populate Dimensions data

      if (pixelSizes != null) {
        if (units.length == pixelSizes.length - 1) {
          // correct for missing units
          // sometimes, the units for the C axis are missing entirely
          ArrayList<String> realUnits = new ArrayList<String>();
          int unitIndex = 0;
          for (int i=0; i<axes.length; i++) {
            if (axes[i].toLowerCase().equals("ch")) {
              realUnits.add("nm");
            }
            else {
              realUnits.add(units[unitIndex++]);
            }
          }
          units = realUnits.toArray(new String[realUnits.size()]);
        }

        for (int i=0; i<pixelSizes.length; i++) {
          Double pixelSize = pixelSizes[i];
          String axis = axes != null && axes.length > i ? axes[i] : "";
          String unit = units != null && units.length > i ? units[i] : "";
          if (axis.equals("x")) {
            if (pixelSize > 0 &&
              checkUnit(unit, "um", "microns", "micrometers"))
            {
              store.setPixelsPhysicalSizeX(new PositiveFloat(pixelSize), 0);
            }
            else {
              LOGGER.warn("Expected positive value for PhysicalSizeX; got {}",
                pixelSize);
            }
          }
          else if (axis.equals("y")) {
            if (pixelSize > 0 &&
              checkUnit(unit, "um", "microns", "micrometers"))
            {
              store.setPixelsPhysicalSizeY(new PositiveFloat(pixelSize), 0);
            }
            else {
              LOGGER.warn("Expected positive value for PhysicalSizeY; got {}",
                pixelSize);
            }
          }
          else if (axis.equals("z")) {
            if (pixelSize > 0 &&
              checkUnit(unit, "um", "microns", "micrometers"))
            {
              store.setPixelsPhysicalSizeZ(new PositiveFloat(pixelSize), 0);
            }
            else {
              LOGGER.warn("Expected positive value for PhysicalSizeZ; got {}",
                pixelSize);
            }
          }
          else if (axis.equals("t")) {
            if (checkUnit(unit, "ms")) {
              store.setPixelsTimeIncrement(1000 * pixelSize, 0);
            }
          }
        }
      }
      else if (sizes != null) {
        if (sizes.length > 0 && sizes[0] > 0) {
          store.setPixelsPhysicalSizeX(new PositiveFloat(sizes[0]), 0);
        }
        else {
          LOGGER.warn("Expected positive value for PhysicalSizeX; got {}",
            sizes[0]);
        }
        if (sizes.length > 1) {
          sizes[1] /= getSizeY();
          if (sizes[1] > 0) {
            store.setPixelsPhysicalSizeY(new PositiveFloat(sizes[1]), 0);
          }
          else {
            LOGGER.warn("Expected positive value for PhysicalSizeY; got {}",
              sizes[1]);
          }
        }
      }

      // populate Plane data

      if (timestamps != null) {
        for (int t=0; t<timestamps.length; t++) {
          if (t >= getSizeT()) break; // ignore superfluous timestamps
          if (timestamps[t] == null) continue; // ignore missing timestamp
          double deltaT = timestamps[t];
          if (Double.isNaN(deltaT)) continue; // ignore invalid timestamp
          // assign timestamp to all relevant planes
          for (int z=0; z<getSizeZ(); z++) {
            for (int c=0; c<getEffectiveSizeC(); c++) {
              int index = getIndex(z, c, t);
              store.setPlaneDeltaT(deltaT, 0, index);
            }
          }
        }
      }

      // populate LogicalChannel data

      for (int i=0; i<getEffectiveSizeC(); i++) {
        if (channelNames.containsKey(i)) {
          store.setChannelName(channelNames.get(i), 0, i);
        }
        if (pinholes.containsKey(i)) {
          store.setChannelPinholeSize(pinholes.get(i), 0, i);
        }
        if (emWaves != null && i < emWaves.length) {
          if (emWaves[i].intValue() > 0) {
            store.setChannelEmissionWavelength(
              new PositiveInteger(emWaves[i]), 0, i);
          }
          else {
            LOGGER.warn(
              "Expected positive value for EmissionWavelength; got {}",
              emWaves[i]);
          }
        }
        if (exWaves != null && i < exWaves.length) {
          if (exWaves[i].intValue() > 0) {
            store.setChannelExcitationWavelength(
              new PositiveInteger(exWaves[i]), 0, i);
          }
          else {
            LOGGER.warn(
              "Expected positive value for ExcitationWavelength; got {}",
              exWaves[i]);
          }
        }
      }

      // populate Laser data

      Integer[] lasers = wavelengths.keySet().toArray(new Integer[0]);
      Arrays.sort(lasers);
      for (int i=0; i<lasers.length; i++) {
        store.setLaserID(MetadataTools.createLSID("LightSource", 0, i), 0, i);
        if (wavelengths.get(lasers[i]) > 0) {
          store.setLaserWavelength(
            new PositiveInteger(wavelengths.get(lasers[i])), 0, i);
        }
        else {
          LOGGER.warn("Expected positive value for wavelength; got {}",
            wavelengths.get(lasers[i]));
        }
        store.setLaserType(getLaserType("Other"), 0, i);
        store.setLaserLaserMedium(getLaserMedium("Other"), 0, i);

        store.setLaserManufacturer(laserManufacturer, 0, i);
        store.setLaserModel(laserModel, 0, i);
        store.setLaserPower(laserPower, 0, i);
        store.setLaserRepetitionRate(laserRepetitionRate, 0, i);
      }

      if (lasers.length == 0 && laserManufacturer != null) {
        store.setLaserID(MetadataTools.createLSID("LightSource", 0, 0), 0, 0);
        store.setLaserType(getLaserType("Other"), 0, 0);
        store.setLaserLaserMedium(getLaserMedium("Other"), 0, 0);
        store.setLaserManufacturer(laserManufacturer, 0, 0);
        store.setLaserModel(laserModel, 0, 0);
        store.setLaserPower(laserPower, 0, 0);
        store.setLaserRepetitionRate(laserRepetitionRate, 0, 0);
      }

      // populate FilterSet data

      if (filterSetModel != null) {
        store.setFilterSetID(MetadataTools.createLSID("FilterSet", 0, 0), 0, 0);
        store.setFilterSetModel(filterSetModel, 0, 0);

        String dichroicID = MetadataTools.createLSID("Dichroic", 0, 0);
        String emFilterID = MetadataTools.createLSID("Filter", 0, 0);
        String exFilterID = MetadataTools.createLSID("Filter", 0, 1);

        store.setDichroicID(dichroicID, 0, 0);
        store.setDichroicModel(dichroicModel, 0, 0);
        store.setFilterSetDichroicRef(dichroicID, 0, 0);

        store.setFilterID(emFilterID, 0, 0);
        store.setFilterModel(emissionModel, 0, 0);
        store.setFilterSetEmissionFilterRef(emFilterID, 0, 0, 0);

        store.setFilterID(exFilterID, 0, 1);
        store.setFilterModel(excitationModel, 0, 1);
        store.setFilterSetExcitationFilterRef(exFilterID, 0, 0, 0);
      }

      // populate Objective data

      if (objectiveModel != null) store.setObjectiveModel(objectiveModel, 0, 0);
      if (immersion == null) immersion = "Other";
      store.setObjectiveImmersion(getImmersion(immersion), 0, 0);
      if (lensNA != null) store.setObjectiveLensNA(lensNA, 0, 0);
      if (workingDistance != null) {
        store.setObjectiveWorkingDistance(workingDistance, 0, 0);
      }
      if (magnification != null) {
        store.setObjectiveCalibratedMagnification(magnification, 0, 0);
      }
      store.setObjectiveCorrection(getCorrection("Other"), 0, 0);

      // link Objective to Image
      String objectiveID = MetadataTools.createLSID("Objective", 0, 0);
      store.setObjectiveID(objectiveID, 0, 0);
      store.setImageObjectiveSettingsID(objectiveID, 0);

      // populate Detector data

      String detectorID = MetadataTools.createLSID("Detector", 0, 0);
      store.setDetectorID(detectorID, 0, 0);
      store.setDetectorManufacturer(detectorManufacturer, 0, 0);
      store.setDetectorModel(detectorModel, 0, 0);
      store.setDetectorType(getDetectorType("Other"), 0, 0);

      for (Integer key : gains.keySet()) {
        int index = key.intValue();
        if (index < getEffectiveSizeC()) {
          store.setDetectorSettingsGain(gains.get(key), 0, index);
          store.setDetectorSettingsID(detectorID, 0, index);
        }
      }

      // populate Experimenter data

      if (lastName != null) {
        String experimenterID = MetadataTools.createLSID("Experimenter", 0);
        store.setExperimenterID(experimenterID, 0);
        store.setExperimenterLastName(lastName, 0);
        store.setExperimenterDisplayName(lastName, 0);
      }

      // populate StagePosition data

      if (stagePos != null) {
        for (int i=0; i<getImageCount(); i++) {
          if (stagePos.length > 0) {
            store.setPlanePositionX(stagePos[0], 0, i);
            addGlobalMeta("X position for position #1", stagePos[0]);
          }
          if (stagePos.length > 1) {
            store.setPlanePositionY(stagePos[1], 0, i);
            addGlobalMeta("Y position for position #1", stagePos[1]);
          }
          if (stagePos.length > 2) {
            store.setPlanePositionZ(stagePos[2], 0, i);
            addGlobalMeta("Z position for position #1", stagePos[2]);
          }
        }
      }

      if (exposureTime != null) {
        for (int i=0; i<getImageCount(); i++) {
          store.setPlaneExposureTime(exposureTime, 0, i);
        }
      }
    }
  }

  // -- Helper methods --

  /*
   * String tokenizer for parsing metadata. Splits on any white-space
   * characters. Tabs and spaces are often used interchangeably in real-life ICS
   * files.
   *
   * Also splits on 0x04 character which appears in "paul/csarseven.ics" and
   * "paul/gci/time resolved_1.ics".
   *
   * Also respects double quote marks, so that
   *   Modules("Confocal C1 Grabber").BarrierFilter(2)
   * is just one token.
   *
   * If not for the last requirement, the one line
   *   String[] tokens = line.split("[\\s\\x04]+");
   * would work.
   */
  private String[] tokenize(String line) {
    List<String> tokens = new ArrayList<String>();
    boolean inWhiteSpace = true;
    boolean withinQuotes = false;
    StringBuffer token = null;
    for (int i = 0; i < line.length(); ++i) {
      char c = line.charAt(i);
      if (Character.isWhitespace(c) || c == 0x04) {
        if (withinQuotes) {
          // retain white space within quotes
          token.append(c);
        }
        else if (!inWhiteSpace) {
          // put out pending token string
          inWhiteSpace = true;
          if (token.length() > 0) {
            tokens.add(token.toString());
            token = null;
          }
        }
      }
      else {
        if ('"' == c) {
          // toggle quotes
          withinQuotes = !withinQuotes;
        }
        if (inWhiteSpace) {
          inWhiteSpace = false;
          // start a new token string
          token = new StringBuffer();
        }
        // build token string
        token.append(c);
      }
    }
    // put out any pending token strings
    if (null != token && token.length() > 0) {
      tokens.add(token.toString());
    }
    return tokens.toArray(new String[0]);
  }

  /* Given a list of tokens and an array of lists of regular expressions, tries
   * to find a match.  If no match is found, looks in OTHER_KEYS.
   */
  String[] findKeyValue(String[] tokens, String[][] regexesArray) {
    String[] keyValue = findKeyValueForCategory(tokens, regexesArray);
    if (null == keyValue) {
      keyValue = findKeyValueOther(tokens, OTHER_KEYS);
    }
    if (null == keyValue) {
      String key = tokens[0];
      String value = concatenateTokens(tokens, 1, tokens.length);
      keyValue = new String[] { key, value };
    }
    return keyValue;
  }

  /*
   * Builds a string from a list of tokens.
   */
  private String concatenateTokens(String[] tokens, int start, int stop) {
    StringBuffer returnValue = new StringBuffer();
    for (int i = start; i < tokens.length && i < stop; ++i) {
      returnValue.append(tokens[i]);
      if (i < stop - 1) {
        returnValue.append(' ');
      }
    }
    return returnValue.toString();
  }

  /*
   * Given a list of tokens and an array of lists of regular expressions, finds
   * a match.  Returns key/value pair if matched, null otherwise.
   *
   * The first element, tokens[0], has already been matched to a category, i.e.
   * 'history', and the regexesArray is category-specific.
   */
  private String[] findKeyValueForCategory(String[] tokens,
                                           String[][] regexesArray) {
    String[] keyValue = null;
    int index = 0;
    for (String[] regexes : regexesArray) {
      if (compareTokens(tokens, 1, regexes, 0)) {
        int splitIndex = 1 + regexes.length; // add one for the category
        String key = concatenateTokens(tokens, 0, splitIndex);
        String value = concatenateTokens(tokens, splitIndex, tokens.length);
        keyValue = new String[] { key, value };
        break;
      }
      ++index;
    }
    return keyValue;
  }

  /* Given a list of tokens and an array of lists of regular expressions, finds
   * a match.  Returns key/value pair if matched, null otherwise.
   *
   * The first element, tokens[0], represents a category and is skipped.  Look
   * for a match of a list of regular expressions anywhere in the list of tokens.
   */
  private String[] findKeyValueOther(String[] tokens, String[][] regexesArray) {
    String[] keyValue = null;
    for (String[] regexes : regexesArray) {
      for (int i = 1; i < tokens.length - regexes.length; ++i) {
        // does token match first regex?
        if (tokens[i].toLowerCase().matches(regexes[0])) {
          // do remaining tokens match remaining regexes?
          if (1 == regexes.length || compareTokens(tokens, i + 1, regexes, 1)) {
            // if so, return key/value
            int splitIndex = i + regexes.length;
            String key = concatenateTokens(tokens, 0, splitIndex);
            String value = concatenateTokens(tokens, splitIndex, tokens.length);
            keyValue = new String[] { key, value };
            break;
          }
        }
      }
      if (null != keyValue) {
        break;
      }
    }
    return keyValue;
  }

  /*
   * Compares a list of tokens with a list of regular expressions.
   */
  private boolean compareTokens(String[] tokens, int tokenIndex,
                                String[] regexes, int regexesIndex) {
    boolean returnValue = true;
    int i, j;
    for (i = tokenIndex, j = regexesIndex; j < regexes.length; ++i, ++j) {
      if (i >= tokens.length || !tokens[i].toLowerCase().matches(regexes[j])) {
        returnValue = false;
        break;
      }
    }
    return returnValue;
  }

  /** Splits the given string into a list of {@link Double}s. */
  private Double[] splitDoubles(String v) {
    StringTokenizer t = new StringTokenizer(v);
    Double[] values = new Double[t.countTokens()];
    for (int n=0; n<values.length; n++) {
      String token = t.nextToken().trim();
      try {
        values[n] = new Double(token);
      }
      catch (NumberFormatException e) {
        LOGGER.debug("Could not parse double value '{}'", token, e);
      }
    }
    return values;
  }

  /** Verifies that a unit matches the expected value. */
  private boolean checkUnit(String actual, String... expected) {
    if (actual == null || actual.equals("")) return true; // undefined is OK
    for (String exp : expected) {
      if (actual.equals(exp)) return true; // unit matches expected value
    }
    LOGGER.debug("Unexpected unit '{}'; expected '{}'", actual, expected);
    return false;
  }

}
