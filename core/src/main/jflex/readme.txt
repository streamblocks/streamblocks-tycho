The CAL (actor) and network scanners are divided to 4 parts. There is a large overlap between the languages and therefore the scanners have some common source code (expressions, statements et.c.). The macros.flex and postamble.flex are shared between the two scanners.

Here is an ant target for assembling the files:

    <concat destfile="bin/generated/NlScanner.flex" binary="true" force="false">
      <filelist dir="source/jflex">
        <file name="NlPreamble.flex"/>
        <file name="macros.flex"/>
        <file name="NlKeywords.flex"/>
        <file name="postamble.flex"/>
      </filelist>
    </concat>
    <concat destfile="bin/generated/CalScanner.flex" binary="true" force="false">
      <filelist dir="source/jflex">
        <file name="CalPreamble.flex"/>
        <file name="macros.flex"/>
        <file name="CalKeywords.flex"/>
        <file name="postamble.flex"/>
      </filelist>
    </concat>

