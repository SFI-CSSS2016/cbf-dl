library(data.table)
#
# read shingles
#
shingles <- fread("../shingled_CBF.txt", header = T)
shingles_melted <- data.table::melt(shingles, id.vars = c("key"))
#
library(dplyr)
library(scales)
nba.m <- ddply(shingles_melted, .(key, variable), transform,
                rescale = rescale(value))
#
p <- ggplot(data = shingles_melted, aes(x = key, y = variable)) +
  geom_tile()
p
