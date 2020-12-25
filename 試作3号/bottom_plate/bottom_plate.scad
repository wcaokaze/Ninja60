
$fs = 0.1;
$fa = 10;

module basic_key_hole() {
    union() {
        translate([-5, 0.55]) difference() {
            intersection() {
                minkowski() {
                    cube([11.9 - 3, 6.89 - 3, 0.01]);
                    cylinder(r = 3, h = 2.35);
                }

                cube([11.9, 6.89, 2.35]);
            }

            minkowski() {
                cube([11, 0.01, 0.01], center = true);
                cylinder(r = 2.39, h = 5);
            }
        }

        translate([-8.6, 3.34]) cube([3.6, 4.1, 2.15]);
        translate([ 6.9, 0.55]) cube([3.6, 4.1, 2.15]);

        translate([-5.08, 0]) cylinder(r = 1.35, h = 1.7);
        translate([ 0.00, 0]) cylinder(r = 2.50, h = 1.7);
        translate([ 5.08, 0]) cylinder(r = 1.35, h = 1.7);

        translate([0, -4.7, 0.9]) cube([6.6, 2.7, 1.8], center = true);
    }
}

basic_key_hole();
