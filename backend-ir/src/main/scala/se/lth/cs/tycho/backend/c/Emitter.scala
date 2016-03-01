package se.lth.cs.tycho.backend.c

import java.io.Writer

class Emitter(writer: Writer) {
  private var ind: Int = 0

  def indent(): Unit = {
    ind = ind + 1
  }

  def deindent(): Unit = {
    ind = ind - 1
  }

  def emit(s: String): Unit = {
    var i = ind
    while (i > 0) {
      writer.write('\t')
      i = i-1
    }
    writer.write(s)
    writer.write('\n')
  }

  def newline(): Unit = writer.write('\n')

  def flush(): Unit = writer.flush()

  def close(): Unit = writer.close()

}
