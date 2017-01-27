library(data.table)
library(ggplot2)
#
# construct the datasets 
#
d_series <- fread("data/e01_original_curve.txt", header = F)
d_string <- fread("data/e01_string.txt", header = F)
d_orig_mapping <- cbind(d_series[1:length(d_string$V1), ], d_string)
colnames(d_orig_mapping) <- c("t", "x", "y", "z", "l")
#
d_tesselation <- fread("data/e01_tesselation.txt", header = F)
names(d_tesselation) <- c("x", "y", "xend", "yend", "p1", "p2")
#
m_series <- fread("data/e01_mutated_curve.txt", header = F)
m_string <- fread("data/e01_mutated_string.txt", header = F)
m_mapping <- cbind(m_series[1:length(m_string$V1), ], m_string)
colnames(m_mapping) <- c("t", "x", "y", "z", "l")
#
p_curve <- ggplot(data = d_orig_mapping, aes(x = x, y = y, label = l)) +
  theme_bw() + geom_point(color = "dodgerblue1") + geom_path(color = "dodgerblue1") +
  geom_text(check_overlap = TRUE, nudge_y = -0.1, nudge_x = 0.2, color = "dodgerblue1") + 
  geom_segment(data = d_tesselation, aes(x = x, y = y, xend = xend, yend = yend), color = "dodgerblue1", inherit.aes = F) +
  geom_point(data = m_mapping, color = "firebrick4", shape = 6) +
  geom_path(data = m_mapping, color = "firebrick4") +
  geom_text(data = m_mapping, check_overlap = TRUE, nudge_y = 0.1, nudge_x = -0.2, color = "firebrick4") +
  geom_point(data = m_mapping[which(d_string$V1 != m_string$V1), ], color = "red", shape = 8, size = 3) +
  ggtitle("An example of the Cylinder discretization warping with chaotic Rossler attractor") +
  theme( panel.grid = element_blank(), axis.title = element_blank(), axis.text = element_blank(),
         axis.ticks = element_blank(), panel.border = element_blank() )
p_curve
#
#
library(scales)

#
do <- as.matrix(fread("data/e01_bitmap.txt"))
rownames(do) <- paste("R", 1:25, sep = "")
do_melt <- melt(do)
do_melt$value <- rescale(do_melt$value)
names(do_melt) <- c("x", "y", "value")
jet.colors <- colorRampPalette(c("#00007F", "blue", "#007FFF", "cyan", "#7FFF7F", "yellow", "#FF7F00", "red", "#7F0000"))
p1 <- ggplot(do_melt, aes(x, y, fill = value)) + 
  geom_tile(color = "grey40") + scale_fill_gradientn(colors = jet.colors(7)) + 
  theme_void() + ggtitle("The original curve shingling") + theme(legend.position = "none")
p1
#
dm <- as.matrix(fread("data/e01_mutated_bitmap.txt"))
rownames(dm) <- paste("R", 1:25, sep = "")
dm_melt <- melt(dm)
dm_melt$value <- rescale(dm_melt$value)
names(dm_melt) <- c("x", "y", "value")
p2 <- ggplot(dm_melt, aes(x, y, fill = value)) + 
  geom_tile(color = "grey40") + scale_fill_gradientn(colors = jet.colors(7)) +  
  theme_void() + ggtitle("The warped curve shingling") + theme(legend.position = "none")
p2
#
#
grid.arrange(p_curve, p1, p2, layout_matrix = rbind(c(1), c(2, 3)),
             heights = c(0.7, 0.3))
#
library("Cairo")
Cairo(width = 900, height = 1300, 
      file = "TesselatedCBF.pdf", 
      type = "pdf", pointsize = 12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(grid.arrange(p_curve, p1, p2, layout_matrix = rbind(c(1), c(2, 3)),
                   heights = c(0.6, 0.4)))
dev.off()
