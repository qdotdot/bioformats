/*
 * ome.xml.r201004.enums.handlers.LaserMediumHandler
 *
 *-----------------------------------------------------------------------------
 *
 *  Copyright (C) 2005-@year@ Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee,
 *      University of Wisconsin-Madison
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *-----------------------------------------------------------------------------
 */

/*-----------------------------------------------------------------------------
 *
 * THIS IS AUTOMATICALLY GENERATED CODE.  DO NOT MODIFY.
 * Created by callan via xsd-fu on 2010-05-24 16:13:00.333355
 *
 *-----------------------------------------------------------------------------
 */

package ome.xml.r201004.enums.handlers;

import java.util.Hashtable;
import java.util.List;

import ome.xml.r201004.enums.Enumeration;
import ome.xml.r201004.enums.EnumerationException;
import ome.xml.r201004.enums.LaserMedium;

/**
 * Enumeration handler for LaserMedium.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="https://skyking.microscopy.wisc.edu/trac/java/browser/trunk/components/bio-formats/src/loci/formats/enums/handler/LaserMediumHandler.java">Trac</a>,
 * <a href="https://skyking.microscopy.wisc.edu/svn/java/trunk/components/bio-formats/src/loci/formats/enums/handler/LaserMediumHandler.java">SVN</a></dd></dl>
 */
public class LaserMediumEnumHandler implements IEnumerationHandler {

  // -- Fields --

  /** Every LaserMedium value must match one of these patterns. */
  private static final Hashtable<String, String> patterns = makePatterns();

  private static Hashtable<String, String> makePatterns() {
    Hashtable<String, String> p = new Hashtable<String, String>();
    p.put("^\\s*Cu\\s*", "Cu");
    p.put("^\\s*Ag\\s*", "Ag");
    p.put("^\\s*ArFl\\s*", "ArFl");
    p.put("^\\s*ArCl\\s*", "ArCl");
    p.put("^\\s*KrFl\\s*", "KrFl");
    p.put("^\\s*KrCl\\s*", "KrCl");
    p.put("^\\s*XeFl\\s*", "XeFl");
    p.put("^\\s*XeCl\\s*", "XeCl");
    p.put("^\\s*XeBr\\s*", "XeBr");
    p.put("^\\s*N\\s*", "N");
    p.put("^\\s*Ar\\s*", "Ar");
    p.put("^\\s*Kr\\s*", "Kr");
    p.put("^\\s*Xe\\s*", "Xe");
    p.put("^\\s*HeNe\\s*", "HeNe");
    p.put("^\\s*HeCd\\s*", "HeCd");
    p.put("^\\s*CO\\s*", "CO");
    p.put("^\\s*CO2\\s*", "CO2");
    p.put("^\\s*H2O\\s*", "H2O");
    p.put("^\\s*HFl\\s*", "HFl");
    p.put("^\\s*NdGlass\\s*", "NdGlass");
    p.put("^\\s*NdYAG\\s*", "NdYAG");
    p.put("^\\s*ErGlass\\s*", "ErGlass");
    p.put("^\\s*ErYAG\\s*", "ErYAG");
    p.put("^\\s*HoYLF\\s*", "HoYLF");
    p.put("^\\s*HoYAG\\s*", "HoYAG");
    p.put("^\\s*Ruby\\s*", "Ruby");
    p.put("^\\s*TiSapphire\\s*", "TiSapphire");
    p.put("^\\s*Alexandrite\\s*", "Alexandrite");
    p.put("^\\s*Rhodamine6G\\s*", "Rhodamine6G");
    p.put("^\\s*CoumarinC30\\s*", "CoumarinC30");
    p.put("^\\s*GaAs\\s*", "GaAs");
    p.put("^\\s*GaAlAs\\s*", "GaAlAs");
    p.put("^\\s*EMinus\\s*", "EMinus");
    p.put("^\\s*Other\\s*", "Other");
    return p;
  }

  // -- IEnumerationHandler API methods --

  /* @see IEnumerationHandler#getEnumeration(String) */
  public Enumeration getEnumeration(String value)
    throws EnumerationException {
    for (String pattern : patterns.keySet()) {
      if (value.toLowerCase().matches(pattern.toLowerCase())) {
        String v = patterns.get(pattern);
        return LaserMedium.fromString(v);
      }
    }
    throw new EnumerationException(this.getClass().getName() +
     " could not find enumeration for " + value);
  }

  /* @see IEnumerationHandler#getEntity() */
  public Class<? extends Enumeration> getEntity() {
    return LaserMedium.class;
  }

}