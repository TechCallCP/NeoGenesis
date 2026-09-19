package shipwrights.genesis.client.shading;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.maplibre.earcut4j.Earcut;
import org.slf4j.Logger;

import shipwrights.genesis.math.AAPlane;
import shipwrights.genesis.util.NoOpLogger;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ShadowRenderer {

    private static final Logger LOGGER = NoOpLogger.INSTANCE;
    private static final double Z_FIGHTING_EPSILON = 0.005;

    public static Vector3d convertPlaneToLocal3D(Vector2dc vertex2D, AAPlane plane) {
        Vector3i normal = plane.normal();
        double position = plane.position();

        if (Math.abs(normal.x()) == 1) {
            return new Vector3d(position, vertex2D.x(), vertex2D.y());
        } else if (Math.abs(normal.y()) == 1) {
            return new Vector3d(vertex2D.x(), position, vertex2D.y());
        } else {
            return new Vector3d(vertex2D.x(), vertex2D.y(), position);
        }
    }

    public static Vector3d applyZFightingOffset(Vector3d vertex3D, AAPlane plane, double cubeHalfSize) {
        Vector3i normal = plane.normal();
        double offset = Z_FIGHTING_EPSILON * cubeHalfSize;

        return new Vector3d(
                vertex3D.x + normal.x() * offset,
                vertex3D.y + normal.y() * offset,
                vertex3D.z + normal.z() * offset
        );
    }

    public static List<Integer> triangulateShadowPolygon(List<Vector2dc> polygon) {
        if (polygon.size() < 3) {
            return List.of();
        }

        double[] coords = new double[polygon.size() * 2];
        for (int i = 0; i < polygon.size(); i++) {
            coords[i * 2] = polygon.get(i).x();
            coords[i * 2 + 1] = polygon.get(i).y();
        }

        try {
            return Earcut.earcut(coords, null, 2);
        } catch (Exception e) {
            LOGGER.warn("Failed to triangulate shadow polygon: {}", e.getMessage());
            return List.of();
        }
    }

    public static float computeEdgeWidth(FaceShadow shadow) {
        List<Vector2dc> polygon = shadow.polygon();
        if (polygon.isEmpty()) return 0.0001F;

        double cx = 0, cy = 0;
        for (Vector2dc v : polygon) {
            cx += v.x();
            cy += v.y();
        }
        cx /= polygon.size();
        cy /= polygon.size();

        double maxDist = 0;
        for (Vector2dc v : polygon) {
            double dx = v.x() - cx;
            double dy = v.y() - cy;
            maxDist = Math.max(maxDist, Math.sqrt(dx * dx + dy * dy));
        }

        return (float) Math.max(maxDist * 0.25, 0.0001);
    }

    public static void renderShadow(FaceShadow shadow, Matrix4f matrix, VertexConsumer buffer, double cubeHalfSize) {
        List<Vector2dc> polygon = shadow.polygon();
        AAPlane plane = shadow.plane();

        LOGGER.info("renderShadow: Processing shadow with {} vertices", polygon.size());

        List<Integer> indices = triangulateShadowPolygon(polygon);
        if (indices.isEmpty() || indices.size() % 3 != 0) {
            LOGGER.warn("Triangulation failed or produced invalid result. Indices: {}", indices.size());
            return;
        }

        LOGGER.info("Triangulation successful: {} triangles ({} indices)", indices.size() / 3, indices.size());

        double seamExt = Z_FIGHTING_EPSILON * cubeHalfSize;
        double eps = cubeHalfSize * 1e-4;
        Vector3d[] vertices3D = new Vector3d[polygon.size()];
        for (int i = 0; i < polygon.size(); i++) {
            Vector2dc v2d = polygon.get(i);
            double vx = v2d.x();
            double vy = v2d.y();
            if (Math.abs(vx - cubeHalfSize) < eps) vx += seamExt;
            else if (Math.abs(vx + cubeHalfSize) < eps) vx -= seamExt;
            if (Math.abs(vy - cubeHalfSize) < eps) vy += seamExt;
            else if (Math.abs(vy + cubeHalfSize) < eps) vy -= seamExt;
            Vector3d v3d = convertPlaneToLocal3D(new Vector2d(vx, vy), plane);
            vertices3D[i] = applyZFightingOffset(v3d, plane, cubeHalfSize);
            LOGGER.info("Vertex {}: 2D({}, {}) -> 3D({}, {}, {})",
                    i, polygon.get(i).x(), polygon.get(i).y(),
                    vertices3D[i].x, vertices3D[i].y, vertices3D[i].z);
        }

        int r = 0, g = 0, b = 0, a = 176;

        LOGGER.info("Rendering {} triangles with color rgba({}, {}, {}, {})", indices.size() / 3, r, g, b, a);

        boolean reverseWinding = plane.normal().x() == -1 || plane.normal().y() == 1 || plane.normal().z() == -1;

        int triangleCount = 0;
        for (int i = 0; i < indices.size(); i += 3) {
            int i0 = indices.get(i);
            int i1 = indices.get(i + 1);
            int i2 = indices.get(i + 2);

            if (i0 >= vertices3D.length || i1 >= vertices3D.length || i2 >= vertices3D.length) {
                LOGGER.warn("Invalid triangle indices: {}, {}, {} (max: {})", i0, i1, i2, vertices3D.length - 1);
                continue;
            }

            Vector3d v0 = vertices3D[i0];
            Vector3d v1 = vertices3D[i1];
            Vector3d v2 = vertices3D[i2];

            if (reverseWinding) {
                buffer.addVertex(matrix, (float) v0.x, (float) v0.y, (float) v0.z).setColor(r, g, b, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a);
                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a);
            } else {
                buffer.addVertex(matrix, (float) v0.x, (float) v0.y, (float) v0.z).setColor(r, g, b, a);
                buffer.addVertex(matrix, (float) v1.x, (float) v1.y, (float) v1.z).setColor(r, g, b, a);
                buffer.addVertex(matrix, (float) v2.x, (float) v2.y, (float) v2.z).setColor(r, g, b, a);
            }
            triangleCount++;
        }

        LOGGER.info("Successfully rendered {} triangles", triangleCount);
    }
}
