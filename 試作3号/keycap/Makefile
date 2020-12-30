
define keycap
	openscad -o $@ keycap.scad \
		-D "position_x=$1" -D "position_y=$2" \
		-D "rotation_x=$3" -D "rotation_y=$4" -D "rotation_z=$5" \
		-D "is_upper_layer=$6" \
		-D "x=$7" -D "y=$8" -D "w=$9" -D "h=$(10)" \
		-D "is_cylindrical=$(11)" -D "is_home_position=$(12)"
endef

define alphanumeric_key
	$(call keycap, $1, $2, $3, $4, $5, $6, $7, $8, 1, 1, false, false)
endef

all: alphanumeric_keys.stl modifier_keys.stl

alphanumeric_keys.stl: \
	alphanumeric0.stl alphanumeric1.stl alphanumeric2.stl alphanumeric3.stl \
	alphanumeric4.stl alphanumeric5.stl alphanumeric6.stl alphanumeric7.stl
	openscad -o $@ alphanumeric_keys.scad

modifier_keys.stl: \
	modifier0.stl  modifier1.stl  modifier2.stl  modifier3.stl  modifier4.stl \
	modifier5.stl  modifier6.stl  modifier7.stl  modifier8.stl  modifier9.stl \
	modifier10.stl modifier11.stl modifier12.stl modifier13.stl
	openscad -o $@ modifier_keys.scad

alphanumeric0.stl:; $(call alphanumeric_key, 0, 0, 0, 0,   0, false, 0.5, 0.5)
alphanumeric1.stl:; $(call alphanumeric_key, 0, 0, 0, 0, -90,  true, 3.5, 1.5)
alphanumeric2.stl:; $(call alphanumeric_key, 1, 0, 0, 0,   0, false, 1.5, 0.5)
alphanumeric3.stl:; $(call alphanumeric_key, 1, 0, 0, 0, -90,  true, 2.5, 1.5)
alphanumeric4.stl:; $(call alphanumeric_key, 0, 1, 0, 0,   0, false, 2.5, 0.5)
alphanumeric5.stl:; $(call alphanumeric_key, 0, 1, 0, 0, -90,  true, 1.5, 1.5)
alphanumeric6.stl:; $(call alphanumeric_key, 1, 1, 0, 0,   0, false, 3.5, 0.5)
alphanumeric7.stl:; $(call alphanumeric_key, 1, 1, 0, 0, -90,  true, 0.5, 1.5)

# IDE key
modifier0.stl:; $(call keycap, 0.5, 1, 0, 0, 90, false, 4, 0, 1, 2, true, false)

# Home position keys
modifier1.stl:; $(call keycap, 0.0, 1, 0, 0,  90, true, -2, 0, 1, 1, false, true)
modifier2.stl:; $(call keycap, 1.0, 1, 0, 0, -90, true,  2, 0, 1, 1, false, true)

# modified space key
modifier3.stl:; $(call keycap, 2.25, 1, 0, 0, 0, false, 1.25, 1, 1.5, 1, true, false)

# right fn key
modifier4.stl:; $(call keycap, 2.25, 1, 0, 0, 0, true, 1, 1, 1.5, 1, true, false)

# super key
modifier5.stl:; $(call keycap, 3.5, 1, 0, 0, 180, true, 1, 1, 1, 1, true, false)

# secondary IDE key and ESC key
modifier6.stl:; $(call alphanumeric_key, 4.5, 1, 0, 0, 0, false, 4.5, 0.5)
modifier7.stl:; $(call alphanumeric_key, 4.5, 0, 0, 0, 0, false, 4.5, 1.5)

# fn key
modifier8.stl:; $(call keycap, 0.375, 0, 0, 0, 0, false, -1.625, -2, 1.75, 1, false, false)

# alt key
modifier9.stl:; $(call keycap, 1.875, 0, 0, 0, 0, false, -2, 1, 1.25, 1, true, false)

# space key
modifier10.stl:; $(call keycap, 3.25, 0, 0, 0, 0, false, -1.25, 1, 1.5, 1, true, false)

# esc key
modifier11.stl:; $(call keycap, 1, 0, 0, 0, 0, true, 2.5, 1, 1, 1, true, false)

# backspace key
modifier12.stl:; $(call keycap, 2, 0, 0, 0, 0, true, -2, 1, 1, 1, true, false)

# enter key
modifier13.stl:; $(call keycap, 3.25, 0, 0, 0, 0, true, -1, 1, 1.5, 1, true, false)

