val org = List(0, -2147483648)
val sorted = org.sortWith{case (x,y) => x > y}
sorted == org
