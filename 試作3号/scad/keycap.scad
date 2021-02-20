
include <shared.scad>;
use <../../res/Cica-Regular.ttf>;

enable_only_outer = true;
enable_legends    = false;

keycap_visible_fa = 15; // 0.6;
keycap_invisible_fa = 8;

key_pitch = 16;
keycap_margin = 0.375;
keycap_thickness = 1.5;

keycap_height = 6.93;
dish_r = 20;

tilt_xr = 260;
tilt_yr = 130;

thumb_tilt_r = 16;
thumb_tilt_a = 3;

function arc_length_to_angle(arc_r, length) = length * 360 / 2 / PI / arc_r;

/**
 * 親指用キーキャップ。
 * 底が扇型で皿部分は通常のキーキャップと同様長方形にシリンドリカルのカーブ。
 *
 * arc_r            - 底の扇型の半径。外側ではなくキーキャップの中央までの半径。
 * arc_start_a      - 底の扇型の開始角度
 * arc_end_a        - 底の扇型の終了角度
 * dish_offset      - 皿の位置。皿は円筒形なので位置をずらすと結果的に傾いているかのように見える
 * h                - キーキャップの長さ。mm単位
 * polishing_margin - ステムの十字部が太くなります。mm単位
 *                    磨きなどする場合に削れる分を想定して指定しましょう
 */
module thumb_keycap(arc_r, arc_start_a, arc_end_a, h, dish_offset, polishing_margin = 0) {
    bottom_inner_r = arc_r - h / 2 + 0.375;
    bottom_outer_r = arc_r + h / 2 - 0.375;
    bottom_arc_start_a = arc_start_a + arc_length_to_angle(bottom_inner_r, 0.375);
    bottom_arc_end_a   = arc_end_a   - arc_length_to_angle(bottom_inner_r, 0.375);

    dish_position_z = dish_offset * sin(-acos(dish_offset / dish_r));
    top_z = keycap_height + dish_position_z;

    module dish(fa) {
        translate([arc_r, dish_offset, top_z + dish_r - 2]) {
            rotate([90 - thumb_tilt_a, 0, 90]) {
                cylinder(r = dish_r, h = 32, center = true, $fa = fa);
            }
        }
    }

    module arc(outer_r, inner_r, start_a, end_a, top_w, top_h, z, fa) {
        function bottom_inner_x(a) = inner_r * cos(a);
        function bottom_inner_y(a) = inner_r * sin(a);
        function bottom_outer_x(a) = outer_r * cos(a);
        function bottom_outer_y(a) = outer_r * sin(a);

        top_inner_x = arc_r + -top_h / 2;
        top_outer_x = arc_r +  top_h / 2;
        function top_y(a) = -top_w / 2 + top_w * (a - start_a) / (end_a - start_a);

        points = [
            [
                for (a = [start_a, end_a]) [
                    bottom_inner_x(a) + (top_inner_x - bottom_inner_x(a)) * (z / top_z),
                    bottom_inner_y(a) + (top_y(a)    - bottom_inner_y(a)) * (z / top_z)
                ]
            ],
            [
                for (a = [end_a : (start_a - end_a) / 16 : start_a]) [
                    bottom_outer_x(a) + (top_outer_x - bottom_outer_x(a)) * (z / top_z),
                    bottom_outer_y(a) + (top_y(a)    - bottom_outer_y(a)) * (z / top_z)
                ]
            ]
        ];

        translate([0, 0, z]) linear_extrude(0.01) {
            polygon([ for (a = points) for (p = a) p ]);
        }
    }

    module round_arc(outer_r, inner_r, start_a, end_a, top_w, top_h, round_r, z, fa) {
        minkowski() {
            arc(
                outer_r - round_r,
                inner_r + round_r,
                start_a + arc_length_to_angle(inner_r, round_r),
                end_a   - arc_length_to_angle(inner_r, round_r),
                top_w - round_r * 2,
                top_h - round_r * 2,
                z,
                fa
            );

            cylinder(r = round_r, h = 0.01, $fa = fa);
        }
    }

    module outer() {
        top_w = key_pitch - 4;
        top_h = key_pitch - 4;

        step = 0.25;

        difference() {
            union() {
                for (z = [0 : step : top_z]) {
                    hull() {
                        round_arc(
                            bottom_outer_r,
                            bottom_inner_r,
                            bottom_arc_start_a,
                            bottom_arc_end_a,
                            top_w, top_h,
                            1,
                            z,
                            keycap_visible_fa
                        );

                        round_arc(
                            bottom_outer_r,
                            bottom_inner_r,
                            bottom_arc_start_a,
                            bottom_arc_end_a,
                            top_w, top_h,
                            1,
                            z + step,
                            keycap_visible_fa
                        );
                    }
                }
            }

            hull() {
                cylinder(r = bottom_inner_r, h = 0.01, $fa = keycap_visible_fa);
                translate([0, 0, top_z]) cube([arc_r * 2 - top_h, top_w, 0.01], center = true);
            }

            dish(keycap_visible_fa);
        }
    }

    module inner() {
        top_w = key_pitch - keycap_thickness - 4;
        top_h = key_pitch - keycap_thickness - 4;

        difference() {
            hull() {
                arc(
                    bottom_outer_r - keycap_thickness,
                    bottom_inner_r + keycap_thickness,
                    bottom_arc_start_a + arc_length_to_angle(bottom_inner_r, keycap_thickness),
                    bottom_arc_end_a   - arc_length_to_angle(bottom_inner_r, keycap_thickness),
                    top_w, top_h,
                    0,
                    keycap_invisible_fa
                );

                translate([arc_r, 0, top_z - keycap_thickness]) cube([top_w, top_h, 0.01], center = true);
            }

            hull() {
                cylinder(
                    r = bottom_inner_r + keycap_thickness,
                    h = 0.01, $fa = keycap_invisible_fa
                );

                translate([0, 0, top_z - keycap_thickness]) cube([arc_r * 2 - top_h, top_w, 0.01], center = true);
            }

            translate([0, 0, -keycap_thickness]) dish(keycap_invisible_fa);
        }
    }

    module pillar() {
        intersection() {
            union() {
                translate([0, 0, 2.5]) difference() {
                    cylinder(r = arc_r + 1, h = 24, $fa = keycap_invisible_fa);
                    cylinder(r = arc_r - 1, h = 24, $fa = keycap_invisible_fa);
                }

                translate([0, -1, 2.5]) cube([bottom_outer_r, 2, 24]);

                translate([(bottom_outer_r + bottom_inner_r) / 2, 0]) {
                    translate([0, 0, 2.5]) {
                        polygon_pyramid(16, 4.3, h = 24);
                    }

                    north_south_thickness = 1.05 + polishing_margin;
                    east_west_thickness   = 1.25 + polishing_margin;

                    translate([0, 0, 12.5]) cube([north_south_thickness, 4, 24], center = true);
                    translate([0, 0, 12.5]) cube([4, east_west_thickness,   24], center = true);
                }
            }

            outer();
        }
    }

    translate([-arc_r, 0]) union() {
        if (enable_only_outer) {
            outer();
        } else {
            difference() {
                outer();
                inner();
            }

            pillar();
        }
    }
}

/*
 * キーキャップ。子を渡すとintersectionによって外形が調整されます
 *
 * x                  - 東西方向のキーの位置。U(1Uのキーの長さを1とする)単位。
 * y                  - 南北方向のキーの位置。U単位。
 * w                  - 東西方向のキーの長さ。U単位。
 * h                  - 南北方向のキーの長さ。U単位。
 * legend             - 刻印の文字列。
 * is_fluent_to_north - 上面の凹みが北側のキーと繋がるようになります。
 *                      is_cylindricalがtrueの場合は無視されます。
 * is_fluent_to_south - 上面の凹みが南側のキーと繋がるようになります。
 *                      is_cylindricalがtrueの場合は無視されます。
 * is_cylindrical     - 上面の凹みの形状。trueで円筒形、falseで球形。
 * is_home_position   - trueにするとホームポジションを指で確かめるための突起がつきます。
 *                      いまのところ正常っぽく生成できるのは
 *                      (x == 2 || x == -2) && y == 0 の場合のみ
 * bottom_z           - 外形の底面のZ座標。
 *                      この高さにおける幅がキーピッチいっぱいに広がるため、
 *                      調整用の子に合わせてこの値を指定することで、
 *                      なるべくキー間の隙間を詰める効果を期待できます。
 * left_wall_angle    - 外形の左側に角度がつきます。0が北、90が西向き
 * right_wall_angle   - 外形の左側に角度がつきます。0が北、-90が東向き
 * wall_y             - left_wall_angle, right_wall_angleを指定する場合の
 *                      このキーの中心のY座標。mm単位
 *                      この値が大きいほどキーの幅が広くなることになりますね
 * polishing_margin   - ステムの十字部が太くなります。mm単位
 *                      磨きなどする場合に削れる分を想定して指定しましょう
 */
module keycap(x, y, w = 1, h = 1, legend,
              is_fluent_to_north = false, is_fluent_to_south = false,
              is_cylindrical = false, is_home_position = false, is_thin_pillar = false,
              bottom_z = 0,
              left_wall_angle = 0, right_wall_angle = 0, wall_y = 0,
              polishing_margin = 0)
{
    top_w = key_pitch * w - 4;
    top_h = key_pitch * h - 4;
    bottom_w = key_pitch * w - keycap_margin * 2;
    bottom_h = key_pitch * h - keycap_margin * 2;

    bottom_north_y =  bottom_h / 2;
    bottom_south_y = -bottom_h / 2;
    bottom_north_left_x  = -bottom_w / 2 + (wall_y + bottom_north_y) / tan(90 + left_wall_angle);
    bottom_north_right_x =  bottom_w / 2 + (wall_y + bottom_north_y) / tan(90 + right_wall_angle);
    bottom_south_left_x  = -bottom_w / 2 + (wall_y + bottom_south_y) / tan(90 + left_wall_angle);
    bottom_south_right_x =  bottom_w / 2 + (wall_y + bottom_south_y) / tan(90 + right_wall_angle);

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
                        cylinder(r = dish_r, h = 32, center = true, $fa = fa);
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
                        translate([
                                -key_pitch * (w - 1) / 2,
                                -key_pitch * (h - 1) / 2 +
                                        (is_fluent_to_south ? -key_pitch : 0)
                        ]) {
                            cube([
                                    key_pitch * (w - 1) + 0.001,
                                    key_pitch * (h - 1) + 0.001 +
                                            (is_fluent_to_north ? key_pitch : 0) +
                                            (is_fluent_to_south ? key_pitch : 0),
                                    0.001
                            ]);
                        }
                    }

                    sphere(dish_r, $fa = fa);
                }
            }
        }
    }

    module legend(text) {
        intersection() {
            translate([
                -4 * cos(tilt_xa),
                -4 * cos(tilt_ya),
                keycap_height + dish_position_z
            ]) {
                rotate([90 - tilt_ya, tilt_xa - 90]) {
                    linear_extrude(8, center = true) text(
                        text, size = 6,
                        font = "Cica", halign = "center", valign = "center",
                        direction = "ltr", language = "en",
                        $fa = keycap_visible_fa
                    );
                }
            }

            dish(keycap_height - 0.5, keycap_visible_fa);
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
                    polygon_pyramid(16, 4.3, h = is_thin_pillar ? 2 : 24);
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

    difference() {
        if (enable_only_outer) {
            outer() { children(); }
        } else {
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

        if (enable_legends) {
            legend(legend);
        }
    }
}
