//
// ConvertToOmeTiff.java
//

import loci.common.services.ServiceFactory;
import loci.formats.ImageReader;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import loci.formats.out.OMETiffWriter;

/**
 * Converts the given files to OME-TIFF format.
 *
 * <dl><dt><b>Source code:</b></dt>
 * <dd><a href="http://trac.openmicroscopy.org.uk/ome/browser/bioformats.git/components/bio-formats/utils/ConvertToOmeTiff.java">Trac</a>,
 * <a href="http://git.openmicroscopy.org/?p=bioformats.git;a=blob;f=components/bio-formats/utils/ConvertToOmeTiff.java;hb=HEAD">Gitweb</a></dd></dl>
 */
public class ConvertToOmeTiff {

  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.out.println("Usage: java ConvertToOmeTiff file1 file2 ...");
      return;
    }
    ImageReader reader = new ImageReader();
    OMETiffWriter writer = new OMETiffWriter();
    for (int i=0; i<args.length; i++) {
      String id = args[i];
      int dot = id.lastIndexOf(".");
      String outId = (dot >= 0 ? id.substring(0, dot) : id) + ".ome.tif";
      System.out.print("Converting " + id + " to " + outId + " ");

      // record metadata to OME-XML format
      ServiceFactory factory = new ServiceFactory();
      OMEXMLService service = factory.getInstance(OMEXMLService.class);
      IMetadata omexmlMeta = service.createOMEXMLMetadata();
      reader.setMetadataStore(omexmlMeta);
      reader.setId(id);

      // configure OME-TIFF writer
      writer.setMetadataRetrieve(omexmlMeta);
      writer.setId(outId);
      //writer.setCompression("J2K");

      // write out image planes
      int seriesCount = reader.getSeriesCount();
      for (int s=0; s<seriesCount; s++) {
        reader.setSeries(s);
        writer.setSeries(s);
        int planeCount = reader.getImageCount();
        for (int p=0; p<planeCount; p++) {
          byte[] plane = reader.openBytes(p);
          // write plane to output file
          writer.saveBytes(p, plane);
          System.out.print(".");
        }
      }
      writer.close();
      reader.close();
      System.out.println(" [done]");
    }
  }

}
