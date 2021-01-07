
include <shared.scad>;

keycap_visible_fa = 15; // 0.6;
keycap_invisible_fa = 8;

key_pitch = 16;
keycap_thickness = 1.5;

keycap_height = 6.93;
dish_r = 20;

tilt_xr = 260;
tilt_yr = 130;

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
 * polishing_margin - ステムの十字部が太くなります。mm単位
 *                    磨きなどする場合に削れる分を想定して指定しましょう
 */
module keycap(x, y, w = 1, h = 1,
              is_cylindrical = false, is_home_position = false,
              bottom_z = 0,
              polishing_margin = 0)
{
    top_w = key_pitch * w - 4;
    top_h = key_pitch * h - 4;
    bottom_w = key_pitch * w - 0.75;
    bottom_h = key_pitch * h - 0.75;

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
                        cylinder(r = dish_r, h = dish_r, center = true, $fa = fa);
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
                    cylinder(r = r, h = 0.001, $fa = keycap_visible_fa);
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
                dish(keycap_height, fa = keycap_visible_fa);
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
                    top_w    - keycap_thickness * 2, top_h    - keycap_thickness * 2,
                    bottom_w - keycap_thickness * 2, bottom_h - keycap_thickness * 2
            );

            dish(keycap_height - keycap_thickness, fa = keycap_invisible_fa);
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
