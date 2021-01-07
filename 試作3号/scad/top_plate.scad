
include <shared.scad>;

difference() {
    cube([2 * 19.05, 2 * 19.05, 1.5]);
    translate([0.5 * 19.05, 0.5 * 19.05]) cube([14, 14, 6], center = true);
    translate([0.5 * 19.05, 1.5 * 19.05]) cube([14, 14, 6], center = true);
    translate([1.5 * 19.05, 0.5 * 19.05]) cube([14, 14, 6], center = true);
    translate([1.5 * 19.05, 1.5 * 19.05]) cube([14, 14, 6], center = true);
}
