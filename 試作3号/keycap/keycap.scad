
$fs = 0.1;
case_fa = 5;
visible_fa = 5;
invisible_fa = 8;

key_pitch = 16;
thickness = 1.5;

height = 6.93;
dish_r = 20;

tilt_xr = 250;
tilt_yr = 120;

case_curve_r = 850;
case_south_r = 380;
case_north_r = 255;

case_south_x = 16 * 1;
case_north_x = 16 * 0;

module polygon_pyramid(n, r, h) {
    linear_extrude(h) polygon([
        for (a = [180.0 / n : 360.0 / n : 360.0 + 180.0 / n])
            [r * sin(a), r * cos(a)]
    ]);
}

module case_curve() {
    start_angle = -90;
    end_angle = -85;
    step = 0.1;

    translate([0, key_pitch * -2.5, case_curve_r]) union() {
        for (i = [0 : step : 1]) {
            a_angle = start_angle + (end_angle - start_angle) * i;
            a_y = case_curve_r * cos(a_angle);
            a_z = case_curve_r * sin(a_angle);

            b_angle = start_angle + (end_angle - start_angle) * (i + step);
            b_y = case_curve_r * cos(b_angle);
            b_z = case_curve_r * sin(b_angle);

            a_r = case_south_r + (case_north_r - case_south_r) * i;
            b_r = case_south_r + (case_north_r - case_south_r) * (i + step);

            a_x = case_south_x + (case_north_x - case_south_x) * i;
            b_x = case_south_x + (case_north_x - case_south_x) * (i + step);

            hull() {
                translate([a_x, a_y, a_z + a_r]) rotate([90, 0]) cylinder(r = a_r, h = 0.01, $fa = case_fa);
                translate([b_x, b_y, b_z + b_r]) rotate([90, 0]) cylinder(r = b_r, h = 0.01, $fa = case_fa);
            }
        }
    }
}

/*
 * キーキャップ。子を渡すとintersectionによって外形が調整されます
 */
module keycap(x, y, w = 1, h = 1, is_cylindrical = false, is_home_position = false) {
    top_w = key_pitch * w - 4;
    top_h = key_pitch * h - 4;
    bottom_w = key_pitch * w - 0.75;
    bottom_h = key_pitch * h - 0.75;

    tilt_xa = acos(key_pitch * x / tilt_xr);
    tilt_ya = acos(key_pitch * y / tilt_yr);

    dish_position_z = tilt_xr * (1 - sin(tilt_xa)) + tilt_yr * (1 - sin(tilt_ya));
    top_z = height + dish_position_z + 3;

    module dish(height, fa) {
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
                        cylinder(r = dish_r, h = dish_r, center = true, $fa = fa);
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
                    rotate([tilt_ya - 90, tilt_xa - 90]) {
                        cube(center = true, [
                                key_pitch * (w - 1) + 0.001,
                                key_pitch * (h - 1) + 0.001,
                                0.001
                        ]);
                    }

                    sphere(dish_r, $fa = fa);
                }
            }
        }
    }

    module outer() {
        module round_rect_pyramid() {
            module round_rect(w, h, r) {
                minkowski() {
                    cube([w - r * 2, h - r * 2, 0.01], center = true);
                    cylinder(r = r, h = 0.001, $fa = visible_fa);
                }
            }

            hull() {
                translate([0, 0, top_z]) round_rect(top_w, top_h, 1);
                round_rect(bottom_w, bottom_h, 1);
            }
        }

        intersection() {
            difference() {
                round_rect_pyramid();
                dish(height, fa = visible_fa);
            }

            children();
        }
    }

    module inner() {
        module rect_pyramid(top_w, top_h, bottom_w, bottom_h) {
            hull() {
                translate([0, 0, top_z]) cube([top_w, top_h, 0.01], center = true);
                cube([bottom_w, bottom_h, 0.01], center = true);
            }
        }

        difference() {
            rect_pyramid(
                    top_w    - thickness * 2, top_h    - thickness * 2,
                    bottom_w - thickness * 2, bottom_h - thickness * 2
            );

            dish(height - thickness, fa = invisible_fa);
        }
    }

    module pillar() {
        intersection() {
            union() {
                translate([-16, - 1.5, 2.5]) cube([32,  3, 24]);
                translate([- 1, -16.0, 2.5]) cube([ 2, 32, 24]);

                translate([0, 0, 2.5]) {
                    polygon_pyramid(16, 4.3, h = (x >= -2 && x <= 2) ? 24 : 2);
                }

                translate([-1.05 / 2, -4.00 / 2, 0.5]) cube([1.05, 4.00, 24]);
                translate([-4.00 / 2, -1.25 / 2, 0.5]) cube([4.00, 1.25, 24]);
            }

            union() {
                outer() { children(); }

                difference() {
                    translate([0, 0, -3]) polygon_pyramid(16, 4.3, h = 24);
                    dish(height - thickness, fa = invisible_fa);
                }
            }
        }
    }

    module home_position_mark() {
        translate([
                0,
                -top_h / 2,
                height + dish_position_z - 1.5
        ]) {
            rotate([0, tilt_xa, 0]) {
                minkowski() {
                    cube([1, 0.001, 2.75], center = true);
                    sphere(0.3);
                }
            }
        }
    }

    union() {
        difference() {
            union() {
                outer() { children(); }

                if (is_home_position) {
                    home_position_mark();
                }
            }

            inner();
        }

        pillar() { children(); }
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
            polygon_pyramid(16, 2.81, h = 4);
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

module keycap_with_stem(x, y, case_x, case_y, w = 1, h = 1,
                        is_cylindrical = false, is_home_position = false)
{
    keycap(x, y, w, h, is_cylindrical, is_home_position) {
        translate([key_pitch * -case_x, key_pitch * -case_y]) case_curve();
    }

    translate([0, 0, -3]) %stem_holder();
}

for (y = [-1 : 2]) {
    for (x = [-2 : 4]) {
        wall_n = y == 1.5;
        wall_e = x == 4;
        wall_s = y == -1.5;
        wall_w = x == -2;

        translate([16 * x, 16 * y]) keycap_with_stem(
                x, y, case_x = x, case_y = y,
                walls = [wall_n, wall_e, wall_s, wall_w],
                is_home_position = x == 2 && y == -0.5
        );
    }
}

translate([16 * -1.625, 16 * -2]) keycap_with_stem(-1.125, -2.5, case_x = -1.625, case_y = -2, w = 1.75, h = 1);
translate([16 * -0.125, 16 * -2]) keycap_with_stem(-2.000,  0.5, case_x = -0.125, case_y = -2, w = 1.25, h = 1, is_cylindrical = true);
translate([16 *  1.250, 16 * -2]) keycap_with_stem(-1.250,  0.5, case_x =  1.250, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  2.750, 16 * -2]) keycap_with_stem( 1.250,  0.5, case_x =  2.750, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  4.000, 16 * -2]) keycap_with_stem( 2.500,  0.5, case_x =  4.000, case_y = -2, w = 1.00, h = 1, is_cylindrical = true);

/*
layout(position_x, position_y, rotation_x, rotation_y, rotation_z, is_upper_layer) {
    keycap(x, y, w, h, is_cylindrical, is_home_position);
}
*/
