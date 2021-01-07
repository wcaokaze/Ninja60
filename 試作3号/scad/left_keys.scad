
include <shared.scad>;
include <keycap.scad>;
include <case.scad>;

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
