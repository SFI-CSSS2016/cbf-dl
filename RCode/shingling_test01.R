library(data.table)
#
# read shingles
#
shingles <- fread("../shingled_mutant_CBF.txt", header = T)
shingles_melted <- data.table::melt(shingles[sample(1:nrow(shingles), 1000, replace = F),], id.vars = c("key"))
#
library(dplyr)
library(scales)
shingles_melted$value <- rescale(shingles_melted$value)
#
p <- ggplot(data = shingles_melted, aes(x = key, y = variable,fill = value)) +
  geom_raster()
p

unique(shingles$key)


ones <- shingles[shingles$key == 1,]
str(ones)
mt <- as.matrix(ones[,1:625])
dd <- dist(mt, method = "binary")
str(dd)
clusters <- hclust(dd)
plot(clusters, main = "class One mutants, method='binary'")
