$fs = 0.1;
$fa = 10;

module keycap(x, y) {
    module round_rect(w, h, r) {
        minkowski() {
            cube([w - r * 2, h - r * 2, 0.01], center = true);
            cylinder(r = r, h = 0.001);
        }
    }

    module round_rect_pyramid(top_size, bottom_size, height) {
        hull() {
            translate([0, 0, height]) {
                round_rect(top_size, top_size, 1);
            }

            round_rect(bottom_size, bottom_size, 1);
        }
    }

    key_pitch = 16;
    thickness = 1.5;
    top_size = 12;
    bottom_size = 15.25;
    height = 5;
    dish_r = 20;

    tilt_xr = 512;
    tilt_yr = 200;
    tilt_xa = acos(key_pitch * x / tilt_xr);
    tilt_ya = acos(key_pitch * y / tilt_yr);

    dish_position_z = tilt_xr + tilt_yr
            - tilt_xr * sin(tilt_xa) - tilt_yr * sin(tilt_ya);

    difference() {
        round_rect_pyramid(top_size, bottom_size, height + dish_position_z + 4.5);

        translate([
                -dish_r * cos(tilt_xa),
                -dish_r * cos(tilt_ya),
                height + dish_position_z + dish_r
        ]) {
            sphere(dish_r);
        }

        round_rect_pyramid(
                top_size - thickness * 2,
                bottom_size - thickness * 2,
                height + dish_position_z - thickness
        );
    }
}

for (x = [-2 : 4]) {
    for (y = [-1 : 2]) {
        translate([x * 16, y * 16, 0]) keycap(x, y);
    }
}
