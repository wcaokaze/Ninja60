
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

all: \
	 0.stl  1.stl  2.stl  3.stl  4.stl  5.stl  6.stl  7.stl  8.stl  9.stl \
	10.stl 11.stl 12.stl 13.stl 14.stl 15.stl 16.stl 17.stl 18.stl 19.stl \
	20.stl 21.stl 22.stl 23.stl 24.stl 25.stl 26.stl 27.stl 28.stl 29.stl \
	30.stl 31.stl 32.stl 33.stl 34.stl 35.stl 36.stl 37.stl 38.stl 39.stl \
	40.stl 41.stl 42.stl 43.stl 44.stl 45.stl 46.stl 47.stl 48.stl 49.stl \
	50.stl 51.stl 52.stl 53.stl 54.stl 55.stl 56.stl 57.stl 58.stl 59.stl \
	60.stl 61.stl 62.stl 63.stl 64.stl 65.stl 66.stl 67.stl 68.stl 69.stl \
	70.stl 71.stl 72.stl
	openscad -o Ninja60.stl Ninja60.scad

0.stl:; $(call keycap, 0, 1.5, 0, 0,   0, false,  4, 0, 1, 2,  true, false)
1.stl:; $(call keycap, 0, 1.0, 0, 0,   0,  true, -2, 0, 1, 1, false,  true)
2.stl:; $(call keycap, 0, 2.0, 0, 0, 180,  true,  2, 0, 1, 1, false,  true)

3.stl:; $(call alphanumeric_key, 1, 1, 0, 0, 0, false, 0,  0)
4.stl:; $(call alphanumeric_key, 1, 1, 0, 0, 0,  true, 0,  2)
5.stl:; $(call alphanumeric_key, 1, 2, 0, 0, 0, false, 0,  1)
6.stl:; $(call alphanumeric_key, 1, 2, 0, 0, 0,  true, 0, -1)

7.stl:;  $(call alphanumeric_key, 2, 1, 0, 0,   0, false, -1,  1)
8.stl:;  $(call alphanumeric_key, 2, 1, 0, 0,  90, true,  -3,  2)
9.stl:;  $(call alphanumeric_key, 3, 1, 0, 0,   0, false, -1,  2)
10.stl:; $(call alphanumeric_key, 3, 1, 0, 0,  90, true,  -2,  2)
11.stl:; $(call alphanumeric_key, 4, 1, 0, 0,   0, false, -2,  1)
12.stl:; $(call alphanumeric_key, 4, 1, 0, 0,  90, true,  -3,  1)
13.stl:; $(call alphanumeric_key, 2, 2, 0, 0,   0, false, -1,  0)
14.stl:; $(call alphanumeric_key, 2, 2, 0, 0,  90, true,  -3, -1)
15.stl:; $(call alphanumeric_key, 3, 2, 0, 0,   0, false, -1, -1)
16.stl:; $(call alphanumeric_key, 3, 2, 0, 0,  90, true,  -2, -1)
17.stl:; $(call alphanumeric_key, 4, 2, 0, 0,   0, false, -2,  0)
18.stl:; $(call alphanumeric_key, 4, 2, 0, 0,  90, true,  -3,  0)

19.stl:; $(call alphanumeric_key, 5, 1, 0, 0,   0, false, 1,  1)
20.stl:; $(call alphanumeric_key, 5, 1, 0, 0, -90, true,  3,  2)
21.stl:; $(call alphanumeric_key, 6, 1, 0, 0,   0, false, 1,  2)
22.stl:; $(call alphanumeric_key, 6, 1, 0, 0, -90, true,  2,  2)
23.stl:; $(call alphanumeric_key, 7, 1, 0, 0,   0, false, 2,  1)
24.stl:; $(call alphanumeric_key, 7, 1, 0, 0, -90, true,  3,  1)
25.stl:; $(call alphanumeric_key, 5, 2, 0, 0,   0, false, 1,  0)
26.stl:; $(call alphanumeric_key, 5, 2, 0, 0, -90, true,  3, -1)
27.stl:; $(call alphanumeric_key, 6, 2, 0, 0,   0, false, 1, -1)
28.stl:; $(call alphanumeric_key, 6, 2, 0, 0, -90, true,  2, -1)
29.stl:; $(call alphanumeric_key, 7, 2, 0, 0,   0, false, 2,  0)
30.stl:; $(call alphanumeric_key, 7, 2, 0, 0, -90, true,  3,  0)

31.stl:; $(call keycap, 0, 3.5, 0, 0,   0, false,  4, 0, 1, 2,  true, false)
32.stl:; $(call keycap, 0, 3.0, 0, 0,   0,  true, -2, 0, 1, 1, false,  true)
33.stl:; $(call keycap, 0, 4.0, 0, 0, 180,  true,  2, 0, 1, 1, false,  true)

34.stl:; $(call alphanumeric_key, 1, 3, 0, 0, 0, false, 0,  0)
35.stl:; $(call alphanumeric_key, 1, 3, 0, 0, 0,  true, 0,  2)
36.stl:; $(call alphanumeric_key, 1, 4, 0, 0, 0, false, 0,  1)
37.stl:; $(call alphanumeric_key, 1, 4, 0, 0, 0,  true, 0, -1)

38.stl:; $(call alphanumeric_key, 2, 3, 0, 0,   0, false, -1,  1)
39.stl:; $(call alphanumeric_key, 2, 3, 0, 0,  90, true,  -3,  2)
40.stl:; $(call alphanumeric_key, 3, 3, 0, 0,   0, false, -1,  2)
41.stl:; $(call alphanumeric_key, 3, 3, 0, 0,  90, true,  -2,  2)
42.stl:; $(call alphanumeric_key, 4, 3, 0, 0,   0, false, -2,  1)
43.stl:; $(call alphanumeric_key, 4, 3, 0, 0,  90, true,  -3,  1)
44.stl:; $(call alphanumeric_key, 2, 4, 0, 0,   0, false, -1,  0)
45.stl:; $(call alphanumeric_key, 2, 4, 0, 0,  90, true,  -3, -1)
46.stl:; $(call alphanumeric_key, 3, 4, 0, 0,   0, false, -1, -1)
47.stl:; $(call alphanumeric_key, 3, 4, 0, 0,  90, true,  -2, -1)
48.stl:; $(call alphanumeric_key, 4, 4, 0, 0,   0, false, -2,  0)
49.stl:; $(call alphanumeric_key, 4, 4, 0, 0,  90, true,  -3,  0)

50.stl:; $(call alphanumeric_key, 5, 3, 0, 0,   0, false, 1,  1)
51.stl:; $(call alphanumeric_key, 5, 3, 0, 0, -90, true,  3,  2)
52.stl:; $(call alphanumeric_key, 6, 3, 0, 0,   0, false, 1,  2)
53.stl:; $(call alphanumeric_key, 6, 3, 0, 0, -90, true,  2,  2)
54.stl:; $(call alphanumeric_key, 7, 3, 0, 0,   0, false, 2,  1)
55.stl:; $(call alphanumeric_key, 7, 3, 0, 0, -90, true,  3,  1)
56.stl:; $(call alphanumeric_key, 5, 4, 0, 0,   0, false, 1,  0)
57.stl:; $(call alphanumeric_key, 5, 4, 0, 0, -90, true,  3, -1)
58.stl:; $(call alphanumeric_key, 6, 4, 0, 0,   0, false, 1, -1)
59.stl:; $(call alphanumeric_key, 6, 4, 0, 0, -90, true,  2, -1)
60.stl:; $(call alphanumeric_key, 7, 4, 0, 0,   0, false, 2,  0)
61.stl:; $(call alphanumeric_key, 7, 4, 0, 0, -90, true,  3,  0)

# fn key
62.stl:; $(call keycap, 0.375, 0, 0, 0, 0, false, -1.625, -2, 1.75, 1, false, false)

# alt key
63.stl:; $(call keycap, 1.875, 0, 0, 0, 0, false, -2, 1, 1.25, 1, true, false)

# space key
64.stl:; $(call keycap, 3.25, 0, 0, 0, 0, false, -1.25, 1, 1.5, 1, true, false)

# modified space key
65.stl:; $(call keycap, 4.75, 0, 0, 0, 0, false, 1.25, 1, 1.5, 1, true, false)

# esc key
66.stl:; $(call keycap, 1, 0, 0, 0, 0, true, 2.5, 1, 1, 1, true, false)

# backspace key
67.stl:; $(call keycap, 2, 0, 0, 0, 0, true, -2, 1, 1, 1, true, false)

# enter key
68.stl:; $(call keycap, 3.25, 0, 0, 0, 0, true, -1, 1, 1.5, 1, true, false)

# right fn key
69.stl:; $(call keycap, 4.75, 0, 0, 0, 0, true, 1, 1, 1.5, 1, true, false)

# super key
70.stl:; $(call keycap, 6, 0, 0, 0, 180, true, 1, 1, 1, 1, true, false)

# secondary IDE key and ESC key
71.stl:; $(call alphanumeric_key, 6, 0, 0, 0, 0, false, 4, 0)
72.stl:; $(call alphanumeric_key, 7, 0, 0, 0, 0, false, 4, 2)
