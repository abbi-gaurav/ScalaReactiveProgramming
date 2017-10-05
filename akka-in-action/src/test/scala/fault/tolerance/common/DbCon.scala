package fault.tolerance.common

class DbCon(url: String) {
  /**
    * Writes a map to a database.
    * @param map the map to write to the database.
    * @throws DbBrokenConnectionException when the connection is broken. It might be back later
    * @throws DBNodeDownException when the database Node has been removed from the database cluster. It will never work again.
    */
  def write(map: Map[Symbol, Any]): Unit =  {
    //
  }

  def close(): Unit = {
    //
  }
}
