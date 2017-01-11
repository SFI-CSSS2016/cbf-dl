library(data.table)
#
# read shingles
#
shingles <- fread("../shingled_CBF.txt", header = T)
shingles_melted <- data.table::melt(shingles, id.vars = c("key"))
#
library(dplyr)
library(scales)
shingles_melted$value <- rescale(shingles_melted$value)
#
p <- ggplot(data = shingles_melted, aes(x = key, y = variable, color = value, fill = value)) +
  geom_raster(hjust = 0, vjust = 0)
p
