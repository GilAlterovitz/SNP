library(illuminaio)
library(lumi)

idat2lumibatch <- function(filenames) {
  # filenames is a character vector of iDAT filenames
  require(illuminaio)
  require(lumi)
  idatlist = lapply(filenames,readIDAT)
  exprs = sapply(idatlist,function(x) {
    return(x$Quants$MeanBinData)})
  se.exprs = sapply(idatlist,function(x) {
    return(x$Quants$DevBinData/sqrt(x$Quants$NumGoodBeadsBinData))})
  beadNum = sapply(idatlist,function(x) {
    return(x$Quants$NumGoodBeadsBinData)})
  rownames(exprs)=rownames(se.exprs)=rownames(beadNum)=idatlist[[1]]$Quants$CodesBinData
  colnames(exprs)=colnames(se.exprs)=colnames(beadNum)=sapply(idatlist,function(x) {
    return(paste(x$Barcode,x$Section,sep="_"))})
  pd = data.frame(Sentrix=colnames(exprs))
  rownames(pd)=colnames(exprs)
  lb = new("LumiBatch",exprs=exprs,se.exprs=se.exprs,beadNum=beadNum,
           phenoData=AnnotatedDataFrame(pd))
  return(lb)
}


# Paths
Args = commandArgs()
path_work = Args[6]
path_raw = file.path(path_work,'raw')
path_samplesheet = file.path(path_work,'samplesheet')
path_expr = file.path = file.path(path_work,'exprdata')
if (!file.exists(path_expr)){
  dir.create(path_expr)
}
print('Paths load: Done.')

# Different Groups
groups = c('blood', 'tissue')
for (group in groups) {
  path_rawdata = file.path(path_raw,group)
  folderlist = list.files(path_rawdata)
  pathlist = c()
  for (folder in folderlist) {
    path_folders = file.path(path_rawdata,folder)
    filelist = list.files(path_folders)
    idatfilelist = filelist[grep('*.idat',filelist)]
    pathlist = c(pathlist,file.path(path_folders,idatfilelist))
  }
  
  # Read idat files and transform them to lumibatch format
  lumidata = idat2lumibatch(pathlist)
  print('Lumidata transform data: Done.')
  
  # Preprocess with lumi
  expr = lumiExpresso(lumidata,varianceStabilize.param=list(method='log2'))
  rm(lumidata)
  print('Preprocess with R: Done.')
  
  exprfilename = paste('exprdata_',group,'.txt',sep='')
  file_expr = file.path(path_expr,exprfilename)
  exprdata = data.frame(exprs(expr))
  illuminaID = rownames(exprdata)
  samplekeys = colnames(exprdata)
  
  IDmap = data.frame(IlluminaID2nuID(illuminaID,species='Mouse'))
  IDmap2 = IDmap[!is.na(IDmap[['Symbol']]),]
  index = match(illuminaID,rownames(IDmap2))
  exprdata = exprdata[illuminaID[!is.na(index)],]
  nuID = as.character(IDmap2[rownames(exprdata),'Symbol'])
  exprdata = data.frame(nuID,exprdata)
  index_dup = duplicated(nuID)
  nuID_dup = nuID[index_dup]
  duplist = nuID_dup[!duplicated(nuID_dup)]
  for (dup in duplist) {
    index = which(!is.na(match(exprdata[[1]],dup)))
    exprdata_dup = exprdata[index,-1]
    colmean = colMeans(exprdata_dup)
    exprdata[index[1],-1] = colmean
    exprdata = exprdata[-index[-1],]
  }
  rownames(exprdata) = exprdata[[1]]
  exprdata = exprdata[,-1]
  #rownames(exprdata) = as.character(IDmap2[rownames(exprdata),'Symbol'])
  print('IDmapping: Done.')
  
  # Read Samplesheet
  samplesheetfilename = paste('PLS_',group,'.csv',sep='')
  file_samplesheet = file.path(path_samplesheet,samplesheetfilename)
  samplesheet = read.csv(file_samplesheet,header=T,as.is=T,skip=7)
  print('Load samplesheet: Done.')
  
  # Map samples
  exprdata_test = exprdata
  samplenames = c()
  un_num = 0
  drug_num = 0
  rad_num = 0
  raddrug_num = 0
  for (key in samplekeys) {
    key_temp = unlist(strsplit(key,'X'))[2]
    key_sep = unlist(strsplit(key_temp,'_'))
    key1 = key_sep[1]
    key2 = key_sep[2]
    index1 = match(as.character(samplesheet[[6]]),key1)
    samplesheet_temp = samplesheet[!is.na(index1),]
    index2 = match(as.character(samplesheet_temp[[7]]),key2)
    if (!is.na(match(1,index2))) {
      sampleinfo = as.character(samplesheet_temp[!is.na(index2),])
      samplenum = as.numeric(unlist(strsplit(sampleinfo[1],' '))[3])
      if (5<samplenum&&samplenum<26) {
        un_num = un_num+1
        groupname = paste('grp1',un_num,sep='.')
        samplenames = c(samplenames, groupname)
      }
      else if (30<samplenum&&samplenum<51) {
        drug_num = drug_num+1
        groupname = paste('grp2',drug_num,sep='.')
        samplenames = c(samplenames, groupname)
      }
      else if (55<samplenum&&samplenum<76) {
        rad_num = rad_num+1
        groupname = paste('grp3',rad_num,sep='.')
        samplenames = c(samplenames, groupname)
      }
      else if (80<samplenum&&samplenum<101) {
        raddrug_num = raddrug_num+1
        groupname = paste('grp4',raddrug_num,sep='.')
        samplenames = c(samplenames, groupname)
      }
      else {
        exprdata=exprdata[,-which(colnames(exprdata)==key)]
      }
    }
    else {
      exprdata=exprdata[,-which(colnames(exprdata)==key)]
    }
  }
  colnames(exprdata) = samplenames
  print('Sample mapping: Done.')
  
  # Output exprdata
  index_un = which(unlist(gregexpr('grp1',colnames(exprdata)))==1)
  index_rad = which(unlist(gregexpr('grp3',colnames(exprdata)))==1)
  index_drug = which(unlist(gregexpr('grp2',colnames(exprdata)))==1)
  index_raddrug = which(unlist(gregexpr('grp4',colnames(exprdata)))==1)
  expr_un_rad = exprdata[,c(index_un,index_rad)]
  expr_un_drug = exprdata[,c(index_un,index_drug)]
  expr_un_raddrug = exprdata[,c(index_un,index_raddrug)]
  expr_rad_raddrug = exprdata[,c(index_rad,index_raddrug)]
  
  exprfilename_un_rad = paste('exprdata_grp1_grp3_',group,'.txt',sep='')
  exprfilename_un_drug = paste('exprdata_grp1_grp2_',group,'.txt',sep='')
  exprfilename_un_raddrug = paste('exprdata_grp1_grp4_',group,'.txt',sep='')
  exprfilename_rad_raddrug = paste('exprdata_grp3_grp4_',group,'.txt',sep='')
  file_expr_un_rad = file.path(path_expr,exprfilename_un_rad)
  file_expr_un_drug = file.path(path_expr,exprfilename_un_drug)
  file_expr_un_raddrug = file.path(path_expr,exprfilename_un_raddrug)
  file_expr_rad_raddrug = file.path(path_expr,exprfilename_rad_raddrug)
    
  write.table(expr_un_rad,file_expr_un_rad,quote=F,sep='\t')
  write.table(expr_un_drug,file_expr_un_drug,quote=F,sep='\t')
  write.table(expr_un_raddrug,file_expr_un_raddrug,quote=F,sep='\t')
  write.table(expr_rad_raddrug,file_expr_rad_raddrug,quote=F,sep='\t')
  write.table(exprdata,file_expr,quote=F,sep='\t')
  print('Output data: Done.')
}
