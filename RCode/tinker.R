library(data.table)
#
dd <- fread("../test01.txt", header = F)
plot(dd$V2, dd$V3, col = "blue")
text(labels = dd$V5, x = dd$V3, y = dd$V4, cex = 0.65, pos = 2)
#

#
dd <- fread("../test02.txt", header = F)
points(dd$V2, dd$V3, col = "red")
text(labels = dd$V5, x = dd$V3, y = dd$V4, cex = 0.65, pos = 2)
#


dd <- fread("../rossler_results_00.txt", header = F)
plot(dd[1:68,]$V2, dd[1:68,]$V3, col = "blue")
text(dd$V5, percent_losses, labels=namebank, cex= 0.3, pos=3)

dd <- fread("../rossler_results_01.txt", header = F)
points(dd[1:68,]$V3, dd[1:68,]$V4, col = "red")

s1 = c(-0.46427649, -0.55504787, -0.8428431, -0.86589548, -0.93639631, -0.81726995, -0.26361216, -1.2580483, -1.2503934, -0.91830825, -0.92210226, -0.98448828, -1.2880511, -1.14346, -1.0488611, -0.36538582, -0.6913952, -0.98055025, -0.99133601, -0.88708673, -1.109881, -0.7687248, -1.0102962, -0.84699606, -1.3277464, -0.7129093, -1.2977275, 1.3314643, 0.9357379, 1.2360468, 1.0560677, 0.91074495, 1.3713479, 0.93705876, 0.91029198, 0.93108664, 1.4937796, 1.3192656, 1.2190887, 0.94836262, 0.16902251, 0.97739879, 1.2301476, 0.57156683, 0.75134091, 0.96185121, 0.7813122, 1.1044295, 1.0003177, 0.5507251, 0.2688748, 1.1255209, 1.0758635, 1.071738, 1.1544819, 0.75940186, 0.1511864, 1.0202768, 1.2572739, 1.007149, 1.1624171, 0.82999734, 1.4850935, 1.1050903, 1.5393173, 1.1156157, 1.8913625, 1.3900586, 1.1566925, 1.5148579, 1.6335918, 0.8102606, 0.67666423, 0.7093935, 1.3614136, 1.3123797, 0.59160823, 0.83192841, 1.0970562, 0.96495583, 0.92592252, 1.236138, 1.1216699, 0.934699, 1.2165038, -1.6066345, -0.59477544, -0.75663256, -0.56605888, -0.64849922, -0.65649255, -0.53225104, -0.11504817, -0.57315237, -0.54118282, -0.70008673, -1.198927, -0.95269931, -0.99828204, -0.82499015, -0.38078029, -0.47659767, -0.60136144, -0.36936332, -1.2990449, -0.91839479, -1.2648127, -0.99193767, -0.80557657, -0.9209508, -0.36623347, -0.34636283, -1.0469164, -0.91466684, -0.91156991, -0.98677519, -1.2189544, -0.52962015, -1.3310856, -1.0837, -1.0179615, -0.91602872, -1.1342892, -0.92022369, -0.7893615, -0.63870583, -0.96366425, -1.2451691)
plot(s1, type="l")

#
#
#
#
#
dd <- fread("../test01.txt", header = F)
plot(dd$V2, dd$V3, col = "red", pch = 20)
names(dd) <- c("time", "x", "y", "z")
#
ee <- fread("../voronoi_edges.txt")
names(ee) <- c("x", "y", "xend", "yend", "p1", "p2")
#
library(ggplot2)
p <- ggplot(data = dd, aes(x = x, y = y)) +
  theme_bw() + geom_point(color = "cornflowerblue") + geom_path(color = "red2") +
  geom_segment(data = ee, aes(x = x, y = y, xend = xend, yend = yend))
p


library(plotly)
p <- plot_ly(dd, x = ~x, y = ~y, z = ~z,
             type = 'scatter3d', mode = 'points',
             line = list(color = 'cornflowerblue', width = 1),
             name = "Rössler attractor")
p



dd$group <- 1
#
dd1 <- fread("../results.txt", header = F)
names(dd1) <- c("t", "x", "y", "z")
p <- plot_ly(dd1, x = ~x, y = ~y, z = ~z,
             type = 'scatter3d', mode = 'points',
             line = list(color = 'red', width = 1),
             name = "Rössler attractor")
p



dd1$group2 <- 2
#
dat <- cbind(dd, dd1)
diff <- dd$V2[1:1024] - dd1$X2[1:1024]

library(plotly)

p <- plot_ly(dat, x = ~V2, y = ~V3, z = 0,
             type = 'scatter3d', mode = 'lines',
             line = list(color = 'red', width = 1)) %>%
  add_trace(x = ~X2, y = ~X3, z = ~X4,
            line = list(color = 'blue', width = 1))
p

data <- read.csv('https://raw.githubusercontent.com/plotly/datasets/master/_3d-line-plot.csv')
