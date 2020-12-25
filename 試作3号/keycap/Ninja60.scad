
module layout(x, y, rotation = [0, 0], is_upper_layer = false, file_name) {
    key_distance = 16.5;
    upper_layer_z_offset = 28.5;

    translate_x = (x + 0.5) * key_distance;
    translate_y = (y + 0.5) * key_distance;
    translate_z = is_upper_layer ? upper_layer_z_offset : 0;

    rotation_x = rotation[0] + (is_upper_layer ? 180 : 0);
    rotation_y = rotation[1];
    rotation_z = rotation[2];

    translate([translate_x, translate_y, translate_z]) {
        rotate([rotation_x, rotation_y, rotation_z]) {
            import(file_name);
        }
    }
}

// alphanumeric keys
for (y = [1, 3]) {
    layout(0, 0.5 + y, [0, 0,   0], false, "ide_key.stl");
    layout(0,       y, [0, 0,   0], true,  "home_position-2_0.stl");
    layout(0, 1.0 + y, [0, 0, 180], true,  "home_position2_0.stl");
    layout(1,       y, [0, 0,   0], false, "keycap0_0.stl");
    layout(1,       y, [0, 0,   0], true,  "keycap0_2.stl");
    layout(1, 1.0 + y, [0, 0,   0], false, "keycap0_1.stl");
    layout(1, 1.0 + y, [0, 0,   0], true,  "keycap0_-1.stl");

    layout(2,     y, [0, 0,   0], false, "keycap-1_1.stl");
    layout(2,     y, [0, 0,  90], true,  "keycap-3_2.stl");
    layout(3,     y, [0, 0,   0], false, "keycap-1_2.stl");
    layout(3,     y, [0, 0,  90], true,  "keycap-2_2.stl");
    layout(4,     y, [0, 0,   0], false, "keycap-2_1.stl");
    layout(4,     y, [0, 0,  90], true,  "keycap-3_1.stl");
    layout(2, 1 + y, [0, 0,   0], false, "keycap-1_0.stl");
    layout(2, 1 + y, [0, 0,  90], true,  "keycap-3_-1.stl");
    layout(3, 1 + y, [0, 0,   0], false, "keycap-1_-1.stl");
    layout(3, 1 + y, [0, 0,  90], true,  "keycap-2_-1.stl");
    layout(4, 1 + y, [0, 0,   0], false, "keycap-2_0.stl");
    layout(4, 1 + y, [0, 0,  90], true,  "keycap-3_0.stl");

    layout(5,     y, [0, 0,   0], false, "keycap1_1.stl");
    layout(5,     y, [0, 0, -90], true,  "keycap3_2.stl");
    layout(6,     y, [0, 0,   0], false, "keycap1_2.stl");
    layout(6,     y, [0, 0, -90], true,  "keycap2_2.stl");
    layout(7,     y, [0, 0,   0], false, "keycap2_1.stl");
    layout(7,     y, [0, 0, -90], true,  "keycap3_1.stl");
    layout(5, 1 + y, [0, 0,   0], false, "keycap1_0.stl");
    layout(5, 1 + y, [0, 0, -90], true,  "keycap3_-1.stl");
    layout(6, 1 + y, [0, 0,   0], false, "keycap1_-1.stl");
    layout(6, 1 + y, [0, 0, -90], true,  "keycap2_-1.stl");
    layout(7, 1 + y, [0, 0,   0], false, "keycap2_0.stl");
    layout(7, 1 + y, [0, 0, -90], true,  "keycap3_0.stl");
}

// modifier keys
layout(0.375, 0, [0, 0,   0], false, "fn_key.stl");
layout(1.875, 0, [0, 0,   0], false, "alt_key.stl");
layout(3.250, 0, [0, 0,   0], false, "space_key.stl");
layout(4.750, 0, [0, 0,   0], false, "modified_space_key.stl");
layout(1.000, 0, [0, 0,   0], true,  "esc_key.stl");
layout(2.000, 0, [0, 0,   0], true,  "backspace_key.stl");
layout(3.250, 0, [0, 0,   0], true,  "enter_key.stl");
layout(4.750, 0, [0, 0,   0], true,  "right_fn_key.stl");
layout(6.000, 0, [0, 0, 180], true,  "super_key.stl");

// secondary IDE key and ESC key
layout(6, 0, [0, 0, 0], false, "keycap4_0.stl");
layout(7, 0, [0, 0, 0], false, "keycap4_2.stl");
