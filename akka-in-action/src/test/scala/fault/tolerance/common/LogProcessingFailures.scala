package fault.tolerance.common

import java.io.File

@SerialVersionUID(1)
class DiskError(msg: String)
  extends Error(msg) with Serializable

@SerialVersionUID(1)
class CorruptedFileException(msg: String, val file: File)
  extends Exception(msg) with Serializable

@SerialVersionUID(1)
class DBNodeDownException(msg: String)
  extends Exception(msg) with Serializable

@SerialVersionUID(1)
class DBbrokenConnectionException(msg: String)
  extends Exception(msg) with Serializable
