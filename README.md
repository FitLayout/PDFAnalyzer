PDF Analsis Tools
=================
(c) 2015 Radek Burget (burgetr@fit.vutbr.cz)

PDF Analysis Tools provide a set of utilities for advanced PDF document analysis. Unlike
the existing PDF to HTML convertors such as [PDFToHTML](http://cssbox.sourceforge.net/pdf2dom/documentation.php),
that focus on obtaining a DOM or HTML representation of the document that is *visually* as close as possible
to the original document, the goal of the PDF Analysis Tools is to produce an output document that has
the same *logical* stucture.

For this purpose, the tools implement different algorithms for detect ingcommon graphical patterns
in the source PDF document that can be represented by some standard HTML elements and CSS constructions.
The most important of them include:

- Sorting the text elements based on their positions.
- Heading and paragraph detection.
- Mapping the graphical separators (different kinds of vertical and horizontal lines in the input PDF)
to CSS borders of the corresponding HTML elements. 

The resulting document may not display exactly as the source PDF but it should have the same
logical structure. Therefore, it is more suitable for further analysis and/or editing.


Acknowledgements
----------------
This work was supported by the BUT FIT grant FIT-S-14-2299 and the IT4Innovations Centre of Excellence CZ.1.05/1.1.00/02.0070.
