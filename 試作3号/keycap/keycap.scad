
$fs = 0.1;
$fa = 10;

key_pitch = 16;
thickness = 1.5;

height = 6.75;
dish_r = 20;

tilt_xr = 250;
tilt_yr = 120;

module case_form() {
    case_form_xr = tilt_xr + height;
    case_form_yr = tilt_yr + height;

    translate([0, 0, case_form_xr + case_form_yr]) minkowski() {
        rotate([90, 0, 90]) cylinder(r = case_form_yr, h = 0.001);
        rotate([90, 0,  0]) cylinder(r = case_form_xr, h = 0.001);
    }
}

module polygon_pyramid(n, r, h) {
    linear_extrude(h) polygon([
        for (a = [180.0 / n : 360.0 / n : 360.0 + 180.0 / n])
            [r * sin(a), r * cos(a)]
    ]);
}

module keycap(x, y, w = 1, h = 1, is_cylindrical = false, is_home_position = false) {
    top_w = key_pitch * w - 4;
    top_h = key_pitch * h - 4;
    bottom_w = key_pitch * w - 0.75;
    bottom_h = key_pitch * h - 0.75;

    tilt_xa = acos(key_pitch * x / tilt_xr);
    tilt_ya = acos(key_pitch * y / tilt_yr);

    dish_position_z = tilt_xr + tilt_yr
            - tilt_xr * sin(tilt_xa) - tilt_yr * sin(tilt_ya);

    module dish(height) {
        if (is_cylindrical) {
            translate([
                    -dish_r * cos(tilt_xa),
                    0,
                    height + dish_position_z + dish_r - 3
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
                    height + dish_position_z + dish_r - 3
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

    module outer() {
        top_z = height + dish_position_z + 3;

        bottom_z = tilt_xr * (1 - sin(tilt_xa))
                 + tilt_yr * (1 - sin(tilt_ya));

        module round_rect_pyramid() {
            module round_rect(w, h, r) {
                minkowski() {
                    cube([w - r * 2, h - r * 2, 0.01], center = true);
                    cylinder(r = r, h = 0.001);
                }
            }

            hull() {
                translate([0, 0, top_z])    round_rect(top_w,    top_h,    1);
                translate([0, 0, bottom_z]) round_rect(bottom_w, bottom_h, 1);
            }
        }

        intersection() {
            difference() {
                round_rect_pyramid();
                dish(height);
            }

            translate([key_pitch * -x, key_pitch * -y]) case_form();
        }
    }

    module inner() {
        top_z = height + dish_position_z + 3;

        bottom_z = tilt_xr * (1 - sin(tilt_xa))
                 + tilt_yr * (1 - sin(tilt_ya));

        module rect_pyramid(top_w, top_h, bottom_w, bottom_h) {
            hull() {
                translate([0, 0, top_z])    cube([top_w,    top_h,    0.01], center = true);
                translate([0, 0, bottom_z]) cube([bottom_w, bottom_h, 0.01], center = true);
            }
        }

        difference() {
            rect_pyramid(
                    top_w    - thickness * 2, top_h    - thickness * 2,
                    bottom_w - thickness * 2, bottom_h - thickness * 2
            );

            dish(height - thickness);
        }
    }

    module pillar() {
        union() {
            translate([     0, -1.5 / 2, 1.5 + (x > 0 ? 1 : 0)]) cube([32.0,  1.5, 32]);
            translate([   -32, -1.5 / 2, 1.5 + (x < 0 ? 1 : 0)]) cube([32.0,  1.5, 32]);
            translate([-1 / 2,        0, 1.5 + (y > 0 ? 1 : 0)]) cube([ 1.0, 32.0, 32]);
            translate([-1 / 2,      -32, 1.5 + (y < 0 ? 1 : 0)]) cube([ 1.0, 32.0, 32]);
        }
    }

    module stem_holder_foundation() {
        polygon_pyramid(16, 2.81, h = 32);
    }

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

    difference() {
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
                inner();

                union() {
                    pillar();
                    stem_holder_foundation();
                }
            }
        }

        // dig out for stem_holder
        polygon_pyramid(16, 4.3, h = 2.5);
    }
}

module stem_holder() {
    module stem() {
        union() {
            translate([0, 0, 15 / 2]) cube([1.05, 4.00, 32], center = true);
            translate([0, 0, 15 / 2]) cube([4.00, 1.25, 32], center = true);
        }
    }

    difference() {
        union() {
            translate([0, 0, 3.5]) polygon_pyramid(16, 4.3, h = 2);
            polygon_pyramid(16, 2.81, h = 3.5);
        }

        stem();
    }
}

module layout(x, y, rotation_x = 0, rotation_y = 0, rotation_z = 0, is_upper_layer = false) {
    key_distance = 16.5;
    upper_layer_z_offset = 28.5;

    translate([
            (x + 0.5) * key_distance,
            (y + 0.5) * key_distance,
            is_upper_layer ? upper_layer_z_offset : 0
    ]) {
        rotate([
                rotation_x + (is_upper_layer ? 180 : 0),
                rotation_y,
                rotation_z
        ]) {
            children();
        }
    }
}

for (y = [0 : 1]) {
    for (x = [0 : 4]) {
        translate([16 * x, 16 * y]) {
            keycap(x, y);
            translate([0, 0, -3]) %stem_holder();
        }
    }
}
/*
layout(position_x, position_y, rotation_x, rotation_y, rotation_z, is_upper_layer) {
    keycap(x, y, w, h, is_cylindrical, is_home_position);
}
*/
