
$fs = 0.1;
$fa = 10;

module key_switch_hole() {
    translate([0, 0, 1]) union() {
        translate([-4.25 / 2, -16 / 2, 2.47]) cube([4.25, 16, 2.53]);

        hull() {
            translate([-14.75 / 2, -14.75 / 2, 3.2]) cube([14.75, 14.75, 1.8]);
            cube([13.75, 14.75, 0.01], center = true);
        }
    }
}

key_switch_hole();
