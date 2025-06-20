package uk.ac.standrews.cs.valipop.utils.addressLookup;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Standalone executable class
 * 
 * @author Tom Dalton (tsd4@st-andrews.ac.uk)
 */
public class PlanetXML {

    static Map<String, Coords> nodeMap = new HashMap<>();

    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, InvalidCoordSet, InterruptedException {

        String file = "/home/tsd4/sweden-residential-ways.ser";

        Cache cache = new Cache();
        try {
            cache = Cache.readFromFile(file);
        } catch (ClassNotFoundException e) {
            System.err.println("Cache class not found in serial input file");
            System.exit(1);
        } catch (FileNotFoundException e) {
            System.err.println("No cache found at file path - proceeding to construct new cache");
            cache.setFilePath(file);
        } catch (NotSerializableException e) {
            System.err.println("Cache malformed? If from previous error then please delete: " + file);
            System.exit(1);
        }

        ReverseGeocodeLookup rgl = new ReverseGeocodeLookup(cache);

        parseXML("/home/tsd4/sweden-latest.osm", rgl);

        cache.writeToFile();

        System.out.println("END OF PLANET XML REACHED!!!");
    }

    @SuppressWarnings("unused")
    private static void addResidentialWaysToCache(Document document, ReverseGeocodeLookup rgl) throws InterruptedException, InvalidCoordSet, IOException, APIOverloadedException, URISyntaxException {

        NodeList wayList = document.getElementsByTagName("way");

        for(int i = 0; i < wayList.getLength(); i++) {

            if(i != 0 && i % 1000 == 0) {
                rgl.cache.writeToFile();
            }

            Element way = getElement(wayList, i);

            if(way != null) {

                NodeList ownedNodes = way.getChildNodes();
                boolean residentialWay = false;
                ArrayList<Coords> nodes = new ArrayList<>();

                for(int j = 0 ; j < ownedNodes.getLength(); j++) {

                    Element node = getElement(ownedNodes, j);

                    if(node != null) {

                        String nodeID = node.getAttribute("ref");

                        if(nodeID.equals("")) {
                            String key = node.getAttribute("k");
                            if(key.equals("highway")) {
                                String value = node.getAttribute("v");
                                residentialWay = residentialWay || value.equals("residential");
                            }

                        } else {
                            Coords c = nodeMap.get(nodeID);

                            if (c != null) {
                                nodes.add(c);
                            } else {
                                System.out.println(nodeID);
                            }
                        }
                    }
                }

                if(residentialWay) {
                    Coords middle = getMiddleElement(nodes);

                    if(middle != null) {
                        rgl.getArea(middle);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    private static void populateNodeMap(Document document) {
        NodeList nodeList = document.getElementsByTagName("node");

        for(int n = 0; n < nodeList.getLength(); n++) {

            Element element = getElement(nodeList, n);

            if(element != null) {

                String id = element.getAttribute("id");
                String lat = element.getAttribute("lat");
                String lon = element.getAttribute("lon");

                Coords coords = new Coords(lat, lon);

                nodeMap.put(id, coords);
            }
        }
    }

    @SuppressWarnings("unused")
    private static Document readInDocument(String pathToDocument) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        return builder.parse(new File( pathToDocument ));
    }

    private static Coords getMiddleElement(ArrayList<Coords> nodes) {

        return nodes.size() != 0 ? nodes.get(nodes.size() / 2) : null;
    }

    private static void parseXML(String fileName, ReverseGeocodeLookup rgl) throws InterruptedException, InvalidCoordSet, IOException {

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileName));

            String wayId = null;
            ArrayList<String> waysNodes = new ArrayList<>();

            boolean residentialWay = false;

            while(xmlEventReader.hasNext()){

                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()){
                    StartElement startElement = xmlEvent.asStartElement();

                    if(startElement.getName().getLocalPart().equals("node")){

                        //Get the 'id' attribute from Employee element
                        String id = startElement.getAttributeByName(new QName("id")).getValue();
                        String lat = startElement.getAttributeByName(new QName("lat")).getValue();
                        String lon = startElement.getAttributeByName(new QName("lon")).getValue();

                        Coords coords = new Coords(lat, lon);

                        nodeMap.put(id, coords);

                    } else if(startElement.getName().getLocalPart().equals("way")){

                        wayId = startElement.getAttributeByName(new QName("id")).getValue();

                    } else if(startElement.getName().getLocalPart().equals("nd")){

                        String nodeRef = startElement.getAttributeByName(new QName("ref")).getValue();
                        waysNodes.add(nodeRef);

                    } else if(startElement.getName().getLocalPart().equals("tag")) {

                        String key = startElement.getAttributeByName(new QName("k")).getValue();

                        if(key.equals("highway")) {
                            String value = startElement.getAttributeByName(new QName("v")).getValue();
                            residentialWay = residentialWay || value.equals("residential");
                        }
                    }
                }

                //if Employee end element is reached, add employee object to list
                if(xmlEvent.isEndElement()){
                    EndElement endElement = xmlEvent.asEndElement();
                    if(endElement.getName().getLocalPart().equals("way")){

                        if(wayId != null) {
                            if(residentialWay) {
                                if (waysNodes.size() != 0) {
                                    String middlingNode = waysNodes.get(waysNodes.size() / 2);
                                    Coords mid = nodeMap.get(middlingNode);

                                    if (mid != null) {

                                        boolean retrieved = false;

                                        while(!retrieved) {
                                            try {
                                                rgl.getArea(mid);
                                                retrieved = true;
                                            } catch (APIOverloadedException e) {
                                                System.out.println("Block. 30 min pause");
                                                Thread.sleep(1000 * 60 * 30);
                                            } catch (URISyntaxException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }
                                }
                                residentialWay = false;
                            }
                        }

                        waysNodes.clear();
                    }
                }
            }

        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public static Element getElement(NodeList nodeList, int index) {

        Node node = nodeList.item(index);

        if(node.getNodeType() == Node.ELEMENT_NODE) {
            return (Element) node;
        }

        return null;
    }
}
