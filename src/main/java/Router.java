import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.PriorityQueue;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {


    private static class Point implements Comparable<Point> {
        GraphDB g;
        long id;
        double distanceFromStart = 0;
        Point prev;

        double lon;
        double lat;

        double distance = 0;
        double priority = 0;
        ArrayList<Long> adjacentNodeIds = new ArrayList<>();


        private Point() {
        }


        private Point(GraphDB g, long id, long destinationId,
                      Point prev, double distanceFromStart) {
            this.g = g;
            this.id = id;
            this.prev = prev;
            this.distanceFromStart = distanceFromStart;

            this.lon = g.getNodeLat(id);
            this.lat = g.getNodeLon(id);

            this.distance = g.distance(id, destinationId);
            this.priority = distanceFromStart + this.distance;
            this.adjacentNodeIds = g.getAdjacentNodeIds(id);
        }

        @Override
        public int compareTo(Point o) {
            if (this.priority > o.priority) {
                return 1;
            } else if (this.priority < o.priority) {
                return -1;
            } else {
                return 0;
            }
        }

        public boolean equals(Point o) {
            return this.id == o.id;
        }
    }

    public static LinkedList<Long> getRouteBack(Point p) {
        LinkedList<Long> r = new LinkedList<>();
        while (p.prev != null) {
            r.addFirst(p.id);
            p = p.prev;
        }
        return r;
    }

    static PriorityQueue<Point> PQ = new PriorityQueue<>();

    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest,
     * where the longs are node IDs.
     */

    public static LinkedList<Long> shortestPath(GraphDB g,
                                                double stlon, double stlat,
                                                double destlon, double destlat) {
        PQ.clear();
        long startId = g.closest(stlon, stlat);
        long endId = g.closest(destlon, destlat);

        Point start = new Point(g, startId, endId, new Point(), 0);
        PQ.add(start);

        HashMap<Long, Point> inPQ = new HashMap<>();

        Point best = new Point();
        while (PQ.size() > 0) {
            best = PQ.peek();
            PQ.remove(best);

            if (best.id == endId) {
                break;
            }

            for (int i = 0; i < best.adjacentNodeIds.size(); i++) {
                long nodeToAddId = best.adjacentNodeIds.get(i);
                double distanceFromBest = g.distance(best.id, nodeToAddId);
                Point temp = new Point(g, nodeToAddId, endId, best,
                        best.distanceFromStart + distanceFromBest);

                //If its not the previous node
                if (!temp.equals(best.prev)) {
                    //If a node with the same ID already exists
                    if (inPQ.containsKey(nodeToAddId)) {
                        Point pointToCompareTo = inPQ.get(nodeToAddId);
                        if (temp.compareTo(pointToCompareTo) < 0) {
                            pointToCompareTo.distanceFromStart = temp.distanceFromStart;
                            pointToCompareTo.priority = temp.priority;
                            pointToCompareTo.prev = temp.prev;
                        }
                    } else {
                        PQ.add(temp);
                        inPQ.put(nodeToAddId, temp);
                    }
                }
            }
        }
        LinkedList t = getRouteBack(best);
        return t;
    }
}
