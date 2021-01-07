
$fs = 0.1;
case_fa = 2;
visible_fa = 15; // 0.6;
invisible_fa = 8;

key_pitch = 16;
thickness = 1.5;

height = 6.93;
dish_r = 20;

tilt_xr = 260;
tilt_yr = 130;

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

// aを0に近くなるようにdで減算します
function close_origin(a, d) =
    (a < -d) ?
        a + d
    : (a < d) ?
        0
    :
        a - d
    ;

case_start_angle = -90;
case_end_angle   = -85;

function interpolate(start, end, rate) = start + (end - start) * rate;

/*
 * キーボードに対して垂直に東西方向に立てた半径case_south_rの円を、
 * キーボードに対して垂直に南北方向に立てた半径case_curve_rの円弧上を
 * 走らせた場合に残る残像の形状です。
 *
 * このとき、移動する円の半径は徐々にcase_south_rからcase_north_rに変化し、
 * x座標は徐々にcase_south_xからcase_north_xに移動します。
 *
 * 具体的には
 * 南端に立てた円が徐々に小さくなりながら西に移動しながら円弧を描いて北に移動しています
 * 無茶苦茶ですね
 */
module case_curve() {
    step = 0.1;

    translate([0, key_pitch * -2.5, case_curve_r]) union() {
        for (i = [0 : step : 1]) {
            a_angle = interpolate(case_start_angle, case_end_angle, i);
            a_y = case_curve_r * cos(a_angle);
            a_z = case_curve_r * sin(a_angle);

            b_angle = interpolate(case_start_angle, case_end_angle, i + step);
            b_y = case_curve_r * cos(b_angle);
            b_z = case_curve_r * sin(b_angle);

            a_r = interpolate(case_south_r, case_north_r, i);
            b_r = interpolate(case_south_r, case_north_r, i + step);

            a_x = interpolate(case_south_x, case_north_x, i);
            b_x = interpolate(case_south_x, case_north_x, i + step);

            hull() {
                translate([a_x, a_y, a_z + a_r]) rotate([90, 0]) cylinder(r = a_r, h = 0.01, $fa = case_fa);
                translate([b_x, b_y, b_z + b_r]) rotate([90, 0]) cylinder(r = b_r, h = 0.01, $fa = case_fa);
            }
        }
    }
}

function case_y_to_angle(y) = atan2(-case_curve_r, (y + 2.5 * key_pitch));

function case_y_to_interpolate_rate(y)
    = (case_y_to_angle(y) - case_start_angle) / (case_end_angle - case_start_angle);

function case_y_to_cylinder_r(y)
    = interpolate(case_south_r, case_north_r, case_y_to_interpolate_rate(y));

function case_pos_to_cylinder_angle(x, y)
    = atan2(-case_y_to_cylinder_r(y),
            x - interpolate(case_south_x, case_north_x, case_y_to_interpolate_rate(y))
    );

/*
 * case_curveの(x, y)におけるz座標。
 *
 * x - 東西方向の位置。mm単位。
 * y - 南北方向の位置。mm単位。
 */
function case_curve_z(x, y)
    = case_curve_r * (1 + sin(case_y_to_angle(y)))
    + case_y_to_cylinder_r(y) * (1 + sin(case_pos_to_cylinder_angle(x, y)));

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
 */
module keycap(x, y, w = 1, h = 1,
              is_cylindrical = false, is_home_position = false,
              bottom_z = 0)
{
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
                translate([0, 0, top_z])    round_rect(top_w,    top_h,    1);
                translate([0, 0, bottom_z]) round_rect(bottom_w, bottom_h, 1);
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
                translate([0, 0, top_z])    cube([top_w,    top_h,    0.01], center = true);
                translate([0, 0, bottom_z]) cube([bottom_w, bottom_h, 0.01], center = true);
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
                height + dish_position_z - 2
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

/*
 * キースイッチのステムを差し込む部分。十字の穴が貫通していて、
 * 上からはキーキャップ、下からはキースイッチが挿さるようになっています
 */
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

/*
 * キーを2層に並べるときに便利なやつ
 *
 * x              - 東西方向の位置。U単位
 * y              - 南北方向の位置。U単位
 * rotation_x     - X軸の回転角度。degree
 * rotation_y     - Y軸の回転角度。degree
 * rotation_z     - Z軸の回転角度。degree
 * is_upper_layer - trueで上の層に配置。このとき自動的にX軸で180°回転して裏返されます
 */
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

/*
 * デバッグ用。キーキャップに透明のステムホルダーが挿さった状態のモデルを生成します
 *
 * x - キーの東西方向の位置。
 * y - キーの南北方向の位置。
 * case_x - ケースの原点に対してキーが配置される位置。U単位。
 *          あえてxと一致させないことでx == 0のキーを東端に配置することなどが可能
 * case_y - ケースの原点に対してキーが配置される位置。U単位。
 *          あえてyと一致させないことでy == 0のキーを南端に配置することなどが可能
 */
module keycap_with_stem(x, y, case_x, case_y, w = 1, h = 1,
                        is_cylindrical = false, is_home_position = false)
{
    keycap(
        x, y, w, h,
        is_cylindrical, is_home_position,
        bottom_z = case_curve_z(
            key_pitch * close_origin(case_x, 0.5),
            key_pitch * (case_y - 0.5)
        )
    ) {
        translate([key_pitch * -case_x, key_pitch * -case_y]) case_curve();
    }

    translate([0, 0, -3]) %stem_holder();
}

for (y = [-1 : 2]) {
    for (x = [-2 : 3]) {
        translate([16 * x, 16 * y]) keycap_with_stem(
                x, y, case_x = x, case_y = y,
                is_home_position = x == 2 && y == -0
        );
    }
}

translate([16 * 4, 16 *  1.0]) keycap_with_stem(4, 1, case_x = 4, case_y = 1);
translate([16 * 4, 16 *  2.0]) keycap_with_stem(4, 2, case_x = 4, case_y = 2);
translate([16 * 4, 16 * -0.5]) keycap_with_stem(4, 0, case_x = 4, case_y = -0.5, w = 1, h = 2);

translate([16 * -1.625, 16 * -2]) keycap_with_stem(-1.625, -2, case_x = -1.625, case_y = -2, w = 1.75, h = 1);
translate([16 * -0.125, 16 * -2]) keycap_with_stem(-2.000,  1, case_x = -0.125, case_y = -2, w = 1.25, h = 1, is_cylindrical = true);
translate([16 *  1.250, 16 * -2]) keycap_with_stem(-1.250,  1, case_x =  1.250, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  2.750, 16 * -2]) keycap_with_stem( 1.250,  1, case_x =  2.750, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  4.000, 16 * -2]) keycap_with_stem( 2.500,  1, case_x =  4.000, case_y = -2, w = 1.00, h = 1, is_cylindrical = true);

/*
layout(position_x, position_y, rotation_x, rotation_y, rotation_z, is_upper_layer) {
    keycap(x, y, w, h, is_cylindrical, is_home_position);
}
*/
