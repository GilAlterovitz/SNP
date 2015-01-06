#
#
#method path_work binpath Rpath
if __name__ == '__main__':
    #import win32com.client as win32
    import os, sys

    #path_work = 'I:/files/Gil project/chinachen/mouse2'

    #method=sys.argv[1]
    #path_work=sys.argv[2]
    #cwd=sys.argv[3]
    #path_lib = sys.argv[3]+"\\lib\\"
    #P_R=sys.argv[4]

    method="B"
    path_work="C:\\Users\\SNP\\Documents\\SNP\\greporter(gene expression)"
    cwd="C:\\Users\\SNP\\Documents\\SNP\\greporter(gene expression)\\src\\py"
    path_lib = cwd+"\\lib\\"
    P_R="C:\\Program Files\\R\\R-3.1.2\\bin"

    sys.path.append(path_lib)
    print path_lib
    #method = raw_input('L for limma/B for badge: ')

    import Labels
    import Path
    labels = Labels.Labels(method)
    path = Path.DataPaths(path_work,labels)
    progpath = Path.ProgramPath(cwd)
    expath = Path.ExternalPath(P_R)
    IOpath = Path.IOPath(path_work)
    fps = Path.AllFilesFP(path, labels)

    #import PreProcess
    #PreProcess.RMA(path, expath, progpath, labels)
    #print "Finish PreProcess"
    
    if method=='L':
        import CallLimma
        CallLimma.Limma(path, expath, progpath, fps, labels)
    else:
        print 'Run Badge please. Input files appear in %s. Please save your file to directory: %s. Set the filenames like: grp1_grp2_selected_unsorted.txt. Then press Enter to continue. ' % (path.exprdata, path.genereport)
        raw_input()

        if method=='L':
            import GetGenes
            GetGenes.getDiff_Limma(fps, labels)
            GetGenes.nuID2enterzID(fps, labels)
        else:
            import GetGenes
            GetGenes.Sort(fps, labels)
            GetGenes.getDiff_Badge(fps, labels)
            GetGenes.nuID2enterzID(fps, labels)


            #import David
            #David.davidCall(fps, labels)

            #import String
            #String.stringCall(path, fps, labels)
            #String.genEdgeList(path, fps, labels)
            #String.genNetworkInput(path, fps, labels)
            #String.genNetwork(path, progpath)
            #String.annoNetwork(path, progpath, fps, labels)

            #import CrossValidation
            #CrossValidation.exprToArff(path, fps, labels)
            #CrossValidation.syncArffFeatures(path, fps, labels)
            #CrossValidation.callWeka(fps, labels)

            import WriteReport
            WriteReport.writeDocReport(path, IOpath, fps, labels)
            WriteReport.writeXlsReport(path, IOpath, fps, labels)