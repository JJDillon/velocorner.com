package velocorner.manual

import org.slf4s.Logging
import velocorner.SecretConfig
import velocorner.feed.StravaActivityFeed
import velocorner.storage.Storage

object ActivitiesFromStravaToStorageApp extends App with Logging with MyMacConfig {

  private val config = SecretConfig.load()
  implicit val feed = new StravaActivityFeed(None, config)

  val storage = Storage.create("mo")
  storage.initialize()
  //val activities = StravaFeed.listRecentAthleteActivities
  val activities = StravaActivityFeed.listAllAthleteActivities
  storage.store(activities)

  log.info("done...")
  storage.destroy()
  sys.exit(0)
}
