
include <shared.scad>;

module o_ring(tightening = 0) {
    difference() {
        polygon_pyramid(16, 4.3, h = 2);

        minkowski() {
            cube([1, 0.01, 0.01], center = true);
            polygon_pyramid(8, 2.97 - tightening * 2, h = 2);
        }
    }
}
