
include <shared.scad>;
include <keycap.scad>;
include <case.scad>;
include <stem_holder.scad>;
include <o_ring.scad>;
include <encoder_knob.scad>;

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
            close_origin(case_x, key_pitch_h * 0.5),
            case_y - key_pitch_v * 0.5
        ),
        left_wall_angle  = left_wall_angle,
        right_wall_angle = right_wall_angle,
        wall_y = wall_y
    ) {
        translate([-case_x, -case_y]) case_curve();
    }

    translate([0, 0, -3]) %stem_holder();
    translate([0, 0, 0.5]) %o_ring();
}

keycap_half_width = key_pitch_h / 2 - keycap_margin;

// x = -2
translate([
    -keycap_half_width * cos(0) - key_pitch_h * cos(3) - keycap_margin * 2 * cos(3),
    -keycap_half_width * sin(0) - key_pitch_h * sin(3) - keycap_margin * 2 * sin(3)
]) rotate([0, 0, 3]) {
    s = -4;
    for (y = [-1 : 2]) {
        case_x = key_pitch_h * -2;
        case_y = key_pitch_v * (y + 2) + s;
        translate([-keycap_half_width, case_y]) keycap_with_stem(
                -2, y, case_x, case_y,
                is_thin_pillar = false
        );
    }
}

// x = -1
translate([
    -keycap_half_width * cos(0) - keycap_margin * 2 * cos(3),
    -keycap_half_width * sin(0) - keycap_margin * 2 * sin(3)
]) rotate([0, 0, 3]) {
    s = -1;
    legends = ["Z", "A", "'", "1"];
    for (y = [-1 : 2]) {
        case_x = key_pitch_h * -1;
        case_y = key_pitch_v * (y + 2) + s;
        translate([-keycap_half_width, case_y]) keycap_with_stem(
                -1, y, case_x, case_y, legend = legends[y + 1],
                right_wall_angle = -1.5,
                wall_y = case_y,
                is_thin_pillar = false
        );
    }
}

// x = 0
translate([
    keycap_half_width * cos(0),
    keycap_half_width * sin(0)
]) rotate([0, 0, 0]) {
    s = 9.5;
    legends = ["Q", "O", ",", "2"];
    for (y = [-1 : 2]) {
        case_x = key_pitch_h * 0;
        case_y = key_pitch_v * (y + 2) + s;
        translate([-keycap_half_width, case_y]) keycap_with_stem(
                0, y, case_x, case_y, legend = legends[y + 1],
                left_wall_angle  =  1.5,
                right_wall_angle = -1,
                wall_y = case_y,
                is_thin_pillar = false
        );
    }
}

// x = 1
translate([
    keycap_half_width * cos(0) + keycap_margin * 2 * cos(-2),
    keycap_half_width * sin(0) + keycap_margin * 2 * sin(-2)
]) rotate([0, 0, -2]) {
    s = 14.25;
    legends = ["J", "E", ".", "3"];
    for (y = [-1 : 2]) {
        case_x = key_pitch_h * 1;
        case_y = key_pitch_v * (y + 2) + s;
        translate([keycap_half_width, case_y]) keycap_with_stem(
                1, y, case_x, case_y,
                legend = legends[y + 1],
                left_wall_angle = 1,
                wall_y = case_y,
                is_thin_pillar = false
        );
    }
}

// x = 2
translate([
    keycap_half_width * cos(0) + key_pitch_h * cos(-2) + 2 + keycap_margin * 2 * cos(-4),
    keycap_half_width * sin(0) + key_pitch_h * sin(-2)     + keycap_margin * 2 * sin(-4)
]) rotate([0, 0, -6]) {
    s = 4;
    legends = ["K", "U", "P", "4"];
    for (y = [-1 : 2]) {
        case_x = key_pitch_h * 2;
        case_y = key_pitch_v * (y + 2) + s;
        translate([keycap_half_width, case_y]) keycap_with_stem(
                2, y, case_x, case_y,
                legend = legends[y + 1],
                wall_y = case_y,
                is_fluent_to_north = (y == 0),
                is_fluent_to_south = (y == 1),
                is_home_position = (y == 0),
                is_thin_pillar = false
        );
    }
}

// x = 3
translate([
    keycap_half_width * cos(0) + key_pitch_h * cos(-2) + 2 + key_pitch_h * cos(-4) + keycap_margin * 2 * cos(-4),
    keycap_half_width * sin(0) + key_pitch_h * sin(-2)     + key_pitch_h * sin(-4) + keycap_margin * 2 * sin(-4)
]) rotate([0, 0, -6]) {
    legends = ["X", "I", "Y", "5"];
    for (y = [-1 : 2]) {
        case_x = key_pitch_h * 3;
        case_y = key_pitch_v * (y + 2);
        translate([keycap_half_width, case_y]) keycap_with_stem(
                3, y, case_x, case_y,
                legend = legends[y + 1],
                wall_y = case_y,
                is_fluent_to_north = (y == 0),
                is_fluent_to_south = (y == 1),
                is_thin_pillar = true
        );
    }
}

translate([14, 7]) encoder_knob();

translate([16, -63]) rotate([0, 0, 59]) {
    rotate([0, 0, 15 * -2]) translate([65, 0]) thumb_keycap(65, -7.5, 7.5, h = 16, dish_offset = 0);
    rotate([0, 0, 15 * -1]) translate([65, 0]) thumb_keycap(65, -7.5, 7.5, h = 16, dish_offset = 0);
    rotate([0, 0, 15 *  0]) translate([65, 0]) thumb_keycap(65, -7.5, 7.5, h = 16, dish_offset = -1);
    rotate([0, 0, 15 *  1]) translate([65, 0]) thumb_keycap(65, -7.5, 7.5, h = 16, dish_offset = -2);
}
