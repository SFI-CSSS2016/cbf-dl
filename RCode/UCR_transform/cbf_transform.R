library(stringr)
##
conn <- file("../src/resources/data/CBF/CBF_TRAIN", open = "r")
linn <- readLines(conn)
##
ds <- (1:129)
##
for (i in 1:length(linn)) {
  str <- str_trim(linn[i])
  str_split <- unlist(strsplit(str, "\\s+"))
  label <- as.numeric(str_split[1])
  series <- as.numeric(str_split[2:129])
  ds <- rbind(ds, c(series, label))
}
close(conn)
#
write.csv(ds[-1,], "../src/resources/data/CBF/cbf_train_original.csv", col.names = F, row.names = F)
