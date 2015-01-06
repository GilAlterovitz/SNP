library("affy")
#library(mogene20stcdf)

#----------------------------------
Args <- commandArgs()
workpath <- Args[6]
array <- Argv[7]
fp_probeinfo <- Argv[8]
groupnum = argv[9]
# Set paths
CELdir <- paste(workpath, 'CELfiles', sep='/')

path_exprData <- paste(workpath, 'exprdata', sep='/')
path_annlib <- paste(workpath, 'annlib', sep='/')

# Full file paths
# fp_probeinfo <- paste(path_annlib, 'Mouse430_2.na33.annot.csv', sep='/')
fp_groupmap <- paste(path_annlib, 'groupmap.txt', sep='/')

# Read CEL files & Perform RMA normalization
#eset.rma <- justRMA(celfile.path=CELdir, verbose=TRUE,cdfname='Mouse430_2')
eset.rma <- justRMA(celfile.path=CELdir, verbose=TRUE,cdfname=array)
expr = exprs(eset.rma)

# Reset column names
col_raw <- colnames(expr)
col = c()
for (i in c(1:length(col_raw))){
  temp = strsplit(col_raw[i],'.',fixed=T)[[1]][1]
  col[i] = substr(temp,1,3)
}
groupmap = read.table(fp_groupmap, sep='\t', header=T)
match(groupmap[[1]],col)->index
expr=data.frame(expr[,index])
for (i in c(1:groupnum)){
  index = which(groupmap[,2]==i)
  k=1
  for (j in index){
    col[j] = paste('grp', i, '.', k, sep='')
    k = k+1
  }
}
colnames(expr)<-col

# Map the probes
probeinfo = read.table(fp_probeinfo, header=T, sep=',')
genesymbol = as.character(probeinfo[['Gene.Symbol']])
for (i in c(1:length(genesymbol))){
  genesymbol[i] = strsplit(genesymbol[i],' /// ')[[1]][1]
}
expr = expr[which(genesymbol!='---'),]
genesymbol = genesymbol[which(genesymbol!='---')]
index = duplicated(genesymbol)
for (symbol in genesymbol[index]){
  index2 = which(genesymbol==symbol)
  colmean = colMeans(expr[index2,])
  for (i in index2){
    expr[i,] = colmean
  }
}
expr = expr[!index,]
genesymbol = genesymbol[!index]
rownames(expr) = genesymbol

expr_out = 
# Devide the expression data
expr_grp1_grp2 = expr[,c(which(groupmap[,2]==1),which(groupmap[,2]==2))]
expr_grp1_grp3 = expr[,c(which(groupmap[,2]==1),which(groupmap[,2]==3))]
expr_grp2_grp3 = expr[,c(which(groupmap[,2]==2),which(groupmap[,2]==3))]

# Write expression data into a text file and saving other components into RData file
exprFilePath2 <- paste(path_exprData, 'expr_grp1_grp2.txt', sep='/')
exprFilePath3 <- paste(path_exprData, 'expr_grp1_grp3.txt', sep='/')
exprFilePath4 <- paste(path_exprData, 'expr_grp2_grp3.txt', sep='/')
write.table(expr, file=exprFilePath, row.names=T, col.names=T, sep='\t', quote=F)
write.table(expr_grp1_grp2, file=exprFilePath2, row.names=T, col.names=T, sep='\t', quote=F)
write.table(expr_grp1_grp3, file=exprFilePath3, row.names=T, col.names=T, sep='\t', quote=F)
write.table(expr_grp2_grp3, file=exprFilePath4, row.names=T, col.names=T, sep='\t', quote=F)