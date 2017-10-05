package fault.tolerance.common

import java.io.File

import fault.tolerance.strategy1.DbWriter.Line

trait LogParsing {
  // Parses log files. creates line objects from the lines in the log file.
  // If the file is corrupt a CorruptedFileException is thrown
  def parse(file: File): Vector[Line] = {
    // implement parser here, now just return dummy value
    Vector.empty[Line]
  }
}

