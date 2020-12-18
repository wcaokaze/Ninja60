$fs = 0.1;
$fa = 10;

module keycap(x, y) {
    module round_rect_pyramid(top_size, bottom_size, height) {
        module round_rect(w, h, r) {
            minkowski() {
                cube([w - r * 2, h - r * 2, 0.01], center = true);
                cylinder(r = r, h = 0.001);
            }
        }

        hull() {
            translate([0, 0, height]) {
                round_rect(top_size, top_size, 1);
            }

            round_rect(bottom_size, bottom_size, 1);
        }
    }

    module stem_holder() {
        module pillar() {
            union() {
                translate([0, 0, 11 / 2 + 4]) cube([16.0,  1.5, 11], center = true);
                translate([0, 0, 11 / 2 + 4]) cube([ 1.5, 16.0, 11], center = true);
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

    module outer() {
        difference() {
            round_rect_pyramid(top_size, bottom_size, height + dish_position_z + 4.5);

            translate([
                    -dish_r * cos(tilt_xa),
                    -dish_r * cos(tilt_ya),
                    height + dish_position_z + dish_r
            ]) {
                sphere(dish_r);
            }
        }
    }

    module inner() {
        round_rect_pyramid(
                top_size - thickness * 2,
                bottom_size - thickness * 2,
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

keycap(4, 2);
/*
for (x = [-2 : 4]) {
    for (y = [-1 : 2]) {
        translate([x * 16, y * 16, 0]) keycap(x, y);
    }
}
*/
