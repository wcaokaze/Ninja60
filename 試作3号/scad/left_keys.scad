
include <shared.scad>;
include <keycap.scad>;
include <case.scad>;
include <stem_holder.scad>;
include <o_ring.scad>;

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
module keycap_with_stem(x, y, case_x, case_y, w = 1, h = 1, legend = "",
                        is_fluent_to_north = false, is_fluent_to_south = false,
                        left_wall_angle = 0, right_wall_angle = 0, wall_y = 0,
                        is_cylindrical = false, is_home_position = false, is_thin_pillar = false)
{
    keycap(
        x, y, w, h, legend,
        is_fluent_to_north, is_fluent_to_south,
        is_cylindrical, is_home_position, is_thin_pillar,
        bottom_z = case_curve_z(
            close_origin(case_x, key_pitch * 0.5),
            case_y - key_pitch * 0.5
        ),
        left_wall_angle  = left_wall_angle,
        right_wall_angle = right_wall_angle,
        wall_y = wall_y
    ) {
        translate([-case_x, -case_y]) case_curve();
    }

    translate([0, 0, -3]) %stem_holder();
    translate([0, 0, 0.5]) o_ring();
}

keycap_half_width = key_pitch / 2 - keycap_margin;
column_angle = 2.5;

// x = [-2, -1]
translate([
    -keycap_half_width * cos(column_angle * 0) - keycap_margin * 2 * cos(column_angle * 1),
    -keycap_half_width * sin(column_angle * 0) - keycap_margin * 2 * sin(column_angle * 1)
]) rotate([0, 0, column_angle * 1]) {
    s2 = -10.4;
    for (y = [0 : 2]) {
        case_x = key_pitch * -3;
        case_y = key_pitch * (y + 2) + s2;
        translate([-keycap_half_width - 2 * key_pitch, case_y]) keycap_with_stem(
                -3, y, case_x, case_y,
                is_thin_pillar = false
        );
    }

    s1 = -4;
    for (y = [-1 : 2]) {
        case_x = key_pitch * -2;
        case_y = key_pitch * (y + 2) + s1;
        translate([-keycap_half_width - key_pitch, case_y]) keycap_with_stem(
                -2, y, case_x, case_y,
                is_thin_pillar = false
        );
    }

    legends = ["Z", "A", "'", "1"];
    for (y = [-1 : 2]) {
        case_x = key_pitch * -1;
        case_y = key_pitch * (y + 2);
        translate([-keycap_half_width, case_y]) keycap_with_stem(
                -1, y, case_x, case_y, legend = legends[y + 1],
                right_wall_angle = -column_angle / 2,
                wall_y = case_y,
                is_thin_pillar = false
        );
    }
}

// x = 0
translate([
    keycap_half_width * cos(column_angle * 0),
    keycap_half_width * sin(column_angle * 0)
]) rotate([0, 0, column_angle * 0]) {
    s = 8;
    legends = ["Q", "O", ",", "2"];
    for (y = [-1 : 2]) {
        case_x = key_pitch * 0;
        case_y = key_pitch * (y + 2) + s;
        translate([-keycap_half_width, case_y]) keycap_with_stem(
                0, y, case_x, case_y, legend = legends[y + 1],
                left_wall_angle  =  column_angle / 2,
                right_wall_angle = -column_angle / 2,
                wall_y = case_y,
                is_thin_pillar = false
        );
    }
}

// x = 1
translate([
    keycap_half_width * cos(column_angle * 0) + keycap_margin * 2 * cos(column_angle * -1),
    keycap_half_width * sin(column_angle * 0) + keycap_margin * 2 * sin(column_angle * -1)
]) rotate([0, 0, column_angle * -1]) {
    s = 12;
    legends = ["J", "E", ".", "3"];
    for (y = [-1 : 2]) {
        case_x = key_pitch * 1;
        case_y = key_pitch * (y + 2) + s;
        translate([keycap_half_width, case_y]) keycap_with_stem(
                1, y, case_x, case_y,
                legend = legends[y + 1],
                left_wall_angle  =  column_angle / 2,
                right_wall_angle = -column_angle / 2,
                wall_y = case_y,
                is_thin_pillar = false
        );
    }
}

// x = 2
translate([
    keycap_half_width * cos(column_angle * 0) + key_pitch * cos(column_angle * -1) + keycap_margin * 2 * cos(column_angle * -2),
    keycap_half_width * sin(column_angle * 0) + key_pitch * sin(column_angle * -1) + keycap_margin * 2 * sin(column_angle * -2)
]) rotate([0, 0, column_angle * -2]) {
    s = 4;
    legends = ["K", "U", "P", "4"];
    for (y = [-1 : 2]) {
        case_x = key_pitch * 2;
        case_y = key_pitch * (y + 2) + s;
        translate([keycap_half_width, case_y]) keycap_with_stem(
                2, y, case_x, case_y,
                legend = legends[y + 1],
                left_wall_angle  =  column_angle / 2,
                right_wall_angle = -column_angle / 2,
                wall_y = case_y,
                is_fluent_to_north = (y == 0),
                is_fluent_to_south = (y == 1),
                is_home_position = (y == 0),
                is_thin_pillar = false
        );
    }
}

// x = [3, 4]
translate([
    keycap_half_width * cos(column_angle * 0) + key_pitch * cos(column_angle * -1) + key_pitch * cos(column_angle * -2) + keycap_margin * 2 * cos(column_angle * -3),
    keycap_half_width * sin(column_angle * 0) + key_pitch * sin(column_angle * -1) + key_pitch * sin(column_angle * -2) + keycap_margin * 2 * sin(column_angle * -3)
]) rotate([0, 0, column_angle * -3]) {
    legends = ["X", "I", "Y", "5"];
    for (y = [-1 : 2]) {
        case_x = key_pitch * 3;
        case_y = key_pitch * (y + 2);
        translate([keycap_half_width, case_y]) keycap_with_stem(
                3, y, case_x, case_y,
                legend = legends[y + 1],
                left_wall_angle = column_angle / 2,
                wall_y = case_y,
                is_fluent_to_north = (y == 0),
                is_fluent_to_south = (y == 1),
                is_thin_pillar = true
        );
    }
}

thumb_angle_bias = -61;
translate([16, 16 * 0 - 65]) rotate([0, 0, -thumb_angle_bias]) {
    thumb_keycap(65, 35 + thumb_angle_bias, 52 + thumb_angle_bias, 16);
    thumb_keycap(65, 52 + thumb_angle_bias, 71 + thumb_angle_bias, 16);
    thumb_keycap(65, 71 + thumb_angle_bias, 90 + thumb_angle_bias, 16);
}

/*
translate([16 * -1.625, 16 * -2]) keycap_with_stem(-1.625, -2, case_x = -1.625, case_y = -2, w = 1.75, h = 1);
translate([16 * -0.125, 16 * -2]) keycap_with_stem(-2.000,  1, case_x = -0.125, case_y = -2, w = 1.25, h = 1, is_cylindrical = true);
translate([16 *  1.250, 16 * -2]) keycap_with_stem(-1.250,  1, case_x =  1.250, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  2.750, 16 * -2]) keycap_with_stem( 1.250,  1, case_x =  2.750, case_y = -2, w = 1.50, h = 1, is_cylindrical = true);
translate([16 *  4.000, 16 * -2]) keycap_with_stem( 2.500,  1, case_x =  4.000, case_y = -2, w = 1.00, h = 1, is_cylindrical = true);
*/
