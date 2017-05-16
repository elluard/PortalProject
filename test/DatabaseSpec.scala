import java.sql.Date
import javax.inject.Inject

import models.{Account, AccountDataAccess}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.specs2.mock._
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
  * Created by leehwangchun on 2017. 5. 3..
  */
@RunWith(classOf[JUnitRunner])
class DatabaseSpec @Inject()(implicit e : ExecutionContext) extends Specification with Mockito{
  "AccountDataAccess#getList" should {
    "test one" in {
      "login Test, Success" in new WithApplication {
        val account = mock[AccountDataAccess]
        account.verifyPassword("elluard", "2345") returns Future(Right(Account(99, "elluard", "2345", new Date(2011, 1, 1), "이황춘")))
        true
      }
    }
  }
}
