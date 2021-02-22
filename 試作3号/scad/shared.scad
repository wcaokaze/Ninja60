
$fs = 0.1;

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

// aを0から遠ざかるように加算します
function leave_origin(a, d) =
    (a < 0) ?
        a - d
    :
        a + d
    ;

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
