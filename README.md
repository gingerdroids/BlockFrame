# BlockFrame
BlockFrame is a layer on top of Apache PDF-Box. Essentially, PDF-Box knows how to write content to a PDF, but not where. BlockFrame fills this gap. It measures and positions content, then writes a PDF file using PDF-Box.

BlockFrame has been developed for highly structured documents. It is designed to be extensible. 

There are several example programs, for use as a tutorial. 

This is an early version, uploaded to GitHub to guage interest. It is fully functioning - I've used it to layout and draw the grid and clues for a crossword. If there is sufficient interest, I will extend and maintain it. In particular, I could add a package which understands the peculiarites of text documents - sections, headers, footnotes, table & diagram inserts, etc. 

BlockFrame currently uses library PDF-Box 2.0.3. 
