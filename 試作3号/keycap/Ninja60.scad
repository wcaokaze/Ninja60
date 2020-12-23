
include <keycap.scad>;

$fs = 0.1;
$fa = 10;

key_distance = 16.5;
upper_layer_z_offset = 28.5;

module basic_keys() {
    module right_12keys() {
            translate([0.5 * key_distance, 0]) keycap(1,  1);
            translate([1.5 * key_distance, 0]) keycap(1,  2);
            translate([2.5 * key_distance, 0]) keycap(2,  1);
            translate([0.5 * key_distance, -key_distance]) keycap(1,  0);
            translate([1.5 * key_distance, -key_distance]) keycap(1, -1);
            translate([2.5 * key_distance, -key_distance]) keycap(2,  0);

            rotate([180, 0, -90]) {
                translate([0, -0.5 * key_distance, -upper_layer_z_offset]) keycap(3, 2);
                translate([0, -1.5 * key_distance, -upper_layer_z_offset]) keycap(2, 2);
                translate([0, -2.5 * key_distance, -upper_layer_z_offset]) keycap(3, 1);
                translate([key_distance, -0.5 * key_distance, -upper_layer_z_offset]) keycap(3, -1);
                translate([key_distance, -1.5 * key_distance, -upper_layer_z_offset]) keycap(2, -1);
                translate([key_distance, -2.5 * key_distance, -upper_layer_z_offset]) keycap(3,  0);
            }
    }

    translate([key_distance, 0 * key_distance]) keycap(0, 0);
    translate([key_distance, 1 * key_distance]) keycap(0, 1);

    translate([0, 0.5 * key_distance]) keycap(x = 4, y =  0, h = 2.00, is_cylindrical = true);

    rotate([0, 180, 0]) {
        translate([-key_distance, 0 * key_distance, -upper_layer_z_offset]) keycap(0,  2);
        translate([-key_distance, 1 * key_distance, -upper_layer_z_offset]) keycap(0, -1);

        translate([0, key_distance, -upper_layer_z_offset]) {
            rotate([0, 0, 180]) keycap(x = -2, y = 0, is_home_position = true);
        }

        translate([0, 0, -upper_layer_z_offset]) {
            keycap(x = 2, y = 0, is_home_position = true);
        }
    }

    translate([4.5 * key_distance, key_distance]) {
        right_12keys();
        mirror([1, 0]) right_12keys();
    }
}

module modifier_keys() {
    translate([0.375 * key_distance, 0]) keycap(x = -1.625, y = -2, w = 1.75);
    translate([1.875 * key_distance, 0]) keycap(x = -2.000, y =  1, w = 1.25, is_cylindrical = true);
    translate([3.250 * key_distance, 0]) keycap(x = -1.250, y =  1, w = 1.50, is_cylindrical = true);
    translate([4.750 * key_distance, 0]) keycap(x =  1.250, y =  1, w = 1.50, is_cylindrical = true);

    translate([6 * key_distance,  0]) keycap(x = 4, y = 0);
    translate([7 * key_distance,  0]) keycap(x = 4, y = 2);

    rotate([180, 0, 0]) {
        translate([1.00 * key_distance, 0, -upper_layer_z_offset]) keycap(x =  2.5, y =  1, w = 1.0, is_cylindrical = true);
        translate([2.00 * key_distance, 0, -upper_layer_z_offset]) keycap(x = -2.0, y = 1, w = 1.0, is_cylindrical = true);
        translate([3.25 * key_distance, 0, -upper_layer_z_offset]) keycap(x = -1.0, y = 1, w = 1.5, is_cylindrical = true);
        translate([4.75 * key_distance, 0, -upper_layer_z_offset]) keycap(x =  1.0, y = 1, w = 1.5, is_cylindrical = true);

        translate([6.00 * key_distance, 0, -upper_layer_z_offset]) {
            rotate([0, 0, 180]) keycap(x =  1.0, y = 1, w = 1.0, is_cylindrical = true);
        }
    }
}

translate([0.5 * key_distance, 1.5 * key_distance]) basic_keys();
translate([0.5 * key_distance, 3.5 * key_distance]) basic_keys();
translate([0.5 * key_distance, 0.5 * key_distance]) modifier_keys();
