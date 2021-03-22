
include <shared.scad>;

encoder_knob_r = 11;
encoder_knob_h = 14;

module encoder_knob() {
    cylinder(r = encoder_knob_r, h = encoder_knob_h);
}
