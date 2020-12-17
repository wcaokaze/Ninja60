top_size = 14;
bottom_size = 15;
height = 8;

difference() {
    hull() {
        translate([0, 0, height])
            cube([top_size, top_size, 0.01], center = true);
        cube([bottom_size, bottom_size, 0.01], center = true);
    }

    hull() {
        translate([0, 0, height - 1.5])
            cube([top_size - 3, top_size - 3, 0.01], center = true);
        cube([bottom_size - 3, bottom_size - 3, 0.01], center = true);
    }
}
