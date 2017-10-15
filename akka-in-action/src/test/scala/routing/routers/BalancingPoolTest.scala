package routing.routers

import com.typesafe.config.ConfigFactory

class BalancingPoolTest extends PoolTest("balancing-pool", ConfigFactory.load("routing/pool-router.conf"))
