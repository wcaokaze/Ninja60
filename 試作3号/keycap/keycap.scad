$fs = 0.1;
$fa = 5;

module keycap(x, y) {
    module round_rect(w, h, r) {
        minkowski() {
            cube([w - r * 2, h - r * 2, 0.01], center = true);
            cylinder(r = r, h = 0.001);
        }
    }

    module round_rect_pyramid(top_size, bottom_size, dish_position_z) {
        hull() {
            translate([0, 0, dish_position_z + 3.5]) {
                round_rect(top_size, top_size, 1);
            }

            round_rect(bottom_size, bottom_size, 1);
        }
    }

    pitch = 16;
    height = 5;
    dish_r = 20;

    tilt_xr = 512;
    tilt_yr = 200;
    tilt_xa = acos(pitch * x / tilt_xr);
    tilt_ya = acos(pitch * y / tilt_yr);

    dish_position_z = tilt_xr + tilt_yr
            - tilt_xr * sin(tilt_xa) - tilt_yr * sin(tilt_ya);

    difference() {
        round_rect_pyramid(12, 15, height + dish_position_z + 1);

        translate([
                -dish_r * cos(tilt_xa),
                -dish_r * cos(tilt_ya),
                height + dish_position_z + dish_r
        ]) {
            sphere(dish_r);
        }
    }
}

for (x = [-2 : 4]) {
    for (y = [-1 : 2]) {
        translate([x * 16, y * 16, 0]) keycap(x, y);
    }
}
