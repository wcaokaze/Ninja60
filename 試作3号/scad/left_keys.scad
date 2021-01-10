
include <shared.scad>;
include <keycap.scad>;
include <case.scad>;
include <stem_holder.scad>;

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
                        left_wall_angle = 0, right_wall_angle = 0,
                        is_cylindrical = false, is_home_position = false)
{
    keycap(
        x, y, w, h,
        is_cylindrical, is_home_position,
        bottom_z = case_curve_z(
            key_pitch * close_origin(case_x, 0.5),
            key_pitch * (case_y - 0.5)
        ),
        left_wall_angle  = left_wall_angle,
        right_wall_angle = right_wall_angle
    ) {
        translate([key_pitch * -case_x, key_pitch * -case_y]) case_curve();
    }

    translate([0, 0, -3]) %stem_holder();
}

keycap_half_width = key_pitch / 2 - keycap_margin;

// x = [-2, -1]
translate([
    -keycap_half_width * cos(0) - keycap_margin * 2 * cos(2),
    -keycap_half_width * sin(0) - keycap_margin * 2 * sin(2)
]) rotate([0, 0, 2]) {
    for (y = [1 : 4]) {
        translate([-keycap_half_width - key_pitch, key_pitch * y]) keycap_with_stem(
                -2, y - 2, case_x = -2, case_y = y - 2
        );
    }

    for (y = [1 : 4]) {
        translate([-keycap_half_width, key_pitch * y]) keycap_with_stem(
                -1, y - 2, case_x = -1, case_y = y - 2,
                right_wall_angle = -1
        );
    }
}

// x = 0
translate([
    keycap_half_width * cos(0),
    keycap_half_width * sin(0)
]) rotate([0, 0, 0]) {
    for (y = [1.5 : 4.5]) {
        translate([-keycap_half_width, key_pitch * y]) keycap_with_stem(
                0, y - 2, case_x = 0, case_y = y - 2,
                left_wall_angle = 1, right_wall_angle = -1
        );
    }
}

// x = 1
translate([
    keycap_half_width * cos(0) + keycap_margin * 2 * cos(-2),
    keycap_half_width * sin(0) + keycap_margin * 2 * sin(-2)
]) rotate([0, 0, -2]) {
    for (y = [1.75 : 4.75]) {
        translate([keycap_half_width, key_pitch * y]) keycap_with_stem(
                1, y - 2, case_x = 1, case_y = y - 2,
                left_wall_angle = 1, right_wall_angle = -1
        );
    }
}

// x = 2
translate([
    keycap_half_width * cos(0) + key_pitch * cos(-2) + keycap_margin * 2 * cos(-4),
    keycap_half_width * sin(0) + key_pitch * sin(-2) + keycap_margin * 2 * sin(-4)
]) rotate([0, 0, -4]) {
    for (y = [1.25 : 4.25]) {
        translate([keycap_half_width, key_pitch * y]) keycap_with_stem(
                2, y - 2, case_x = 2, case_y = y - 2,
                left_wall_angle = 1, right_wall_angle = -1,
                is_home_position = (y == 2.25)
        );
    }
}

// x = [3, 4]
translate([
    keycap_half_width * cos(0) + key_pitch * cos(-2) + key_pitch * cos(-4) + keycap_margin * 2 * cos(-6),
    keycap_half_width * sin(0) + key_pitch * sin(-2) + key_pitch * sin(-4) + keycap_margin * 2 * sin(-6)
]) rotate([0, 0, -6]) {
    for (y = [1 : 4]) {
        translate([keycap_half_width, key_pitch * y]) keycap_with_stem(
                3, y - 2, case_x = 3, case_y = y - 2,
                left_wall_angle = 1
        );
    }

    translate([key_pitch + keycap_half_width, key_pitch * 0.5]) keycap_with_stem(4, 0, case_x = 4, case_y = -1.75, is_cylindrical = true);
    translate([key_pitch + keycap_half_width, key_pitch * 2.0]) keycap_with_stem(4, 1, case_x = 4, case_y = -0.25, w = 1, h = 2);
    translate([key_pitch + keycap_half_width, key_pitch * 3.5]) keycap_with_stem(4, 1, case_x = 4, case_y =  1.25);
    translate([key_pitch + keycap_half_width, key_pitch * 4.5]) keycap_with_stem(4, 2, case_x = 4, case_y =  2.25);
}

translate([16 * -0.75, 0]) rotate([0, 0, 1]) {
    translate([16 * -0.875, 0]) keycap_with_stem(-1.625, -2.0, case_x = -1.625, case_y = -2, w = 1.75, h = 1);
}

translate([16 * 0.125, 16 * 0]) keycap_with_stem(-2, 1.5, case_x = -0.125, case_y = -2, w = 1.75, h = 1, is_cylindrical = true);
translate([16, 16 * 0 - 65]) thumb_keycap(65, 35, 52, 16);
translate([16, 16 * 0 - 65]) thumb_keycap(65, 52, 71, 16);
translate([16, 16 * 0 - 65]) thumb_keycap(65, 71, 90, 16);

/*
translate([16 * -1.625, 16 * -2]) keycap_with_stem(-1.625, -2, case_x = -1.625, case_y = -2, w = 1.75, h = 1);
translate([16 * -0.125, 16 * -2]) keycap_with_stem(-2.000,  1, case_x = -0.125, case_y = -2, w = 1.25, h = 1, is_cylindrical = true);
translate([16 *  1.250, 16 * -2]) keycap_with_stem(-1.250,  1, case_x =  1.250, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  2.750, 16 * -2]) keycap_with_stem( 1.250,  1, case_x =  2.750, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  4.000, 16 * -2]) keycap_with_stem( 2.500,  1, case_x =  4.000, case_y = -2, w = 1.00, h = 1, is_cylindrical = true);
*/
