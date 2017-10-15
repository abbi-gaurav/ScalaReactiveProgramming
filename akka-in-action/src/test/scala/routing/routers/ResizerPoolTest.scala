package routing.routers

import com.typesafe.config.ConfigFactory

class ResizerPoolTest extends PoolTest("resizer-test", ConfigFactory.load("routing/resizer.conf"))
