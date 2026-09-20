package shipwrights.genesis.math;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;

public class Occlusion {

    /**
     * Filters a list of oriented bounding boxes (OBBs) to those that could
     * potentially occlude a reference-point light source (e.g., a sun)
     * from reaching a target OBB.
     */
    public static List<OBB> getPotentiallyOccluding(OBB self, List<OBB> others, Vector3dc referencePoint) {
        return others.stream().filter(it -> couldOcclude(it, self, referencePoint)).toList();
    }

    /**
     * Determines whether a candidate OBB could potentially occlude light
     * from a point light source to a target OBB.
     */
    public static boolean couldOcclude(
            OBB candidate,
            OBB target,
            Vector3dc referencePoint
    ) {
        Vector3d sunToTarget = new Vector3d(target.center()).sub(referencePoint);
        double targetDist = sunToTarget.length();
        if (targetDist == 0.0) return false;
        sunToTarget.div(targetDist);

        Vector3d sunToCandidate = new Vector3d(candidate.center()).sub(referencePoint);
        double candidateDist = sunToCandidate.length();
        if (candidateDist == 0.0) return false;

        // Step 1: half-space check
        double proj = sunToCandidate.dot(sunToTarget);
        if (proj <= 0.0 || proj >= targetDist) return false;

        // Step 2: cone test
        double targetRadius = target.boundingSphereRadius();
        double sinTheta = Math.min(1.0, targetRadius / targetDist);
        double cosTheta = Math.sqrt(1.0 - sinTheta * sinTheta);

        double candidateRadius = candidate.boundingSphereRadius();

        double dot = proj / candidateDist;

        return dot >= cosTheta - (candidateRadius / candidateDist) * sinTheta;
    }
}
