//default operators and their parametres
//used for the initial configuration of the GUIProcessor
//reference from the default_segm.js script as the default operators on the created tree

proc.apply('FitLayout.Segm.CollapseAreas', {});
proc.apply('FitLayout.Segm.SortByLines', {});
//proc.apply('FitLayout.Segm.FindLines', {useConsistentStyle: true, maxLineEmSpace: 1.5});
//proc.apply('FitLayout.Segm.MultiLine', {useConsistentStyle: true, maxLineEmSpace: 0.5});
//proc.apply('Ceur.Tag.Class', {});
//proc.apply('FitLayout.Segm.HomogeneousLeaves', {});
proc.apply('FitLayout.Segm.SuperAreas', {depthLimit: 1});

//proc.apply('FitLayout.Tag.Entities', {});
//proc.apply('FitLayout.Tag.Visual', {trainFile: "res:eswc_train.arff", classIndex: 0});
//proc.apply('FitLayout.Tag.Visual', {trainFile: "/tmp/test.arff", classIndex: 0});

//proc.apply('Eswc.Tag.Titles', {});
//proc.apply('Eswc.Tag.Pairs', {});
//proc.apply('Eswc.Tag.All', {});

//proc.apply('FitLayout.Tools.XMLOutput', {filename: "/tmp/out.xml"});
