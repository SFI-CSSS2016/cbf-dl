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
p <- ggplot(data = d_orig_mapping, aes(x = x, y = y, label = l)) +
  theme_bw() + geom_point(color = "cornflowerblue") + geom_path(color = "cornflowerblue") +
  geom_text(check_overlap = TRUE, nudge_y = -0.05, nudge_x = 0.05, color = "cornflowerblue") + 
  geom_segment(data = d_tesselation, aes(x = x, y = y, xend = xend, yend = yend), inherit.aes = F) +
  geom_point(data = m_mapping, color = "brown4") +
  geom_path(data = m_mapping, color = "brown4") +
  geom_text(data = m_mapping, check_overlap = TRUE, nudge_y = -0.05, nudge_x = 0.05, color = "brown4") +
  geom_point(data = m_mapping[273:277, ], color = "red")
p
