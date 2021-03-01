
include <shared.scad>;

bottom_plate_fa = 5;

module basic_key_hole() {
    union() {
        translate([-5, 0.55]) difference() {
            intersection() {
                minkowski() {
                    cube([11.9 - 3, 6.89 - 3, 0.01]);
                    cylinder(r = 3, h = 2.35, $fa = bottom_plate_fa);
                }

                cube([11.9, 6.89, 2.35]);
            }

            minkowski() {
                cube([11, 0.01, 0.01], center = true);
                cylinder(r = 2.39, h = 5, $fa = bottom_plate_fa);
            }
        }

        translate([-8.5, 3.34]) cube([3.8, 4.1, 2.15]);
        translate([ 6.8, 0.55]) cube([3.8, 4.1, 2.15]);

        translate([-5.08, 0]) cylinder(r = 1.35, h = 1.7, $fa = bottom_plate_fa);
        translate([ 0.00, 0]) cylinder(r = 2.50, h = 1.7, $fa = bottom_plate_fa);
        translate([ 5.08, 0]) cylinder(r = 1.35, h = 1.7, $fa = bottom_plate_fa);

        difference() {
            linear_extrude(height = 1.7) {
                polygon([[0, 0], [-2.06, 2.325], [-2.06, 3], [5.08, 3], [5.08, 0]]);
            }

            translate([ 3.115, -0.070]) cylinder(r = 0.615, h = 2.15, $fa = bottom_plate_fa);
            translate([-2.060,  2.325]) cylinder(r = 0.615, h = 2.15, $fa = bottom_plate_fa);
        }

        translate([0, -4.7, 0.9]) cube([6.6, 2.7, 1.8], center = true);
    }
}

difference() {
    cube([2 * 19.05, 2 * 19.05, 3.35]);
    translate([0.5 * 19.05, 0.5 * 19.05]) basic_key_hole();
    translate([0.5 * 19.05, 1.5 * 19.05]) basic_key_hole();
    translate([1.5 * 19.05, 0.5 * 19.05]) basic_key_hole();
    translate([1.5 * 19.05, 1.5 * 19.05]) basic_key_hole();
}
