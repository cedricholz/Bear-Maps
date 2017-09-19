import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */

    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     *
     * @param dbPath Path to the XML file to be parsed.
     */
    Trie t;

    HashMap<String, LinkedList<Map<String, Object>>> places = new HashMap<>();

    public void addToPlaces(String name, long id, double lon, double lat) {

        HashMap<String, Object> h = new HashMap<>();
        h.put("name", name);
        h.put("lon", lon);
        h.put("id", id);
        h.put("lat", lat);

        String cleanedName = cleanString(name);

        if (!places.containsKey(cleanedName)) {
            LinkedList<Map<String, Object>> temp = new LinkedList<>();
            temp.add(h);
            places.put(cleanedName, temp);
        } else {
            places.get(cleanedName).add(h);
        }

    }


    public List getPlace(String name) {
        return places.get(name);
    }

    public GraphDB(String dbPath) {
        t = new Trie();
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }

    private class TrieNode implements Comparable<TrieNode> {
        String placeName = "";
        HashMap<String, TrieNode> children = new HashMap<>();
        LinkedList<String> legitChildren = new LinkedList();

        private TrieNode(String placeName) {
            this.placeName = placeName;
        }

        @Override
        public int compareTo(TrieNode o) {
            if (this.placeName.compareTo(o.placeName) > 0) {
                return 1;
            } else if (this.placeName.compareTo(o.placeName) < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public boolean equals(Object y) {
            if (y == null) {
                return false;
            }
            if (y == this) {
                return true;
            }
            if (y.getClass() != this.getClass()) {
                return false;
            }
            TrieNode trieNode = (TrieNode) y;
            return this.placeName.equals((trieNode).placeName);
        }

        @Override
        public int hashCode() {
            return placeName.hashCode();
        }
    }


    public class Trie {
        TrieNode root = new TrieNode("root");

        public Trie() {
        }

        public void addNode(String placeToAdd, int upToChar, TrieNode parent) {
            String cleanPlaceToAdd = cleanString(placeToAdd);


            if (cleanPlaceToAdd.length() == 0) {
                return;
            }

            String curSub = cleanPlaceToAdd.substring(0, upToChar);


            TrieNode toAdd = new TrieNode(curSub);

            if (upToChar == cleanPlaceToAdd.length()) {
                if (parent.children != null) {
                    parent.legitChildren.add(placeToAdd);
                } else {
                    toAdd = new TrieNode(curSub);
                    parent.children.put(curSub, toAdd);
                    parent.legitChildren.add(placeToAdd);
                }
                return;
            } else if (!parent.children.containsKey(curSub)) {
                parent.children.put(curSub, toAdd);
                addNode(placeToAdd, upToChar + 1, toAdd);
            } else {
                addNode(placeToAdd, upToChar + 1, parent.children.get(curSub));
            }
        }
    }


    public LinkedList<String> getAllLegitChildren(String prefix, TrieNode t) {

        if (t.children.size() == 0) {
            return new LinkedList<>();
        }
        LinkedList<String> r = new LinkedList<>();

        for (TrieNode curNode : t.children.values()) {

            String currentPlaceName = curNode.placeName;

            //placeName size <= prefix size
            if (currentPlaceName.length() <= prefix.length()) {
                if (prefix.startsWith(currentPlaceName)) {
                    if (currentPlaceName.length() == prefix.length()) {
                        r.addAll(curNode.legitChildren);
                    }
                    r.addAll(getAllLegitChildren(prefix, curNode));
                    break;
                }
            }
            //Prefix size > placeName size
            else {
                r.addAll(curNode.legitChildren);
                r.addAll(getAllLegitChildren(prefix, curNode));
            }
        }
        return r;
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     *
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     * Remove nodes with no connections from the graph.
     * While this does not guarantee that any two nodes in the remaining graph are connected,
     * we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        for (Iterator<HashMap.Entry<Long, Node>>
             iter = nodes.entrySet().iterator(); iter.hasNext(); ) {
            HashMap.Entry<Long, Node> entry = iter.next();
            Node n = entry.getValue();
            if (n.adjacentNodes.size() == 0) {
                iter.remove();
            }
        }
    }

    /**
     * Returns an iterable of all vertex IDs in the graph.
     */
    Iterable<Long> vertices() {
        ArrayList<Long> vertexIds = new ArrayList<>();
        for (long key : nodes.keySet()) {
            long k = key;
            vertexIds.add(k);
        }

        return vertexIds;
    }

    /**
     * Returns ids of all vertices adjacent to v.
     */
    Iterable<Long> adjacent(long v) {
        Node n = nodes.get(v);
        ArrayList<Long> adjacentVertices = new ArrayList<>();
        for (long key : n.adjacentNodes.keySet()) {
            long k = key;
            adjacentVertices.add(k);
        }
        return adjacentVertices;
    }

    /**
     * Returns the Euclidean distance between vertices v and w, where Euclidean distance
     * is defined as sqrt( (lonV - lonW)^2 + (latV - latW)^2 ).
     */
    double distance(long v, long w) {

        Node n1 = nodes.get(v);
        Node n2 = nodes.get(w);

        double lonV = lon(v);
        double lonW = lon(w);
        double latV = lat(v);
        double latW = lat(w);
        double r = Math.sqrt(Math.pow((lonV - lonW), 2.0) + Math.pow(latV - latW, 2.0));

        return r;
    }

    /**
     * Returns the vertex id closest to the given longitude and latitude.
     */
    long closest(double lon, double lat) {
        Node goal = new Node(0, lon, lat, "");
        nodes.put((long) 0, goal);
        long closestNodeId = 0;
        double closestDistance = Double.MAX_VALUE;

        for (long key : nodes.keySet()) {
            long keyLong = key;
            if (keyLong != 0) {
                double d = distance(0, keyLong);
                if (d < closestDistance) {
                    closestDistance = d;
                    closestNodeId = keyLong;
                }
            }
        }
        nodes.remove(goal);
        return closestNodeId;
    }

    /**
     * Longitude of vertex v.
     */
    double lon(long v) {
        Node n = nodes.get(v);
        double lon = n.lon;
        return lon;
    }

    /**
     * Latitude of vertex v.
     */
    double lat(long v) {
        Node n = nodes.get(v);
        double lat = n.lat;
        return lat;
    }

    private class Node {
        private long id;

        private double lon;
        private double lat;
        private String name;


        private HashMap<Long, Node> adjacentNodes = new HashMap<>();

        private Node(long id, double lon, double lat, String name) {
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            this.name = name;
        }
    }

    public ArrayList<Long> getAdjacentNodeIds(long id) {
        Node n = nodes.get(id);
        ArrayList<Long> r = new ArrayList<>();
        for (Long key : n.adjacentNodes.keySet()) {
            r.add(key);
        }
        return r;
    }

    public double getNodeLat(long id) {
        if (nodes.containsKey(id)) {
            Node n = nodes.get(id);
            return n.lat;
        } else {
            return 0.0;
        }
    }

    public double getNodeLon(long id) {
        if (nodes.containsKey(id)) {
            Node n = nodes.get(id);
            return n.lon;
        } else {
            return 0.0;
        }
    }

    ArrayList<Long> currentWay = new ArrayList<>();

    HashMap<Long, Node> nodes = new HashMap<>();

    public void addNode(long id, double lon, double lat) {
        Node n = new Node(id, lon, lat, "");
        nodes.put(id, n);
    }

    public void addEdge(Node n1, Node n2) {
        n1.adjacentNodes.put(n2.id, n2);
        n2.adjacentNodes.put(n1.id, n1);
    }

    boolean validWay = false;

    public void addToCurrentWay(String id) {
        long longId = Long.parseLong(id);
        currentWay.add(longId);
    }

    public void addCurrentWay() {
        for (int i = 0; i < currentWay.size() - 1; i++) {
            Node n1 = nodes.get(currentWay.get(i));
            Node n2 = nodes.get(currentWay.get(i + 1));
            addEdge(n1, n2);
        }

        currentWay.clear();
    }


}
