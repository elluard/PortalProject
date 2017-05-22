import java.sql.Date
import javax.inject.Inject

import models.{Account, AccountDataAccess}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.specs2.mock.Mockito
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationSpec @Inject()(implicit e : ExecutionContext) extends Specification with Mockito{

  "Application" should {
    "send 404 on a bad request" in new WithApplication{
      route(FakeRequest(GET, "/bojjum")) must beNone
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Thomas.com")
    }
  }
}
