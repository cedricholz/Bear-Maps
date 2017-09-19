import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    // Recommended: QuadTree instance variable. You'll need to make
    //              your own QuadTree since there is no built-in quadtree in Java.

    /**
     * imgRoot is the name of the directory containing the images.
     * You may not actually need this for your class.
     */
    QuadTree q;

    public Rasterer(String imgRoot) {
        q = new QuadTree();
    }


    public class QuadTree {

        private Node root = new Node("root", 0);

        public QuadTree() {
            root.x1 = -122.2998046875;
            root.y1 = 37.892195547244356;
            root.x2 = -122.2119140625;
            root.y2 = 37.82280243352756;
            fillTree(root);
        }


        public void setTopLeft(Node parent, Node r) {
            r.x1 = parent.x1;
            r.y1 = parent.y1;

            r.x2 = (parent.x1 - parent.x2) / 2 + parent.x2;
            r.y2 = (parent.y1 - parent.y2) / 2 + parent.y2;
        }

        public void setTopRight(Node parent, Node r) {
            r.x1 = (parent.x1 - parent.x2) / 2 + parent.x2;
            r.y1 = parent.y1;

            r.x2 = parent.x2;
            r.y2 = (parent.y1 - parent.y2) / 2 + parent.y2;
        }

        public void setBottomLeft(Node parent, Node r) {
            r.x1 = parent.x1;
            r.y1 = (parent.y1 - parent.y2) / 2 + parent.y2;

            r.x2 = (parent.x1 - parent.x2) / 2 + parent.x2;
            r.y2 = parent.y2;
        }

        public void setBottomRight(Node parent, Node r) {
            r.x1 = (parent.x1 - parent.x2) / 2 + parent.x2;
            r.y1 = (parent.y1 - parent.y2) / 2 + parent.y2;
            r.x2 = parent.x2;
            r.y2 = parent.y2;
        }

        public void fillTree(Node r) {
            if (r.fileName.equals("root")) {
                Node tl = new Node("1", 1);
                setTopLeft(root, tl);

                Node tr = new Node("2", 1);
                setTopRight(root, tr);

                Node bl = new Node("3", 1);
                setBottomLeft(root, bl);

                Node br = new Node("4", 1);
                setBottomRight(root, br);

                r.topLeft = tl;
                r.topRight = tr;
                r.bottomLeft = bl;
                r.bottomRight = br;

                fillTree(tl);
                fillTree(tr);
                fillTree(bl);
                fillTree(br);

            } else {
                int n = Integer.parseInt(r.fileName);
                if (n < 4444441) {
                    String currentFile = r.fileName;
                    Node tl = new Node(currentFile + "1", r.N + 1);
                    setTopLeft(r, tl);
                    Node tr = new Node(currentFile + "2", r.N + 1);
                    setTopRight(r, tr);
                    Node bl = new Node(currentFile + "3", r.N + 1);
                    setBottomLeft(r, bl);
                    Node br = new Node(currentFile + "4", r.N + 1);
                    setBottomRight(r, br);

                    r.topLeft = tl;
                    r.topRight = tr;
                    r.bottomLeft = bl;
                    r.bottomRight = br;

                    fillTree(tl);
                    fillTree(tr);
                    fillTree(bl);
                    fillTree(br);
                }
            }
        }


    }

    private class Node {
        private Node topLeft;
        private Node topRight;
        private Node bottomLeft;
        private Node bottomRight;
        private String fileName;
        private double x1;
        private double x2;
        private double y1;
        private double y2;

        private int N;

        private Node(String fileName, int N) {
            this.fileName = fileName;
            this.N = N;
        }
    }


    public int getGoalDepth(double lonDpp) {
        int digits = 1;
        double tempDpp = 49.1;
        while (tempDpp >= lonDpp) {
            tempDpp = 49.1 / Math.pow(2, digits);
            digits += 1;
        }
        if (digits > 7) {
            return 7;
        } else {
            return digits;
        }
    }

    public boolean validRange(Node c, Node goal) {
        if (c.x1 > goal.x2 || goal.x1 > c.x2) {
            return false;
        }
        if (c.y1 < goal.y2 || goal.y1 < c.y2) {
            return false;
        }
        return true;
    }

    public ArrayList<Node> findValidImageNodes(Node c,
                                               int N, Node goal, ArrayList<Node> validNodes) {
        if (c == null) {
            return validNodes;
        } else {
            if (c.N == N && (validRange(c, goal))) {
                validNodes.add(c);
            }
            if (c.N < N) {
                if (validRange(c.topLeft, goal)) {
                    validNodes.addAll(findValidImageNodes(
                            c.topLeft, N, goal, new ArrayList<>()));
                }
                if (validRange(c.topRight, goal)) {
                    validNodes.addAll(findValidImageNodes(
                            c.topRight, N, goal, new ArrayList<>()));
                }
                if (validRange(c.bottomLeft, goal)) {
                    validNodes.addAll(findValidImageNodes(
                            c.bottomLeft, N, goal, new ArrayList<>()));
                }
                if (validRange(c.bottomRight, goal)) {
                    validNodes.addAll(findValidImageNodes(
                            c.bottomRight, N, goal, new ArrayList<>()));
                }
            }
            return validNodes;
        }
    }

    public String[][] getImageArray(ArrayList<Node> validNodes) {

        int rowSize = 0;
        int colSize = 0;

        Node n = validNodes.get(0);

        double smallestX = n.x1;
        double largestY = n.y1;

        double largestX = n.x2;
        double smallestY = n.y2;

        for (int i = 1; i < validNodes.size(); i++) {
            double x1 = validNodes.get(i).x1;
            double x2 = validNodes.get(i).x2;
            double y1 = validNodes.get(i).y1;
            double y2 = validNodes.get(i).y2;
            if (x1 < smallestX) {
                smallestX = x1;
            }
            if (x2 > largestX) {
                largestX = x2;
            }
            if (y1 > largestY) {
                largestY = y1;
            }
            if (y2 < smallestY) {
                smallestY = y2;
            }
        }

        double w = largestX - smallestX;
        double h = largestY - smallestY;


        double nodeWidth = (validNodes.get(0).x2 - validNodes.get(0).x1);
        colSize = (int) Math.round(w / nodeWidth);

        double nodeHeight = (validNodes.get(0).y1 - validNodes.get(0).y2);
        rowSize = (int) Math.round(h / nodeHeight);

        ArrayList<Node> imageArrayList = new ArrayList<>();

        String[][] imageArray = new String[rowSize][colSize];
        int i = 0;
        int j = 0;
        int count = 0;
        while (validNodes.size() > 0) {
            double maxHeight = validNodes.get(0).y1;
            ArrayList<Node> copy = new ArrayList<>();
            copy.addAll(validNodes);
            count = 0;
            for (int k = 0; k < copy.size(); k++) {
                Node curNode = copy.get(k);
                if (curNode.y1 == maxHeight) {
                    count++;
                    imageArray[i][j] = "img/" + curNode.fileName + ".png";
                    j += 1;
                    imageArrayList.add(curNode);
                    validNodes.remove(curNode);
                    if (count == colSize) {
                        i += 1;
                        j = 0;
                        break;
                    }
                }
            }

            copy.clear();
            maxHeight = 0;
        }

        validNodes.addAll(imageArrayList);

        return imageArray;
    }


    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     * The grid of images must obey the following properties, where image in the
     * grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel
     * (LonDPP) possible, while still covering less than or equal to the amount of
     * longitudinal distance per pixel in the query box for the user viewport size. </li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the
     * above condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     * </p>
     * <p>
     * //@param params Map of the HTTP GET request's query parameters - the query box and
     * the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     * Can also be interpreted as the length of the numbers in the image
     * string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     * forget to set this to true! <br>
     * @see //#REQUIRED_RASTER_REQUEST_PARAMS
     */


    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Map<String, Object> results = new HashMap<>();

        Node goal = new Node("GOAL", 0);
        goal.x1 = params.get("ullon");
        goal.x2 = params.get("lrlon");
        goal.y1 = params.get("ullat");
        goal.y2 = params.get("lrlat");

        double w = params.get("w");

        double sl = 288200;
        double xDist = goal.x2 - goal.x1;
        double widthFeet = xDist * sl;
        double lonDpp = widthFeet / w;

        int digits = getGoalDepth(lonDpp);

        ArrayList<Node> validNodes = findValidImageNodes(q.root,
                digits, goal, new ArrayList<>());


        String[][] imageArray = getImageArray(validNodes);

        results.put("render_grid", imageArray);
        results.put("raster_ul_lon", validNodes.get(0).x1);
        results.put("raster_ul_lat", validNodes.get(0).y1);
        results.put("raster_lr_lon", validNodes.get(validNodes.size() - 1).x2);
        results.put("raster_lr_lat", validNodes.get(validNodes.size() - 1).y2);
        results.put("depth", digits);
        results.put("query_success", true);

        return results;
    }

}
