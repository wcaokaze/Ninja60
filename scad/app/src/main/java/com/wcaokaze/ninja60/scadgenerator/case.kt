
include <shared.scad>;

case_fa = 2;

case_curve_r = 850;
case_south_r = 380;
case_north_r = 255;

case_south_x = 16 * 1;
case_north_x = 16 * 0;

case_start_angle = -90;
case_end_angle   = -80;

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

    translate([0, key_pitch_v * -0.5, case_curve_r]) union() {
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

function case_y_to_angle(y) = atan2(-case_curve_r, (y + 0.5 * key_pitch_v));

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
