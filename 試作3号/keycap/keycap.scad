$fs = 0.1;
$fa = 10;

module keycap(x, y, u) {
    module round_rect_pyramid(top_w, top_h, bottom_w, bottom_h, height) {
        module round_rect(w, h, r) {
            minkowski() {
                cube([w - r * 2, h - r * 2, 0.01], center = true);
                cylinder(r = r, h = 0.001);
            }
        }

        hull() {
            translate([0, 0, height]) {
                round_rect(top_w, top_h, 1);
            }

            round_rect(bottom_w, bottom_h, 1);
        }
    }

    module stem_holder() {
        module pillar() {
            union() {
                translate([  0, -1.5 / 2, 4 + (x > 0 ? 1 : 0)]) cube([32.0,  1.5, 11]);
                translate([-32, -1.5 / 2, 4 + (x < 0 ? 1 : 0)]) cube([32.0,  1.5, 11]);
                translate([-1.5 / 2,   0, 4 + (y > 0 ? 1 : 0)]) cube([ 1.5, 32.0, 11]);
                translate([-1.5 / 2, -32, 4 + (y < 0 ? 1 : 0)]) cube([ 1.5, 32.0, 11]);
            }
        }

        module stem() {
            union() {
                translate([0, 0, 15 / 2]) cube([1.10, 4.00, 15], center = true);
                translate([0, 0, 15 / 2]) cube([4.00, 1.30, 15], center = true);
            }
        }

        difference() {
            union() {
                pillar();
                translate([0, 0, 4]) cylinder(d = 8, h = 11);
                cylinder(d = 5.5, h = 15);
            }

            stem();
        }
    }

    key_pitch = 16;
    thickness = 1.5;
    top_w = key_pitch * u - 4;
    top_h = key_pitch - 4;
    bottom_w = key_pitch * u - 0.75;
    bottom_h = key_pitch - 0.75;
    height = 5;
    dish_r = 20;

    tilt_xr = 512;
    tilt_yr = 200;
    tilt_xa = acos(key_pitch * x / tilt_xr);
    tilt_ya = acos(key_pitch * y / tilt_yr);

    dish_position_z = tilt_xr + tilt_yr
            - tilt_xr * sin(tilt_xa) - tilt_yr * sin(tilt_ya);

    module outer() {
        difference() {
            round_rect_pyramid(
                    top_w, top_h,
                    bottom_w, bottom_h,
                    height + dish_position_z + 4.5);

            translate([
                    -dish_r * cos(tilt_xa),
                    -dish_r * cos(tilt_ya),
                    height + dish_position_z + dish_r
            ]) {
                minkowski() {
                    cube([key_pitch * (u - 1) + 0.001, 0.001, 0.001], center = true);
                    sphere(dish_r);
                }
            }
        }
    }

    module inner() {
        round_rect_pyramid(
                top_w - thickness * 2, top_h - thickness * 2,
                bottom_w - thickness * 2, bottom_h - thickness * 2,
                height + dish_position_z - thickness
        );
    }

    union() {
        difference() {
            outer();
            inner();
        }

        intersection() {
            outer();
            stem_holder();
        }
    }
}

for (x = [-2 : 4]) {
    for (y = [-1 : 2]) {
        translate([x * 16, y * 16, 0]) keycap(x, y, 1);
    }
}

translate([-1.625 * 16, -2 * 16]) keycap(-1.625, -2, 1.75);
translate([-0.125 * 16, -2 * 16]) keycap(-0.125, -2, 1.25);
translate([ 1.250 * 16, -2 * 16]) keycap( 1.250, -2, 1.50);
translate([ 2.750 * 16, -2 * 16]) keycap( 2.750, -2, 1.50);
translate([ 4.000 * 16, -2 * 16]) keycap( 4.000, -2, 1.00);
