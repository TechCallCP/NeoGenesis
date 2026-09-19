package shipwrights.genesis.content.radar;

import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import net.minecraft.core.Registry;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import shipwrights.genesis.NeoGenesisMod;
import shipwrights.genesis.space.Celestial;

import java.util.ArrayList;
import java.util.List;

public class RadarDisplay {

    public final int resolution;
    public final double[][] data;
    private static final double FOV = 90.0;
    private final RadarScanner scanner = new RadarScanner(64, Math.PI / 4.0);

    public RadarDisplay(int resolution) {
        this.resolution = resolution;
        this.data = new double[resolution][resolution];
    }

    public void scan(Level level, Vector3dc camera, Vector3dc direction, Vector3dc up, List<Long> excludedShips) {
        clear();

        Vector3d directionNormalized = new Vector3d(direction).normalize();
        Vector3d upInput = new Vector3d(up).normalize();

        Vector3d right = new Vector3d(directionNormalized).cross(upInput).normalize();
        Vector3d upNormalized = new Vector3d(right).cross(directionNormalized).normalize();

        scanner.update(camera, directionNormalized, upNormalized, right);

        scanShips(level, camera, excludedShips);

        if (NeoGenesisMod.isSpaceDimension(level)) {
            scanPlanets(level, camera);
        }
    }

    private void scanShips(Level level, Vector3dc camera, List<Long> excludedShips) {
        double range = 10000.0;
        AABB searchArea = new AABB(
                camera.x() - range, camera.y() - range, camera.z() - range,
                camera.x() + range, camera.y() + range, camera.z() + range
        );

        List<AbstractContraptionEntity> contraptions = level.getEntitiesOfClass(
                AbstractContraptionEntity.class,
                searchArea,
                entity -> !excludedShips.contains((long) entity.getId())
        );

        for (AbstractContraptionEntity contraption : contraptions) {
            scanBox(contraption.getBoundingBox());
        }
    }

    private void scanPlanets(Level level, Vector3dc camera) {
        Registry<Celestial> registry = NeoGenesisMod.getCelestialRegistry(level);
        registry.forEach(body -> {
            double extent = body.getActualSize() / 2.0;
            Vector3dc pos = body.getPosition(NeoGenesisMod.getTicks(level), registry);
            AABB box = new AABB(
                    pos.x() - extent, pos.y() - extent, pos.z() - extent,
                    pos.x() + extent, pos.y() + extent, pos.z() + extent
            );
            scanBox(box);
        });
    }

    private void scanAsteroidBelt(Level level, Vector3dc camera) {
        double majorRadius = 25_000.0;
        double minorRadius = 470.0;
        int segments = 64;

        double arcLength = (2.0 * Math.PI * majorRadius) / segments;
        double boxRadialExtent = minorRadius + arcLength * 0.6;

        for (int i = 0; i < segments; i++) {
            double angle = (2.0 * Math.PI * i) / segments;

            double cx = Math.cos(angle) * majorRadius;
            double cz = Math.sin(angle) * majorRadius;

            AABB box = new AABB(
                    cx - boxRadialExtent, -64, cz - boxRadialExtent,
                    cx + boxRadialExtent, 256, cz + boxRadialExtent
            );

            scanBox(box);
        }
    }

    private void scanBox(AABB box) {
        Vector3dc[] corners = {
                new Vector3d(box.minX, box.minY, box.minZ),
                new Vector3d(box.minX, box.minY, box.maxZ),
                new Vector3d(box.minX, box.maxY, box.minZ),
                new Vector3d(box.minX, box.maxY, box.maxZ),
                new Vector3d(box.maxX, box.minY, box.minZ),
                new Vector3d(box.maxX, box.minY, box.maxZ),
                new Vector3d(box.maxX, box.maxY, box.minZ),
                new Vector3d(box.maxX, box.maxY, box.maxZ)
        };

        List<RadarScanner.RadarScanResult> results = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            scanner.scanPoint(corners[i]).ifPresent(results::add);
        }
        if (results.isEmpty()) return;

        int maxX = -1;
        int minX = 9999;
        int maxY = -1;
        int minY = 9999;
        double depthSqr = 0;

        for (var result : results) {
            maxX = Math.max(maxX, result.x());
            minX = Math.min(minX, result.x());
            maxY = Math.max(maxY, result.y());
            minY = Math.min(minY, result.y());
            depthSqr = Math.max(depthSqr, result.distSquared());
        }

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                writeDepth(x, y, Math.sqrt(depthSqr));
            }
        }
    }

    public void writeDepth(int x, int y, double depth) {
        if (x >= 0 && x < resolution && y >= 0 && y < resolution) {
            double existing = data[x][y];
            if (existing == 0 || depth < existing) {
                data[x][y] = depth;
            }
        }
    }

    private void clear() {
        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {
                data[x][y] = 0;
            }
        }
    }
}
