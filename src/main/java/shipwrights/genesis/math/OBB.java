package shipwrights.genesis.math;

import net.minecraft.world.phys.AABB;

import org.joml.Matrix4dc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public record OBB(AABB localAabb, Quaterniondc orientation, Vector3dc center) {

    /**
     * Creates a cube-shaped OBB with the specified side length, orientation, and center.
     *
     * @param sideLength the length of each side of the cube
     * @param orientation the orientation of the cube as a quaternion
     * @param center the center position of the cube in world space
     * @return a new OBB representing the cube
     */
    public static OBB createCube(double sideLength, Quaterniondc orientation, Vector3dc center) {
        double halfSize = sideLength / 2.0;
        AABB localAabb = new AABB(
                -halfSize, -halfSize, -halfSize,
                halfSize, halfSize, halfSize
        );
        return new OBB(localAabb, orientation, center);
    }

    /**
     * Creates an OBB from an axis-aligned bounding box.
     * The resulting OBB will have identity rotation (no rotation) and will be positioned
     * at the center of the input AABB.
     *
     * @param aabb the axis-aligned bounding box to convert
     * @return a new OBB representing the same volume as the AABB
     */
    public static OBB fromAABB(AABB aabb) {
        double centerX = (aabb.minX + aabb.maxX) / 2.0;
        double centerY = (aabb.minY + aabb.maxY) / 2.0;
        double centerZ = (aabb.minZ + aabb.maxZ) / 2.0;
        Vector3d center = new Vector3d(centerX, centerY, centerZ);

        double halfX = (aabb.maxX - aabb.minX) * 0.5;
        double halfY = (aabb.maxY - aabb.minY) * 0.5;
        double halfZ = (aabb.maxZ - aabb.minZ) * 0.5;

        AABB localAabb = new AABB(
                -halfX, -halfY, -halfZ,
                halfX, halfY, halfZ
        );

        Quaterniondc identityRotation = new Quaterniond();

        return new OBB(localAabb, identityRotation, center);
    }

    /**
     * Creates an OBB from a shipyard-space AABB and a ship-to-world transformation matrix.
     *
     * @param shipyardAABB the axis-aligned bounding box in shipyard space
     * @param shipToWorldTransform the transformation matrix from shipyard space to world space
     * @return a new OBB representing the transformed bounding box in world space
     */
    public static OBB fromShip(AABB shipyardAABB, Matrix4dc shipToWorldTransform) {
        double localCenterX = (shipyardAABB.minX + shipyardAABB.maxX) / 2.0;
        double localCenterY = (shipyardAABB.minY + shipyardAABB.maxY) / 2.0;
        double localCenterZ = (shipyardAABB.minZ + shipyardAABB.maxZ) / 2.0;
        Vector3d localCenter = new Vector3d(localCenterX, localCenterY, localCenterZ);

        Vector3d worldCenter = shipToWorldTransform.transformPosition(localCenter);

        Quaterniond orientation = new Quaterniond();
        shipToWorldTransform.getNormalizedRotation(orientation);

        Vector3d scale = shipToWorldTransform.getScale(new Vector3d());

        double halfX = ((shipyardAABB.maxX - shipyardAABB.minX) * 0.5) * scale.x;
        double halfY = ((shipyardAABB.maxY - shipyardAABB.minY) * 0.5) * scale.y;
        double halfZ = ((shipyardAABB.maxZ - shipyardAABB.minZ) * 0.5) * scale.z;

        AABB localAabb = new AABB(
                -halfX, -halfY, -halfZ,
                halfX, halfY, halfZ
        );

        return new OBB(localAabb, orientation.normalize(), worldCenter);
    }

    /** Get 8 corners of an OBB in world space */
    public Vector3dc[] getCorners() {
        Vector3dc[] corners = new Vector3dc[8];
        AABB aabb = localAabb;
        int i = 0;
        for (int x = 0; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = 0; z <= 1; z++) {
                    double px = (x == 0) ? aabb.minX : aabb.maxX;
                    double py = (y == 0) ? aabb.minY : aabb.maxY;
                    double pz = (z == 0) ? aabb.minZ : aabb.maxZ;
                    Vector3d local = new Vector3d(px, py, pz);
                    Vector3d world = new Vector3d(local).rotate(orientation).add(center);
                    corners[i++] = world;
                }
            }
        }
        return corners;
    }

    /**
     * Calculates the minimum Euclidean distance between this OBB and another OBB.
     * Returns 0 if the OBBs are overlapping.
     */
    public double distanceTo(OBB other) {
        Vector3d closestOnThis = closestPointTo(other.center);
        Vector3d closestOnOther = other.closestPointTo(closestOnThis);
        closestOnThis = closestPointTo(closestOnOther);

        double distance = closestOnThis.distance(closestOnOther);

        if (overlapsWith(other)) {
            return 0.0;
        }

        return distance;
    }

    /**
     * Finds the closest point on this OBB to the given point in world space.
     */
    public Vector3d closestPointTo(Vector3dc point) {
        Vector3d localPoint = new Vector3d(point).sub(this.center);

        Quaterniondc invRotation = new Quaterniond(this.orientation).conjugate();
        localPoint.rotate(invRotation);

        double halfX = (this.localAabb.maxX - this.localAabb.minX) * 0.5;
        double halfY = (this.localAabb.maxY - this.localAabb.minY) * 0.5;
        double halfZ = (this.localAabb.maxZ - this.localAabb.minZ) * 0.5;

        localPoint.x = Math.max(-halfX, Math.min(halfX, localPoint.x));
        localPoint.y = Math.max(-halfY, Math.min(halfY, localPoint.y));
        localPoint.z = Math.max(-halfZ, Math.min(halfZ, localPoint.z));

        localPoint.rotate(this.orientation);
        localPoint.add(this.center);

        return localPoint;
    }

    /**
     * Checks if this OBB overlaps with another OBB using the Separating Axis Theorem.
     */
    public boolean overlapsWith(OBB other) {
        Vector3d[] axesA = new Vector3d[3];
        Vector3d[] axesB = new Vector3d[3];

        getAxes(this.orientation, axesA);
        getAxes(other.orientation, axesB);

        Vector3d centerDelta = new Vector3d(other.center).sub(this.center);

        for (int i = 0; i < 3; i++) {
            if (axisDistance(axesA[i], centerDelta, this.localAabb, other.localAabb, axesA, axesB) > 0) {
                return false;
            }
            if (axisDistance(axesB[i], centerDelta, this.localAabb, other.localAabb, axesA, axesB) > 0) {
                return false;
            }
        }

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Vector3d axis = new Vector3d(axesA[i]).cross(axesB[j]);
                if (axis.lengthSquared() < 1e-10) continue;
                axis.normalize();

                if (axisDistance(axis, centerDelta, this.localAabb, other.localAabb, axesA, axesB) > 0) {
                    return false;
                }
            }
        }

        return true;
    }

    public double boundingSphereRadius() {
        double hx = (localAabb.maxX - localAabb.minX) * 0.5;
        double hy = (localAabb.maxY - localAabb.minY) * 0.5;
        double hz = (localAabb.maxZ - localAabb.minZ) * 0.5;
        return Math.sqrt(hx * hx + hy * hy + hz * hz);
    }

    static void getAxes(Quaterniondc q, Vector3d[] axes) {
        axes[0] = new Vector3d(1, 0, 0).rotate(q);
        axes[1] = new Vector3d(0, 1, 0).rotate(q);
        axes[2] = new Vector3d(0, 0, 1).rotate(q);
    }

    static double axisDistance(
            Vector3dc axis,
            Vector3dc centerDelta,
            AABB aabbA,
            AABB aabbB,
            Vector3d[] axesA,
            Vector3d[] axesB
    ) {
        double centerProj = Math.abs(centerDelta.dot(axis));

        double rA = projectRadius(aabbA, axis, axesA);
        double rB = projectRadius(aabbB, axis, axesB);

        return centerProj - (rA + rB);
    }

    static double projectRadius(AABB aabb, Vector3dc axis, Vector3d[] boxAxes) {
        double halfX = (aabb.maxX - aabb.minX) * 0.5;
        double halfY = (aabb.maxY - aabb.minY) * 0.5;
        double halfZ = (aabb.maxZ - aabb.minZ) * 0.5;

        return Math.abs(axis.dot(boxAxes[0])) * halfX +
                Math.abs(axis.dot(boxAxes[1])) * halfY +
                Math.abs(axis.dot(boxAxes[2])) * halfZ;
    }
}
