package controllers

import javax.inject._

import play.api.data.Form
import play.api.data.Forms._
import models.AccountDataAccess
import play.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}


case class LoginForm(account : String, password : String)

/**
  * Created by leehwangchun on 2017. 4. 2..
  */
@Singleton
class LoginController @Inject()(ac : AccountDataAccess)(implicit e : ExecutionContext, val config: Configuration, val messagesApi: MessagesApi)
  extends Controller with I18nSupport {
  val loginForm = Form (
    mapping(
      "account" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )

  def index = Action {
    Ok(views.html.index())
  }

  def login = Action { request =>
    request.session.get("userName")
      .map(account => Ok(views.html.userInfo(account)).withSession(request.session))
      .getOrElse(Ok(views.html.login(loginForm,"Welcome!")))
  }

  def formParseError(formWithErrors : Form[LoginForm]) : Result = {
    BadRequest(views.html.login(loginForm,"ID or PW is empty"))
  }

  def verifyLogin(request : Request[LoginForm]) : Future[Result] = {
    val loginData = request.body
    ac.verifyPassword(loginData.account, loginData.password).map {
      case Success(a) =>  {
        if (a.nonEmpty) {
          Redirect(routes.LoginController.login)
            .withSession("userName" -> a.head.userName, "uid" -> a.head.uid.toString)
        }
        else Ok(views.html.login(loginForm,"Invalid ID/PW"))
      }
      case Failure(t) => Ok(t.toString + "Failure!!")
    }
  }

  def loginProcess = Action.async(parse.form(loginForm, onErrors = formParseError))(verifyLogin)
}
