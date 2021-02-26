
include <shared.scad>;
use <../../res/Cica-Regular.ttf>;

enable_only_outer = true;
enable_legends    = false;

keycap_invisible_fa = 8;

key_pitch_h = 18;
key_pitch_v = 16;
keycap_margin = 0.375;
keycap_thickness = 1.5;
keycap_height = 6.93;
keycap_wall_fa = 15; // 2;

dish_r = 15;
dish_fa = 15; // 2;

tilt_xr = 260;
tilt_yr = 130;

thumb_tilt_r = 16;
thumb_tilt_a = 3;

function arc_length_to_angle(arc_r, length) = length * 360 / 2 / PI / arc_r;

/**
 * 親指用キーキャップ。
 * 底が扇型で皿部分は通常のキーキャップと同様長方形にシリンドリカルのカーブ。
 *
 * arc_r            - 底の扇型の半径
 * arc_start_a      - 底の扇型の開始角度
 * arc_end_a        - 底の扇型の終了角度
 * h                - キーキャップの長さ。mm単位
 *                    「半径arc_r mmのarc_start_a°からarc_end_a°までの扇型」から
 *                    「半径(arc_r - h)mmのarc_start_a°からarc_end_a°までの扇型」を
 *                    くりぬいたバウムクーヘン型がキーキャップの底面となる。
 * dish_offset      - 皿の位置。底面の扇型で言うところの0°の位置から
 *                    扇型の接線の方向(Y軸と並行の方向)に動く(mm単位)
 *                    皿は円筒形なので位置をずらすと結果的に皿が傾いているかのように見える
 * polishing_margin - ステムの十字部が太くなります。mm単位
 *                    磨きなどする場合に削れる分を想定して指定しましょう
 */
module thumb_keycap(arc_r, arc_start_a, arc_end_a, dish_offset, h, polishing_margin = 0) {
    top_w = 18 - (key_pitch_h - 11);
    top_h = h  - (key_pitch_v - 11);

    bottom_inner_r  = arc_r - h     + 0.375;
    bottom_center_r = arc_r - h / 2;
    bottom_outer_r  = arc_r         - 0.375;

    bottom_arc_start_a = arc_start_a + arc_length_to_angle(bottom_inner_r, 0.375);
    bottom_arc_end_a   = arc_end_a   - arc_length_to_angle(bottom_inner_r, 0.375);

    dish_position_z = dish_offset * sin(-acos(dish_offset / dish_r));
    bottom_z = 0;
    top_z = keycap_height + dish_position_z;

    module dish(fa) {
        translate([bottom_center_r, dish_offset, top_z + dish_r - 2]) {
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

        top_inner_x = bottom_center_r + -top_h / 2;
        top_outer_x = bottom_center_r +  top_h / 2;
        function top_y(a) = -top_w / 2 + top_w * (a - start_a) / (end_a - start_a);

        points = [
            [
                for (a = [start_a, end_a]) [
                    bottom_inner_x(a) + (top_inner_x - bottom_inner_x(a)) * (z / top_z),
                    bottom_inner_y(a) + (top_y(a)    - bottom_inner_y(a)) * (z / top_z)
                ]
            ],
            [
                [
                    bottom_inner_x(end_a) + (top_inner_x  - bottom_inner_x(end_a)) * (z / top_z),
                    bottom_inner_y(end_a) + (top_y(end_a) - bottom_inner_y(end_a)) * (z / top_z)
                ]
            ],
            [
                for (a = [end_a : (start_a - end_a) / 16 : start_a]) [
                    bottom_outer_x(a) + (top_outer_x - bottom_outer_x(a)) * (z / top_z),
                    bottom_outer_y(a) + (top_y(a)    - bottom_outer_y(a)) * (z / top_z)
                ]
            ],
            [
                [
                    bottom_outer_x(start_a) + (top_outer_x    - bottom_outer_x(start_a)) * (z / top_z),
                    bottom_outer_y(start_a) + (top_y(start_a) - bottom_outer_y(start_a)) * (z / top_z)
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
        bottom_r = 0;
        top_r = 3;

        function get_rate(z) = (z - bottom_z) / (top_z - bottom_z);
        function r_at(z) = bottom_r + (top_r - bottom_r) * get_rate(z);

        difference() {
            union() {
                step = 0.5;
                for (z = [bottom_z : step : top_z]) {
                    hull() {
                        round_arc(
                            bottom_outer_r,
                            bottom_inner_r,
                            bottom_arc_start_a,
                            bottom_arc_end_a,
                            top_w, top_h,
                            r_at(z),
                            z,
                            keycap_wall_fa
                        );

                        round_arc(
                            bottom_outer_r,
                            bottom_inner_r,
                            bottom_arc_start_a,
                            bottom_arc_end_a,
                            top_w, top_h,
                            r_at(z + step),
                            z + step,
                            keycap_wall_fa
                        );
                    }
                }
            }

            hull() {
                cylinder(r = bottom_inner_r, h = 0.01, $fa = keycap_wall_fa);
                translate([0, 0, top_z]) cube([bottom_center_r * 2 - top_h, top_w, 0.01], center = true);
            }

            dish(dish_fa);
        }
    }

    module inner() {
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

                translate([bottom_center_r, 0, top_z - keycap_thickness]) {
                    cube(center = true, [
                        top_w - keycap_thickness * 2,
                        top_h - keycap_thickness * 2,
                        0.01
                    ]);
                }
            }

            hull() {
                cylinder(
                    r = bottom_inner_r + keycap_thickness,
                    h = 0.01, $fa = keycap_invisible_fa
                );

                translate([0, 0, top_z - keycap_thickness]) {
                    cube(center = true, [
                        bottom_center_r * 2 - (top_h - keycap_thickness * 2),
                        top_w - keycap_thickness * 2,
                        0.01
                    ]);
                }
            }

            translate([0, 0, -keycap_thickness]) dish(keycap_invisible_fa);
        }
    }

    module pillar() {
        intersection() {
            union() {
                translate([0, 0, 2.5]) difference() {
                    cylinder(r = bottom_center_r + 1, h = 24, $fa = keycap_invisible_fa);
                    cylinder(r = bottom_center_r - 1, h = 24, $fa = keycap_invisible_fa);
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
    top_w = 13 + key_pitch_h * (w - 1);
    top_h = 13 + key_pitch_v * (h - 1);
    bottom_w = key_pitch_h * w - keycap_margin * 2;
    bottom_h = key_pitch_v * h - keycap_margin * 2;

    bottom_north_y =  bottom_h / 2;
    bottom_south_y = -bottom_h / 2;
    bottom_north_left_x  = -bottom_w / 2 + (wall_y + bottom_north_y) / tan(90 + left_wall_angle);
    bottom_north_right_x =  bottom_w / 2 + (wall_y + bottom_north_y) / tan(90 + right_wall_angle);
    bottom_south_left_x  = -bottom_w / 2 + (wall_y + bottom_south_y) / tan(90 + left_wall_angle);
    bottom_south_right_x =  bottom_w / 2 + (wall_y + bottom_south_y) / tan(90 + right_wall_angle);

    // このキーキャップの原点から見た、本来の原点の座標。
    keyboard_origin = [-x * key_pitch_h, -y * key_pitch_v];

    tilt_xa = asin(-keyboard_origin.x / tilt_xr);
    tilt_ya = asin(-keyboard_origin.y / tilt_yr);

    /*
     * 高さ tilt_xr の点を中心としてX方向に(Y軸方向の直線を軸として) tilt_xa° 回転
     * 高さ tilt_yr の点を中心としてY方向に(X軸方向の直線を軸として) tilt_ya° 回転
     * X軸方向に -cos(tilt_xr) 、Y軸方向に -cos(tilt_yr) 移動する。
     *
     * 要するにtilt_xa, tilt_yaで回転し、XY座標は回転前の位置に戻してZ座標だけそのままにする。
     */
    module rotate_for_tilt() {
        translate([0, 0, tilt_xr * (1 - cos(-tilt_xa)) + tilt_yr * (1 - cos(tilt_ya))]) {
            rotate([tilt_ya, -tilt_xa]) {
                children();
            }
        }
    }

    function rotate_point_for_tilt(point) = transition_point(
        [
            point.x * cos(-tilt_xa) + point.z * sin(-tilt_xa),
            point.y * cos( tilt_ya) + point.x * sin( tilt_ya) * sin(-tilt_xa) - point.z * sin(tilt_ya) * cos(-tilt_xa),
            point.y * sin( tilt_ya) - point.x * cos( tilt_ya) * sin(-tilt_xa) + point.z * cos(tilt_ya) * cos(-tilt_xa)
        ],
        [0, 0, tilt_xr * (1 - cos(-tilt_xa)) + tilt_yr * (1 - cos(tilt_ya))]
    );

    module dish(keycap_height, fa) {
        if (is_cylindrical) {
            minkowski() {
                cube(center = true, [
                        key_pitch_h * (w - 1) + 0.001,
                        key_pitch_v * (h - 1) + 0.001,
                        0.001
                ]);

                translate([0, 0, keycap_height]) {
                    rotate_for_tilt() {
                        translate([0, 0, dish_r]) rotate([-tilt_ya, 0, 0]) {
                            cylinder(r = dish_r, h = 32, center = true, $fa = fa);
                        }
                    }
                }
            }
        } else {
            minkowski() {
                translate([0, (is_fluent_to_south ? -key_pitch_v : 0)]) {
                    cube([
                            0.001,
                            0.001 +
                                (is_fluent_to_north ? key_pitch_v : 0) +
                                (is_fluent_to_south ? key_pitch_v : 0),
                            0.001
                    ]);
                }

                translate([0, 0, keycap_height]) {
                    rotate_for_tilt() {
                        translate([0, 0, dish_r]) {
                            sphere(dish_r, $fa = fa);
                        }
                    }
                }
            }
        }
    }

    module legend(text) {
        intersection() {
            translate([0, 0, keycap_height]) {
                rotate_for_tilt() {
                    translate([0, 0, -0.5]) linear_extrude(8) text(
                        text, size = 6,
                        font = "Cica", halign = "center", valign = "center",
                        direction = "ltr", language = "en",
                        $fa = 1
                    );
                }
            }

            dish(keycap_height - 0.5, dish_fa);
        }
    }

    module outer() {
        module round_rect_pyramid() {
            function dish_position(x, y) = transition_point(
                rotate_point_for_tilt([
                    x, y,
                    dish_r * (1 - sin(acos(x / dish_r))) +
                    dish_r * (1 - sin(acos(y / dish_r)))
                ]),
                [0, 0, keycap_height]
            );

            function extract_xy(point) = [point.x, point.y];

            module polygon_from_3d(points) {
                linear_extrude(0.1) polygon([
                    for (p = points) extract_xy(p)
                ]);
            }

            bottom_r = 0;
            top_r = 3;

            bottom_points = [
                [bottom_north_left_x  + bottom_r, bottom_north_y - bottom_r, bottom_z],
                [bottom_north_right_x - bottom_r, bottom_north_y - bottom_r, bottom_z],
                [bottom_south_right_x - bottom_r, bottom_south_y + bottom_r, bottom_z],
                [bottom_south_left_x  + bottom_r, bottom_south_y + bottom_r, bottom_z]
            ];

            top_points = [
                dish_position(-(top_w / 2 - top_r),  (top_h / 2 - top_r)),
                dish_position( (top_w / 2 - top_r),  (top_h / 2 - top_r)),
                dish_position( (top_w / 2 - top_r), -(top_h / 2 - top_r)),
                dish_position(-(top_w / 2 - top_r), -(top_h / 2 - top_r))
            ];

            top_z = max(
                dish_position(-top_w / 2,  top_h / 2).z,
                dish_position( top_w / 2,  top_h / 2).z,
                dish_position( top_w / 2, -top_h / 2).z,
                dish_position(-top_w / 2, -top_h / 2).z
            );

            function get_rate(z) = (z - bottom_z) / (top_z - bottom_z);
            function r_at(z) = bottom_r + (top_r - bottom_r) * get_rate(z);

            union() {
                step = 0.5;
                for (z = [bottom_z : step : top_z]) {
                    hull() {
                        translate([0, 0, z]) minkowski() {
                            polygon_from_3d([
                                z_point_on_line(bottom_points[0], top_points[0], z),
                                z_point_on_line(bottom_points[1], top_points[1], z),
                                z_point_on_line(bottom_points[2], top_points[2], z),
                                z_point_on_line(bottom_points[3], top_points[3], z),
                            ]);

                            cylinder(r = r_at(z), h = 0.001, $fa = keycap_wall_fa);
                        }

                        translate([0, 0, z + step]) minkowski() {
                            polygon_from_3d([
                                z_point_on_line(bottom_points[0], top_points[0], z + step),
                                z_point_on_line(bottom_points[1], top_points[1], z + step),
                                z_point_on_line(bottom_points[2], top_points[2], z + step),
                                z_point_on_line(bottom_points[3], top_points[3], z + step),
                            ]);

                            cylinder(r = r_at(z + step), h = 0.001, $fa = keycap_wall_fa);
                        }
                    }
                }
            }
        }

        intersection() {
            difference() {
                round_rect_pyramid();
                dish(keycap_height, fa = dish_fa);
            }

            children();
        }
    }

    module inner() {
        module rect_pyramid(bottom_w, bottom_h) {
            hull() {
                top_z = keycap_height;

                translate([0, 0, top_z]) {
                    rotate_for_tilt() {
                        cube(
                            [
                                top_w - keycap_thickness * 2,
                                top_h - keycap_thickness * 2,
                                0.01
                            ],
                            center = true
                        );
                    }
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
        translate([0, 0, keycap_height]) {
            rotate_for_tilt() {
                translate([-0.5, -top_h / 2 + 0.15, -1.5]) minkowski() {
                    cube([1, 0.001, 2.75]);
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
