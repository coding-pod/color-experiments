import sys
import csv
import numpy as np
from mpl_toolkits.mplot3d import Axes3D
import matplotlib.pyplot as plt
import colorsys

color_names = "xkcd-rgb.txt"
cie_data = "cie-std-obs.txt"

# hex to rgb
def hex_to_rgb(value):
	value = value.lstrip('#')
	lv = len(value)
	return np.array([int(value[i:i + lv/3], 16) for i in range(0, lv, lv/3)])

# rgb to xyY
def rgb_to_xyY(rgb):
	m = np.array([[0.49, 0.31, 0.20], [0.17697, 0.81240, 0.01063], [0.0, 0.01, 0.99]])
	XYZ = m.dot(rgb)
	s = np.sum(XYZ)
	if s > 0: xyz = XYZ/np.sum(XYZ)
	else: xyz = np.array([0, 0, 0])
	return np.array([xyz[0], xyz[1], XYZ[1]])

# import file
def import_data(file_name):
	data_map = []
	with open(file_name) as input:
		for line in csv.reader(input, dialect="excel-tab"):
			data_map.append(line)
	return data_map

color_map = import_data(color_names)
cie_map = import_data(cie_data)

n = len(color_map)
rgb = np.zeros(shape=(n, 3))
xyY = np.zeros(shape=(n, 3))
#hls = np.zeros(shape=(n, 3))
c = []

for i in range(n):
	rgb[i] = hex_to_rgb(color_map[i][1])/255.0
	xyY[i] = rgb_to_xyY(rgb[i])
	#hls[i] = np.array(colorsys.rgb_to_hls(rgb[i][0], rgb[i][1], rgb[i][2]))
	c.append(color_map[i][1])

fig = plt.figure()

ax1 = fig.add_subplot(121, projection='3d')
ax1.scatter(xyY[..., 0], xyY[..., 1], xyY[..., 2], c=c, marker='o', s=80)
ax1.set_xlabel('x')
ax1.set_ylabel('y')
ax1.set_zlabel('Y')

n = len(cie_map)
x = np.zeros(shape=(n, 1))
y = np.zeros(shape=(n, 1))

for i in range(n):
	x[i] = cie_map[i][7]
	y[i] = cie_map[i][8]

ax2 = fig.add_subplot(122)
ax2.scatter(xyY[..., 0], xyY[..., 1], c=c, marker='o', s=80)
ax2.plot(x, y, 'k')
ax2.set_xlabel('x')
ax2.set_ylabel('y')
ax2.grid(True)
ax2.axis([0, 0.85, 0, 0.85])

plt.show()

