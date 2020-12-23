
module keycap(x, y, w = 1, h = 1, is_cylindrical = false, is_home_position = false) {
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
                translate([  0, -1.5 / 2, 3.5 + (x > 0 ? 1 : 0)]) cube([32.0,  1.5, 11]);
                translate([-32, -1.5 / 2, 3.5 + (x < 0 ? 1 : 0)]) cube([32.0,  1.5, 11]);
                translate([-1.5 / 2,   0, 3.5 + (y > 0 ? 1 : 0)]) cube([ 1.0, 32.0, 11]);
                translate([-1.5 / 2, -32, 3.5 + (y < 0 ? 1 : 0)]) cube([ 1.0, 32.0, 11]);
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
                translate([0, 0, 3.5]) cylinder(d = 8, h = 11);
                cylinder(d = 5.5, h = 15);
            }

            stem();
        }
    }

    key_pitch = 16;
    thickness = 1.5;
    top_w = key_pitch * w - 4;
    top_h = key_pitch * h - 4;
    bottom_w = key_pitch * w - 0.75;
    bottom_h = key_pitch * h - 0.75;
    height = 6;
    dish_r = 20;

    tilt_xr = 250;
    tilt_yr = 180;
    tilt_xa = acos(key_pitch * x / tilt_xr);
    tilt_ya = acos(key_pitch * y / tilt_yr);

    dish_position_z = tilt_xr + tilt_yr
            - tilt_xr * sin(tilt_xa) - tilt_yr * sin(tilt_ya);

    module home_position_mark() {
        translate([
                0,
                -top_h / 2,
                height + dish_position_z + 0.85
        ]) {
            rotate([0, tilt_xa, 0]) {
                minkowski() {
                    cube([1, 0.001, 2.75], center = true);
                    sphere(0.3);
                }
            }
        }
    }

    module outer() {
        difference() {
            translate([0, 0, 3]) {
                round_rect_pyramid(
                        top_w, top_h,
                        bottom_w, bottom_h,
                        height + dish_position_z + 3);
            }

            if (is_cylindrical) {
                translate([
                        -dish_r * cos(tilt_xa),
                        0,
                        height + dish_position_z + dish_r
                ]) {
                    minkowski() {
                        cube(center = true, [
                                key_pitch * (w - 1) + 0.001,
                                key_pitch * (h - 1) + 0.001,
                                0.001
                        ]);

                        rotate([-tilt_ya, 0, 0]) {
                            cylinder(r = dish_r, h = dish_r, center = true);
                        }
                    }
                }
            } else {
                translate([
                        -dish_r * cos(tilt_xa),
                        -dish_r * cos(tilt_ya),
                        height + dish_position_z + dish_r
                ]) {
                    minkowski() {
                        cube(center = true, [
                                key_pitch * (w - 1) + 0.001,
                                key_pitch * (h - 1) + 0.001,
                                0.001
                        ]);

                        sphere(dish_r);
                    }
                }
            }
        }
    }

    module inner() {
        translate([0, 0, 3]) {
            round_rect_pyramid(
                    top_w - thickness * 2, top_h - thickness * 2,
                    bottom_w - thickness * 2, bottom_h - thickness * 2,
                    height + dish_position_z - thickness - 3
            );
        }
    }

    union() {
        difference() {
            union() {
                outer();

                if (is_home_position) {
                    home_position_mark();
                }
            }

            inner();
        }

        intersection() {
            union() {
                outer();
                translate([0, 0, 1.5]) cube([bottom_w, bottom_h, 3], center = true);
            }

            stem_holder();
        }
    }
}
