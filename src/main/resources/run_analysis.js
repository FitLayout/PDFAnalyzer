/*
  The main PDFAnalyzer configuration script
*/

//render the page
var srcConfig = {
		width: 2400,
		height: 800
};
srcConfig.url = app.inputUrl;
proc.renderPage('FitLayout.CSSBox', srcConfig);

//segmentation
proc.initAreaTree('FitLayout.Grouping', {});

//apply operators
proc.apply('FitLayout.Segm.CollapseAreas', {});
proc.apply('FitLayout.Segm.SortByLines', {});
//proc.apply('FitLayout.Segm.FindLines', {useConsistentStyle: true, maxLineEmSpace: 1.5});
//proc.apply('FitLayout.Segm.MultiLine', {useConsistentStyle: true, maxLineEmSpace: 0.5});
//proc.apply('Ceur.Tag.Class', {});
//proc.apply('FitLayout.Segm.HomogeneousLeaves', {});
//proc.apply('FitLayout.Segm.SuperAreas', {depthLimit: 1});
proc.apply('FitLayout.Pdf.NormalizeSeparators', { });
proc.apply('FitLayout.Pdf.SeparatorPairs', { });
proc.apply('FitLayout.Segm.SortByLines', {});
proc.apply('FitLayout.Segm.FindLines', {useConsistentStyle: true, maxLineEmSpace: 1.5});

proc.apply('FitLayout.Pdf.RemapSeparators', { maxEmDistX: 0.5, maxEmDistY: 0.3 });
proc.apply('FitLayout.Logical.LayoutSplit', {});

proc.apply('FitLayout.Pdf.HTMLOutput', {filename: app.outputFile, produceHeader: true});
