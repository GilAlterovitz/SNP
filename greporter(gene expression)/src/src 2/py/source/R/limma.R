library(limma)

Args <- commandArgs()

pair1 = Args[6]
pair2 = Args[7]
threshold = Args[8]
threshold2 = Args[9]

wpath_expr = Args[10]
wpath_genenm = Args[11]
wpath_genenm_all = Args[12]
wpath_info = Args[13]
wpath_info_all = Args[14]

#======================================
# Read Expr Data
expr = read.table(wpath_expr, header=T, sep="\t")
#phenotable = read.table(wpath_pl, header=T, sep="")
#exprnms = phenotable[,1]
exprnms = colnames(expr)
#phenotable = phenotable[-1]
#rownames(phenotable) = exprnms
genenms = rownames(expr)
num_expr = length(exprnms)
#colnames(expr) = exprnms

# design matrix
design = matrix(data=(0), nrow=num_expr, ncol=2)
colnames(design) = c('Baseline', 'Pheno')
rownames(design) = exprnms
Ano = c()
Bno = c()
count = 1
for (exprnm in unlist(exprnms)) {
  if (grepl(pair2,exprnm)){
    design[exprnm, 'Pheno'] = 1
    Bno = c(Bno, count)
  }
  else {
    design[exprnm, 'Baseline'] = 1
    Ano = c(Ano, count)
  }
  count = count+1
}

# create contrast matrix
cmatrix <- makeContrasts(PhenovsBaseline=Pheno-Baseline, levels=design)

# Fit the matrix
fit = lmFit(expr, design)
fit2 = contrasts.fit(fit, cmatrix)
fit3 = eBayes(fit2)
options(digits=2)
results = topTable(fit3, coef=1, adjust="BH", number=nrow(expr))
selected = results[results[,"P.Value"]<threshold,]
selected2 = selected[1:threshold2,]

# Write Summary Report
FC = as.data.frame(selected[,"logFC"])
logicalFC = as.data.frame(selected[,"logFC"])
expr1 = as.data.frame(selected[,"AveExpr"])
expr2 = as.data.frame(selected[,"AveExpr"])
rownames(expr1) = selected[,"ID"]
colnames(expr1) = "Expr1"
rownames(expr2) = selected[,"ID"]
colnames(expr2) = "Expr2"
colnames(FC) = "Fold Change"
rownames(FC) = selected[,"ID"]
colnames(logicalFC) = "Fold Change(log2 level)"
rownames(logicalFC) = selected[,"ID"]
#Compute Ave. Expr under control condition
for(no in rownames(selected)){
  gene = selected[no, "ID"]
  Avals = unlist(expr[gene, Ano])
  Aval = mean(Avals)
  Bvals = unlist(expr[gene, Bno])
  Bval = mean(Bvals)
  expr1[gene, 'Expr1'] = Aval
  expr2[gene, 'Expr2'] = Bval
  logFC = selected[no,"logFC"]
  logicalFC[gene,"Fold Change(log2 level)"] = 2^logFC
  FC[gene,"Fold Change"] = Aval^(2^logFC-1)
}
summtab = data.frame(selected[,"ID"], FC, logicalFC, expr1, expr2, selected[,"t"], selected[,"P.Value"], selected[,"adj.P.Val"], selected[,"B"])
colnames(summtab) = c("ID", "Fold Change", "Fold Change(log2 level)", "Expr1", "Expr2", "t", "P.Value", "adj.P.Val", "B" )
write.table(summtab, file=wpath_info_all, quote=F, row.names=F, col.names=T, sep="\t")
# Write gene names
write.table(selected[,"ID"], file=wpath_genenm_all, quote=F, row.names=F, col.names=F, sep="\t")

# Write Summary Report
FC = as.data.frame(selected2[,"logFC"])
logicalFC = as.data.frame(selected2[,"logFC"])
expr1 = as.data.frame(selected2[,"AveExpr"])
expr2 = as.data.frame(selected2[,"AveExpr"])
rownames(expr1) = selected2[,"ID"]
colnames(expr1) = "Expr1"
rownames(expr2) = selected2[,"ID"]
colnames(expr2) = "Expr2"
colnames(FC) = "Fold Change"
rownames(FC) = selected2[,"ID"]
colnames(logicalFC) = "Fold Change(log2 level)"
rownames(logicalFC) = selected2[,"ID"]
#Compute Ave. Expr under control condition
for(no in rownames(selected2)){
  gene = selected2[no, "ID"]
  Avals = unlist(expr[gene, Ano])
  Aval = mean(Avals)
  Bvals = unlist(expr[gene, Bno])
  Bval = mean(Bvals)
  expr1[gene, 'Expr1'] = Aval
  expr2[gene, 'Expr2'] = Bval
  logFC = selected2[no,"logFC"]
  logicalFC[gene,"Fold Change(log2 level)"] = 2^logFC
  FC[gene,"Fold Change"] = Aval^(2^logFC-1)
}
summtab = data.frame(selected2[,"ID"], FC, logicalFC, expr1, expr2, selected2[,"t"], selected2[,"P.Value"], selected2[,"adj.P.Val"], selected2[,"B"])
colnames(summtab) = c("ID", "Fold Change", "Fold Change(log2 level)", "Expr1", "Expr2", "t", "P.Value", "adj.P.Val", "B" )
write.table(summtab, file=wpath_info, quote=F, row.names=F, col.names=T, sep="\t")
# Write gene names
write.table(selected2[,"ID"], file=wpath_genenm, quote=F, row.names=F, col.names=F, sep="\t")
