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
    Redirect(routes.LoginController.login).withNewSession
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
      case Success(a) => Redirect(routes.LoginController.login)
      case Failure(a) => BadRequest(views.html.join(joinForm, "DB Error" + a.toString))
    }

    //ac.searchUserByName(joinData.account).flatMap {
    //  case Success(a) if a.nonEmpty => Future(BadRequest(views.html.join(joinForm, "Account name already exists")))
    //  case Success(a) => ac.insertNewUser(dbData).map {
    //    case Success(b) => Redirect(routes.LoginController.index)
    //    case Failure(b) => BadRequest(views.html.join(joinForm, "DB Error" + b.toString))
    //  }
    //  case Failure(a) => Future(BadRequest(views.html.join(joinForm, "DB Error" + a.toString)))
    //}

    //for 함축. 조금만 더 생각해보자
    //for {
    //  a <- ac.searchUserByName(joinData.account)
    //  b <- a match {
    //    case Success(a) if a.nonEmpty => Future(BadRequest(views.html.join(joinForm, "Account name already exists")))
    //    case Success(a) => ac.insertNewUser(dbData).map {
    //          case Success(b) => Redirect(routes.LoginController.index)
    //          case Failure(b) => BadRequest(views.html.join(joinForm, "DB Error" + b.toString))
    //    }
    //    case Failure(a) => Future(BadRequest(views.html.join(joinForm, "DB Error" + a.toString)))
    //  }
    //} yield b
  }

  def joinProcess = Action.async(parse.form(joinForm, onErrors = joinformParseError))(insertAccount)
}