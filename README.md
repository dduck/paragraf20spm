# Paragraf 20 spørgsmål: Download og uddragelse af kernetekst

## What
Scripts and final data from harvesting §20 questions from the Danish Paralament 2021-2024

PDF files omitted for obvious reasons

## Prerequisites

* A platform that can host Java, wget, pdftotext, jq and curl
* Time (it takes several hours to download the PDFs)
* Disk space (a gig is best)


## How

### Get the initial JSON with metadata about the p20 questions
Use Java program ``BuildDownloadScript`` to create a script to download the initial JSON files from the Danish Parament's Open Data platform

These files contain metadata about the p20 questions and links to the PDFs for questions and answers

Collect the output from the Java code in a script file (I used ``getit.sh``). Make it executable with ``chmod``. Execute and it will download the initial JSON in chunks.

## Get the PDF files that contain the p20 questions and answers
Extract the list of files to download using ``jq ".value[].Dokument.Fil[].filurl" file*.json | sort -n | grep -v '""' > filelist.txt``

Use Java program ``BuildWgetScript`` to read the ``filelist.txt`` and generate a script that will download the PDF files (``downloadpdf.sh``). 

Make it executable with ``chmod``. Execute and it will download the PDF files and extract the text in them using ``pdftotext``

## Extract additional metadata for the questions and answers from the Parlament JSON
Generate list of metadata to include in big json using ``jq ".value[].Dokument.Fil[] | .titel, .versionsdato, .filurl" file*.json > titlerdatofiler.txt``

Use Java program ``GenerateJson`` to read the ``titlerdatofiler.txt`` and generate huge JSON file called ``bigjson.json``

This Java program will create a big JSON file that for each question contains 

1. Link to the PDF it came from
2. Date of the file
3. Full text extracted from the file
4. A cleaned up version of that text which only contains the actual question
5. A list of 0..n questions, for each
  5.1 Link to the PDF it came from
  5.2. Date of the file
  5.3 Full text extracted from the file
  5.4 4. A cleaned up version of that text which only contains the actual question


Use ``jq . bigjson.json prettyjson.json`` to make that JSON more human-friendly