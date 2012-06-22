import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import java.io.IOException;

public class ApplicationInfo {
    private static void parseAndroidManifest(String path) {
        // System.out.println(path);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document dom;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            dom = db.parse(path);

            // getting package information
            Element manifest = dom.getDocumentElement();
            String pakkage = manifest.getAttribute("package");

            // getting activity name
            String activity = ((Element)dom.getElementsByTagName("activity").item(0)).getAttribute("android:name");
            System.out.println(String.format("%s/.%s", pakkage, activity.replace(".", "")));
        } catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(SAXException se) {
			se.printStackTrace();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
        
    }

    public static void main(String[] args) {
        String path;
        if(args.length > 0) {
            path = args[0];
        } else {
            path = System.getProperty("user.dir") + "/../AndroidManifest.xml";
        }
        parseAndroidManifest(path);
    }
}
