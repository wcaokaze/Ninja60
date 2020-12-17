$fs = 0.1;
$fa = 1;

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

    height = 8;
    dish_r = 25;

    difference() {
        round_rect_pyramid(14, 15, 30);

        translate([0, 0, height + dish_r]) {
            sphere(dish_r);
        }
    }
}

keycap();
