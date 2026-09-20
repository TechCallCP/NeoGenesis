package shipwrights.genesis.math;

import net.minecraft.world.phys.AABB;

import org.joml.Matrix3d;
import org.joml.Vector3d;

public class Raycast {

    /**
     * Performs ray intersection with Minecraft's native AABB using the slab method.
     */
    public static double raycastAABB(Vector3d origin, Vector3d direction, AABB aabb) {
        return raycastAABB(
                origin,
                direction,
                new Vector3d(aabb.minX, aabb.minY, aabb.minZ),
                new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
        );
    }

    /**
     * Performs ray intersection with an axis-aligned bounding box defined by min and max vectors.
     */
    public static double raycastAABB(Vector3d origin, Vector3d direction, Vector3d min, Vector3d max) {
        double tMin = 0.0;
        double tMax = Double.POSITIVE_INFINITY;

        for (int i = 0; i < 3; i++) {
            double originA = origin.get(i);
            double directionA = direction.get(i);
            double minA = min.get(i);
            double maxA = max.get(i);

            if (Math.abs(directionA) < 1e-9) {
                if (originA < minA || originA > maxA)
                    return Double.POSITIVE_INFINITY;
            } else {
                double invD = 1.0 / directionA;
                double t1 = (minA - originA) * invD;
                double t2 = (maxA - originA) * invD;
                if (t1 > t2) {
                    double tmp = t1;
                    t1 = t2;
                    t2 = tmp;
                }
                tMin = Math.max(tMin, t1);
                tMax = Math.min(tMax, t2);
                if (tMin > tMax)
                    return Double.POSITIVE_INFINITY;
            }
        }

        return tMin;
    }

    /**
     * Performs ray intersection with an oriented bounding box by transforming the ray
     * into the OBB's local coordinate space and performing an AABB test.
     */
    public static double raycastOBB(Vector3d origin, Vector3d direction, Vector3d center, Matrix3d rotation, Vector3d localMin, Vector3d localMax) {
        Matrix3d invRot = new Matrix3d(rotation).transpose();

        Vector3d localOrigin = invRot.transform(new Vector3d(origin).sub(center));
        Vector3d localDir = invRot.transform(new Vector3d(direction));

        return raycastAABB(localOrigin, localDir, localMin, localMax);
    }
}
