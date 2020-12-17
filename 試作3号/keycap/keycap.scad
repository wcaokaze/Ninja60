$fs = 0.1;

module keycap() {
    module round_rect(w, h, r) {
        minkowski() {
            cube([w - r * 2, h - r * 2, 0.01], center = true);
            cylinder(r = r, h = 0.001);
        }
    }

    module round_rect_pyramid(top_size, bottom_size, height) {
        hull() {
            translate([0, 0, height]) {
                round_rect(top_size, top_size, 1);
            }

            round_rect(bottom_size, bottom_size, 1);
        }
    }

    difference() {
        round_rect_pyramid(14, 15, 8);
        round_rect_pyramid(11, 12, 6.5);
    }
}

keycap();
