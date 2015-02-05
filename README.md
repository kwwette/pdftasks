PDFTasks
========

A plugin for JabRef to assist with managing links to PDF files.

Releases
--------

### PDFTasks 0.5 (2015-02-05)

- Respect JabRef's preferred file directories given under the "External
  programs" tab in the Preferences, in particular the "Main file
  directory" and the "Use the bib file location as primary file
  directory" options.
  + When scanning the Bibtex database, PDF files are searched for
    relative to any of the preferred file directories (or the bib file
    location if none are supplied).
  + Links are always saved as relative paths to the highest-priority
    preferred file directory (as determined by JabRef itself).

### PDFTasks 0.4 (2014-10-22)

- Update to Java 1.7.

### PDFTasks 0.3 (2014-07-04)

- Included license and README in jar.

### PDFTasks 0.2 (2013-08-26)

- Upgraded to use PDFBox 1.7.0, used by JabRef 2.7.

### PDFTasks 0.1 (2011-08-12)

- Initial release.
