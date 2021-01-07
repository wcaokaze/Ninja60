
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
