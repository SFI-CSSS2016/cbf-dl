# cbf_figure.R
#
library(data.table)
#
#
dd <- read.table("../src/resources/data/CBF/CBF_TRAIN", header = F)
#
#
library(dplyr)
dd <- arrange(dd, V1)
#
#
library(ggplot2)
p_cylinder <- ggplot(data = data.frame(x = 1:128, y = unlist(dd[dd$V1 == 1, ][1,-1])), aes(x, y)) + geom_line() +
  theme_bw() + ggtitle("Cylinder") + theme(axis.title = element_blank(), panel.grid.minor = element_blank() )
p_cylinder
p_bell <- ggplot(data = data.frame(x = 1:128, y = unlist(dd[dd$V1 == 2, ][1,-1])), aes(x, y)) + geom_line() +
  theme_bw() + ggtitle("Bell") + theme(axis.title = element_blank(), panel.grid.minor = element_blank() )
p_bell
p_funnel <- ggplot(data = data.frame(x = 1:128, y = unlist(dd[dd$V1 == 3, ][1,-1])), aes(x, y)) + geom_line() +
  theme_bw() + ggtitle("Funnel") + theme(axis.title = element_blank(), panel.grid.minor = element_blank() )
p_funnel
#
#
shingles <- read.table("../src/resources/data/CBF/CBF_TRAIN_shingled.txt", sep = ",", header = T)
shingles_melted <- data.table::melt(shingles[,c(c(319:325,430:435,465:470), length(shingles[1,]))], id.vars = c("key"))
shingles_melted$key <- gsub("^1", "Cylinder", shingles_melted$key)
shingles_melted$key <- gsub("^2", "Bell", shingles_melted$key)
shingles_melted$key <- gsub("^3", "Funnel", shingles_melted$key)
#
library(scales)
shingles_melted$value <- rescale(shingles_melted$value)
#
p_shingles <- ggplot(data = shingles_melted, aes(x = key, y = variable, fill = value)) + theme_bw() +
  geom_tile(aes(fill = value)) + 
  ggtitle("Subset of normalized shingle frequencies for CBF data") + 
  ylab("Shingles") + scale_fill_gradient("Normalized frequency  ", low = "white", high = "black") +
  theme(axis.text.x = element_text(angle = 65, hjust = 1), legend.position = "bottom",
        axis.title.x = element_blank()) 
p_shingles
#
#
require(grid)
require(gridExtra)
#
grid.arrange(p_cylinder, p_bell, p_funnel, p_shingles, layout_matrix = rbind(c(1, 2, 3), c(4)),
             heights = c(0.3, 0.7))
library("Cairo")
Cairo(width = 900, height = 800, 
      file = "ShingledCBF.pdf", 
      type = "pdf", pointsize = 12, 
      bg = "transparent", canvas = "white", units = "px", dpi = 82)
print(grid.arrange(p_bell, p_cylinder, p_funnel, p_shingles, layout_matrix = rbind(c(1, 2, 3), c(4)),
                   heights = c(0.3, 0.7)))
dev.off()
