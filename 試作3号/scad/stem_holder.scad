
include <shared.scad>;

/*
 * キースイッチのステムを差し込む部分。十字の穴が貫通していて、
 * 上からはキーキャップ、下からはキースイッチが挿さるようになっています
 *
 * tightening - 穴が狭くなります。mm単位
 *              ガバガバよりはキツキツの方がマシ。わかりますね？
 */
module stem_holder(tightening = 0) {
    module stem() {
        union() {
            north_south_thickness = 1.05 - tightening;
            east_west_thickness   = 1.25 - tightening;

            translate([0, 0, 15 / 2]) cube([north_south_thickness, 4, 32], center = true);
            translate([0, 0, 15 / 2]) cube([4, east_west_thickness,   32], center = true);
        }
    }

    difference() {
        minkowski() {
            cube([1, 0.01, 0.01], center = true);
            polygon_pyramid(8, 2.97, h = 5.5);
        }

        stem();
    }
}
