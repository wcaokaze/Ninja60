
include <shared.scad>;

keycap_visible_fa = 15; // 0.6;
keycap_invisible_fa = 8;

key_pitch = 16;
keycap_margin = 0.375;
keycap_thickness = 1.5;

keycap_height = 6.93;
dish_r = 20;

tilt_xr = 260;
tilt_yr = 130;

function arc_length_to_angle(arc_r, length) = length * 360 / 2 / PI / arc_r;

module thumb_keycap(arc_r, arc_start_a, arc_end_a, h, tilt_a) {
    bottom_inner_r = arc_r - h / 2 + 0.375;
    bottom_outer_r = arc_r + h / 2 - 0.375;
    bottom_arc_start_a = arc_start_a + arc_length_to_angle(bottom_inner_r, 0.375);
    bottom_arc_end_a   = arc_end_a   - arc_length_to_angle(bottom_inner_r, 0.375);

    top_inner_r = arc_r - h / 2 + 2;
    top_outer_r = arc_r + h / 2 - 2;
    top_arc_start_a = arc_start_a + arc_length_to_angle(bottom_inner_r, 2);
    top_arc_end_a   = arc_end_a   - arc_length_to_angle(bottom_inner_r, 2);

    module arc(outer_r, inner_r, start_a, end_a) {
        intersection() {
            difference() {
                cylinder(r = outer_r, h = 0.01, $fa = keycap_visible_fa);
                cylinder(r = inner_r, h = 0.01, $fa = keycap_visible_fa);
            }

            linear_extrude(h = 0.01) {
                polygon([
                    [0, 0],
                    [100 * cos(start_a), 100 * sin(start_a)],
                    [100 * cos(end_a),   100 * sin(end_a)  ]
                ]);
            }
        }
    }

    module round_arc(outer_r, inner_r, start_a, end_a, round_r) {
        minkowski() {
            arc(outer_r - round_r,
                inner_r + round_r,
                start_a + arc_length_to_angle(inner_r, round_r),
                end_a   - arc_length_to_angle(inner_r, round_r));

            cylinder(r = round_r, h = 0.001, $fa = keycap_visible_fa);
        }
    }

    module outer() {
        difference() {
            hull() {
                round_arc(bottom_outer_r, bottom_inner_r, bottom_arc_start_a, bottom_arc_end_a, 1);
                translate([0, 0, 10]) round_arc(top_outer_r, top_inner_r, top_arc_start_a, top_arc_end_a, 1);
            }

            hull() {
                cylinder(r = bottom_inner_r, h = 0.01, $fa = keycap_visible_fa);
                translate([0, 0, 10]) cylinder(r = top_inner_r, h = 0.1, $fa = keycap_visible_fa);
            }
        }
    }

    outer();
}

/*
 * キーキャップ。子を渡すとintersectionによって外形が調整されます
 *
 * x                - 東西方向のキーの位置。U(1Uのキーの長さを1とする)単位。
 * y                - 南北方向のキーの位置。U単位。
 * w                - 東西方向のキーの長さ。U単位。
 * h                - 南北方向のキーの長さ。U単位。
 * is_cylindrical   - 上面の凹みの形状。trueで円筒形、falseで球形。
 * is_home_position - trueにするとホームポジションを指で確かめるための突起がつきます。
 *                    いまのところ正常っぽく生成できるのは
 *                    (x == 2 || x == -2) && y == 0 の場合のみ
 * bottom_z         - 外形の底面のZ座標。
 *                    この高さにおける幅がキーピッチいっぱいに広がるため、
 *                    調整用の子に合わせてこの値を指定することで、
 *                    なるべくキー間の隙間を詰める効果を期待できます。
 * left_wall_angle  - 外形の左側に角度がつきます。0が北、90が西向き
 *                    yが0以外の場合、位置に合わせてキーの幅も変わります
 * right_wall_angle - 外形の左側に角度がつきます。0が北、-90が東向き
 *                    yが0以外の場合、位置に合わせてキーの幅も変わります
 * polishing_margin - ステムの十字部が太くなります。mm単位
 *                    磨きなどする場合に削れる分を想定して指定しましょう
 */
module keycap(x, y, w = 1, h = 1,
              is_cylindrical = false, is_home_position = false,
              bottom_z = 0,
              left_wall_angle = 0, right_wall_angle = 0,
              polishing_margin = 0)
{
    top_w = key_pitch * w - 4;
    top_h = key_pitch * h - 4;
    bottom_w = key_pitch * w - keycap_margin * 2;
    bottom_h = key_pitch * h - keycap_margin * 2;

    bottom_north_y =  bottom_h / 2;
    bottom_south_y = -bottom_h / 2;
    bottom_north_left_x  = -bottom_w / 2 + (key_pitch * y + bottom_north_y) / tan(90 + left_wall_angle);
    bottom_north_right_x =  bottom_w / 2 + (key_pitch * y + bottom_north_y) / tan(90 + right_wall_angle);
    bottom_south_left_x  = -bottom_w / 2 + (key_pitch * y + bottom_south_y) / tan(90 + left_wall_angle);
    bottom_south_right_x =  bottom_w / 2 + (key_pitch * y + bottom_south_y) / tan(90 + right_wall_angle);

    tilt_xa = acos(key_pitch * x / tilt_xr);
    tilt_ya = acos(key_pitch * y / tilt_yr);

    dish_position_z = tilt_xr * (1 - sin(tilt_xa)) + tilt_yr * (1 - sin(tilt_ya));
    top_z = keycap_height + dish_position_z + 3;

    module dish(keycap_height, fa) {
        if (is_cylindrical) {
            translate([
                    -dish_r * cos(tilt_xa),
                    0,
                    keycap_height + dish_position_z + dish_r - 3
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
                    keycap_height + dish_position_z + dish_r - 3
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
            hull() {
                translate([0, 0, top_z]) minkowski() {
                    cube([top_w - 2, top_h - 2, 0.01], center = true);
                    cylinder(r = 1, h = 0.001, $fa = keycap_visible_fa);
                }

                translate([0, 0, bottom_z]) minkowski() {
                    linear_extrude(0.01) polygon([
                        [bottom_north_left_x  + 1, bottom_north_y - 1],
                        [bottom_north_right_x - 1, bottom_north_y - 1],
                        [bottom_south_right_x - 1, bottom_south_y + 1],
                        [bottom_south_left_x  + 1, bottom_south_y + 1]
                    ]);

                    cylinder(r = 1, h = 0.001, $fa = keycap_visible_fa);
                }
            }
        }

        intersection() {
            difference() {
                round_rect_pyramid();
                dish(keycap_height, fa = keycap_visible_fa);
            }

            children();
        }
    }

    module inner() {
        module rect_pyramid(bottom_w, bottom_h) {
            hull() {
                translate([0, 0, top_z]) {
                    cube(
                        [
                            top_w - keycap_thickness * 2,
                            top_h - keycap_thickness * 2,
                            0.01
                        ],
                        center = true
                    );
                }

                translate([0, 0, bottom_z]) {
                    linear_extrude(0.01) polygon([
                        [bottom_north_left_x  + keycap_thickness, bottom_north_y - keycap_thickness],
                        [bottom_north_right_x - keycap_thickness, bottom_north_y - keycap_thickness],
                        [bottom_south_right_x - keycap_thickness, bottom_south_y + keycap_thickness],
                        [bottom_south_left_x  + keycap_thickness, bottom_south_y + keycap_thickness]
                    ]);
                }
            }
        }

        difference() {
            rect_pyramid();
            dish(keycap_height - keycap_thickness, fa = keycap_invisible_fa);
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

                north_south_thickness = 1.05 + polishing_margin;
                east_west_thickness   = 1.25 + polishing_margin;

                translate([0, 0, 12.5]) cube([north_south_thickness, 4, 24], center = true);
                translate([0, 0, 12.5]) cube([4, east_west_thickness,   24], center = true);
            }

            union() {
                outer() { children(); }

                difference() {
                    translate([0, 0, -3]) polygon_pyramid(16, 4.3, h = 24);
                    dish(keycap_height - keycap_thickness, fa = keycap_invisible_fa);
                }
            }
        }
    }

    module home_position_mark() {
        translate([
                0,
                -top_h / 2,
                keycap_height + dish_position_z - 2
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
