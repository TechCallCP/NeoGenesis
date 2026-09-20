package shipwrights.genesis.math;

import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class PolygonClipping {

    /**
     * Computes the 2D cross product (signed area) of triangle (a,b,c).
     * Positive if a->b->c is a left turn, negative if right turn, zero if collinear.
     */
    public static double cross(Vector2d a, Vector2d b, Vector2d c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    /**
     * Sorts a list of points in CCW order around their centroid.
     */
    public static List<Vector2d> angleSort(List<Vector2d> points) {
        Vector2d centroid = new Vector2d(0, 0);
        for (Vector2d p : points) centroid.add(p);
        centroid.div(points.size());

        List<Vector2d> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble((Vector2d p) ->
                Math.atan2(p.y - centroid.y, p.x - centroid.x)
        ));
        return sorted;
    }

    /**
     * Prunes near-collinear points from a polygon (zero-area triangle removal).
     */
    public static List<Vector2d> pruneCollinear(List<Vector2d> polygon, double epsilon) {
        if (polygon.size() < 3) return new ArrayList<>(polygon);

        List<Vector2d> result = new ArrayList<>();
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Vector2d prev = polygon.get((i + n - 1) % n);
            Vector2d curr = polygon.get(i);
            Vector2d next = polygon.get((i + 1) % n);

            double scale = prev.distanceSquared(next);
            if (Math.abs(cross(prev, curr, next)) > epsilon * scale) {
                result.add(curr);
            }
        }
        return result;
    }

    /**
     * Clips a polygon to an axis-aligned rectangle using Sutherland-Hodgman.
     */
    public static List<Vector2d> clipPolygonToRect(List<Vector2d> polygon,
                                                   double xmin, double ymin,
                                                   double xmax, double ymax) {
        List<Vector2d> output = polygon;
        output = clipEdge(output, v -> v.x >= xmin, (A, B) -> intersectEdge(A, B, xmin, true));
        output = clipEdge(output, v -> v.x <= xmax, (A, B) -> intersectEdge(A, B, xmax, true));
        output = clipEdge(output, v -> v.y >= ymin, (A, B) -> intersectEdge(A, B, ymin, false));
        output = clipEdge(output, v -> v.y <= ymax, (A, B) -> intersectEdge(A, B, ymax, false));
        return output;
    }

    private static List<Vector2d> clipEdge(List<Vector2d> polygon,
                                           Predicate<Vector2d> inside,
                                           BiFunction<Vector2d, Vector2d, Vector2d> intersectFun) {
        List<Vector2d> result = new ArrayList<>();
        if (polygon.isEmpty()) return result;

        Vector2d prev = polygon.get(polygon.size() - 1);
        boolean prevInside = inside.test(prev);

        for (Vector2d curr : polygon) {
            boolean currInside = inside.test(curr);

            if (currInside) {
                if (!prevInside) {
                    result.add(intersectFun.apply(prev, curr));
                }
                result.add(curr);
            } else if (prevInside) {
                result.add(intersectFun.apply(prev, curr));
            }

            prev = curr;
            prevInside = currInside;
        }
        return result;
    }

    private static Vector2d intersectEdge(Vector2d A, Vector2d B, double value, boolean vertical) {
        double t;
        if (vertical) {
            t = (value - A.x) / (B.x - A.x);
            return new Vector2d(value, A.y + t * (B.y - A.y));
        } else {
            t = (value - A.y) / (B.y - A.y);
            return new Vector2d(A.x + t * (B.x - A.x), value);
        }
    }

    /**
     * Computes the convex hull of a set of 2D points using Graham scan algorithm.
     * Returns vertices in counter-clockwise order.
     */
    public static List<Vector2d> convexHull(List<Vector2d> points) {
        if (points.size() < 3) {
            return new ArrayList<>(points);
        }

        Vector2d pivot = points.get(0);
        for (Vector2d p : points) {
            if (p.y < pivot.y || (p.y == pivot.y && p.x < pivot.x)) {
                pivot = p;
            }
        }

        final Vector2d finalPivot = pivot;
        List<Vector2d> sorted = new ArrayList<>(points);
        sorted.sort((a, b) -> {
            boolean aIsPivot = a.equals(finalPivot);
            boolean bIsPivot = b.equals(finalPivot);
            if (aIsPivot && bIsPivot) return 0;
            if (aIsPivot) return -1;
            if (bIsPivot) return 1;

            double angleA = Math.atan2(a.y - finalPivot.y, a.x - finalPivot.x);
            double angleB = Math.atan2(b.y - finalPivot.y, b.x - finalPivot.x);

            int angleCompare = Double.compare(angleA, angleB);
            if (angleCompare != 0) return angleCompare;

            double distA = finalPivot.distanceSquared(a);
            double distB = finalPivot.distanceSquared(b);
            return Double.compare(distA, distB);
        });

        List<Vector2d> hull = new ArrayList<>();
        for (Vector2d p : sorted) {
            while (hull.size() >= 2) {
                Vector2d top = hull.get(hull.size() - 1);
                Vector2d secondTop = hull.get(hull.size() - 2);
                if (cross(secondTop, top, p) <= 0) {
                    hull.remove(hull.size() - 1);
                } else {
                    break;
                }
            }
            hull.add(p);
        }

        return hull;
    }
}
