
all: \
	keycap-3_-1.stl keycap-3_0.stl keycap-3_1.stl keycap-3_2.stl \
	keycap-2_-1.stl keycap-2_0.stl keycap-2_1.stl keycap-2_2.stl \
	keycap-1_-1.stl keycap-1_0.stl keycap-1_1.stl keycap-1_2.stl \
	keycap0_-1.stl  keycap0_0.stl  keycap0_1.stl  keycap0_2.stl \
	keycap1_-1.stl  keycap1_0.stl  keycap1_1.stl  keycap1_2.stl \
	keycap2_-1.stl  keycap2_0.stl  keycap2_1.stl  keycap2_2.stl \
	keycap3_-1.stl  keycap3_0.stl  keycap3_1.stl  keycap3_2.stl \
	                keycap4_0.stl                 keycap4_2.stl \
	home_position-2_0.stl home_position2_0.stl ide_key.stl \
	fn_key.stl alt_key.stl space_key.stl modified_space_key.stl esc_key.stl \
	backspace_key.stl enter_key.stl right_fn_key.stl super_key.stl
	openscad -o Ninja60.stl Ninja60.scad

keycap%.stl:	keycap.scad
	openscad -o $@ keycap.scad \
		-D "x=${shell echo $* | cut -d'_' -f1}" \
		-D "y=${shell echo $* | cut -d'_' -f2}" \
		-D "w=1" -D "h=1" -D "is_cylindrical=false" -D "is_home_position=false"

home_position%.stl:	keycap.scad
	openscad -o $@ keycap.scad \
		-D "x=${shell echo $* | cut -d'_' -f1}" \
		-D "y=${shell echo $* | cut -d'_' -f2}" \
		-D "w=1" -D "h=1" -D "is_cylindrical=false" -D "is_home_position=true"

ide_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=4" -D "y=0" -D "w=1" -D "h=2" \
		-D "is_cylindrical=true" -D "is_home_position=false"

fn_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=-1.625" -D "y=-2" -D "w=1.75" -D "h=1" \
		-D "is_cylindrical=false" -D "is_home_position=false"

alt_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=-2" -D "y=1" -D "w=1.25" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

space_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=-1.25" -D "y=1" -D "w=1.5" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

modified_space_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=1.25" -D "y=1" -D "w=1.5" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

esc_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=2.5" -D "y=1" -D "w=1" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

backspace_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=-2" -D "y=1" -D "w=1" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

enter_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=-1" -D "y=1" -D "w=1.5" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

right_fn_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=1" -D "y=1" -D "w=1.5" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

super_key.stl:	keycap.scad
	openscad -o $@ keycap.scad -D "x=1" -D "y=1" -D "w=1" -D "h=1" \
		-D "is_cylindrical=true" -D "is_home_position=false"

