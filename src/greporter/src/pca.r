library(affy)
setwd('./data/')
affy.data = ReadAffy()
eset.mas5 = mas5(affy.data)
exprSet.nologs = exprs(eset.mas5)
exprSet = log(exprSet.nologs, 2)
write.table(exprSet, file="Su_mas5_matrix1.txt", quote=F, sep="\t")

