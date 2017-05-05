package controllers

import java.sql.Date
import javax.inject._

import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.Configuration
import models.AccountDataAccess
import models.Account
import play.api.http.Status

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

case class JoinForm(account : String, password : String, userName : String)

@Singleton
class JoinController @Inject()(ac : AccountDataAccess)(implicit e : ExecutionContext, implicit val config: Configuration, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {
  val joinForm = Form (
    mapping(
      "account" -> nonEmptyText,
      "password" -> nonEmptyText,
      "userName" -> nonEmptyText
    )(JoinForm.apply)(JoinForm.unapply)
  )

  def logOut = Action {
    Redirect(routes.LoginController.index).withNewSession
  }

  def joinformParseError(formWithErrors : Form[JoinForm]) : Result = {
    BadRequest(views.html.join(joinForm, "Insert All Fields"))
  }

  def join = Action {
    Ok(views.html.join(joinForm, "Enter below"))
  }

  def insertAccount(request : Request[JoinForm]) : Future[Result] = {
    val joinData = request.body
    val dbData = Account(0, joinData.account, joinData.password, new Date(0), joinData.userName)

    ac.insertNewUser(dbData).map {
      case Right(a) => Redirect(routes.LoginController.index)
      case Left(a) => BadRequest(views.html.join(joinForm, a))
    }
  }

  def joinProcess = Action.async(parse.form(joinForm, onErrors = joinformParseError))(insertAccount)
}